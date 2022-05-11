package org.toxsoft.uskat.s5.server.backend.impl;

import static java.lang.String.*;
import static org.toxsoft.core.log4j.Logger.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.s5.client.IS5ConnectionParams.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.IS5ServerHardConstants.*;
import static org.toxsoft.uskat.s5.server.backend.impl.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.sessions.cluster.S5ClusterCommandCloseCallback.*;
import static org.toxsoft.uskat.s5.server.sessions.cluster.S5ClusterCommandCreateCallback.*;
import static ru.uskat.core.api.users.ISkSession.*;

import java.rmi.RemoteException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;
import org.toxsoft.core.tslib.bricks.time.impl.TimedList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ELogSeverity;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.core.impl.S5EventSupport;
import org.toxsoft.uskat.s5.client.remote.connection.S5ClusterTopology;
import org.toxsoft.uskat.s5.common.sessions.IS5SessionInfo;
import org.toxsoft.uskat.s5.server.backend.*;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddon;
import org.toxsoft.uskat.s5.server.backend.supports.events.IS5BackendEventSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.links.IS5BackendLinksSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.lobs.IS5BackendLobsSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.objects.IS5BackendObjectsSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.IS5BackendSysDescrSingleton;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterManager;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.sessions.*;
import org.toxsoft.uskat.s5.server.sessions.init.*;
import org.toxsoft.uskat.s5.server.sessions.pas.S5SessionCallbackWriter;

import ru.uskat.backend.ISkBackendInfo;
import ru.uskat.backend.SkBackendInfo;
import ru.uskat.common.dpu.*;
import ru.uskat.common.dpu.impl.DpuObject;
import ru.uskat.common.dpu.rt.events.SkEvent;
import ru.uskat.core.api.ISkExtServicesProvider;
import ru.uskat.core.api.ISkSystem;
import ru.uskat.core.api.users.ISkSession;
import ru.uskat.core.api.users.ISkUser;
import ru.uskat.core.impl.SkUserService;
import ru.uskat.legacy.IdPair;

/**
 * Абстрактная реализация сессии {@link IS5Backend}.
 *
 * @author mvk
 */
@Stateful
@StatefulTimeout( value = STATEFULL_TIMEOUT, unit = TimeUnit.MILLISECONDS )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
@TransactionManagement( TransactionManagementType.CONTAINER )
public class S5BackendSession
    implements SessionBean, IS5BackendLocal, IS5BackendRemote {

  private static final long serialVersionUID = 157157L;

  /**
   * Контекст сессии
   */
  @Resource
  private SessionContext sessionContext;

  /**
   * Идентификатор объекта сессии {@link ISkSession}
   */
  private Skid sessionID;

  /**
   * Менеджер кластера
   */
  @EJB
  private IS5ClusterManager clusterManager;

  /**
   * Менеджер сессий
   */
  @EJB
  private IS5SessionManager sessionManager;

  /**
   * Синглетон реализующий s5-backend
   */
  @EJB
  private IS5BackendCoreSingleton backendCoreSingleton;

  /**
   * Поддержка системного описания
   */
  @EJB
  private IS5BackendSysDescrSingleton sysdescrBackend;

  /**
   * Поддержка управления объектами
   */
  @EJB
  private IS5BackendObjectsSingleton objectsBackend;

  /**
   * Поддержка управления связями между объектами
   */
  @EJB
  private IS5BackendLinksSingleton linksBackend;

  /**
   * Поддержка формирования событий системы
   */
  @EJB
  private IS5BackendEventSingleton eventBackend;

  /**
   * backend управления большими объектами объектами (Large OBject - LOB) системы
   */
  @EJB
  private IS5BackendLobsSingleton lobsBackend;

  /**
   * Ссылка на собственный локальный интерфейс.
   * <p>
   * Прямой доступ запрещен(transient), используйте {@link #selfLocal()}
   */
  private transient IS5BackendLocal selfLocal;

  /**
   * Ссылка на собственный удаленный интерфейс.
   * <p>
   * Прямой доступ запрещен(transient), используйте {@link #selfRemote()}
   */
  private transient IS5BackendRemote selfRemote;

  /**
   * Карта локального доступа к расширениям.
   * <p>
   * Ключ: идентификатор расширения;<br>
   * Значение: локальный доступ к расширению.
   */
  private final IStringMapEdit<IS5BackendAddonSession> addonSessions = new StringMap<>();

  /**
   * Признак того, что сессия готова к удалению
   */
  private boolean removeReady = false;

  /**
   * Журнал работы
   * <p>
   * Прямой доступ запрещен(transient), используйте {@link #logger()}
   */
  private transient ILogger logger;

  /**
   * Конструктор для наследников.
   */
  protected S5BackendSession() {
  }

  // ------------------------------------------------------------------------------------
  // Открытые методы
  //
  /**
   * Возвращает текущий контекст сессии
   *
   * @return {@link SessionContext} контекст сессии
   */
  public final SessionContext sessionContext() {
    return sessionContext;
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса SessionBean
  //
  @Override
  public final void setSessionContext( SessionContext aContext )
      throws EJBException,
      RemoteException {
    logger().info( MSG_SESSION_CONTEXT, aContext );
  }

  @Override
  public final void ejbRemove()
      throws EJBException,
      RemoteException {
    if( sessionID == null ) {
      logger().error( "ejbRemove(...): sessionID = null" ); //$NON-NLS-1$
      return;
    }
    // Имя класса сессии завешающей работы
    try {
      // Завершение работы сессии (кэш кластера)
      sessionManager.closeRemoteSession( sessionID );
    }
    catch( RuntimeException e ) {
      // Ошибка завершения работы
      logger().error( e, ERR_SESSION_CLOSE, sessionID, cause( e ) );
    }
    try {
      // Оповещение других узлов: boolean remoteOnly = true; boolean primaryOnly = false
      clusterManager.sendAsyncCommand( closeCallbackCommand( sessionID ), true, false );
    }
    catch( RuntimeException e ) {
      // Ошибка завершения работы
      logger().error( e, ERR_SESSION_CLOSE, sessionID, cause( e ) );
    }
    if( removeReady ) {
      // Штатное завершение работы
      logger().info( MSG_SESSION_REMOVE, sessionID );
      return;
    }
    // Аварийное завершение работы сессии службы: контейнер вывел компонент в пул, возможно по "грязному" исключению
    logger().error( ERR_SESSION_REMOVE, sessionID );
  }

  @Override
  public final void ejbActivate()
      throws EJBException,
      RemoteException {
    logger().debug( MSG_SESSION_ACTIVATE, sessionID );
  }

  @Override
  public final void ejbPassivate()
      throws EJBException,
      RemoteException {
    logger().debug( MSG_SESSION_PASSIVATE, sessionID );
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IS5BackendRemote
  //
  @Override
  // TODO: 2020-09-02 mvkd
  // @TransactionAttribute( TransactionAttributeType.REQUIRES_NEW )
  @TransactionAttribute( TransactionAttributeType.NOT_SUPPORTED )
  public IS5SessionInitResult init( IS5SessionInitData aInitData ) {
    TsNullArgumentRtException.checkNull( aInitData );
    String login = "???"; //$NON-NLS-1$
    String pswd = "???"; //$NON-NLS-1$
    IAtomicValue remoteAddress = IAtomicValue.NULL;
    IAtomicValue remotePort = IAtomicValue.NULL;
    try {
      // Метка времени начала образования сессии
      long startTime = System.currentTimeMillis();
      // Инициализация интерфейсов доступа
      String principal = sessionContext.getCallerPrincipal().getName();
      // Идентификатор сессии
      sessionID = aInitData.sessionID();
      // Вывод в журнал
      logger().info( MSG_CREATE_SESSION_REQUEST, principal, sessionID, remoteAddress, remotePort );

      // Просто для информации: отсюда можно получить remoteAddr клиента
      // Map<String, Object> contextData = sessionContext.getContextData();
      // Адрес удаленного клиента
      // InetSocketAddress remoteAddr = (InetSocketAddress)contextData.get( "jboss.source-address" );

      // Проверка пользователя
      IOptionSet options = aInitData.connectionOptions();
      remoteAddress = OP_CLIENT_ADDRESS.getValue( options );
      remotePort = OP_CLIENT_PORT.getValue( options );
      login = OP_USERNAME.getValue( options ).asString();
      pswd = OP_PASSWORD.getValue( options ).asString();
      if( !login.equals( principal ) ) {
        // Учтеная запись входа не соответствует вызову
        throw new S5AccessDeniedException( ERR_WRONG_USER );
      }
      // Пользователь системы
      IDpuObject user = objectsBackend.findObject( new Skid( ISkUser.CLASS_ID, login ) );
      if( user == null ) {
        // Доступ запрещен - неверное имя пользователя или пароль
        Gwid eventGwid = Gwid.createEvent( ISkSystem.CLASS_ID, ISkSystem.THIS_SYSTEM, ISkSystem.EVID_LOGIN_FAILED );
        IOptionSetEdit params = new OptionSet();
        params.setStr( ISkSystem.EVPID_LOGIN, login );
        params.setStr( ISkSystem.EVPID_IP, remoteAddress.asString() );
        SkEvent event = new SkEvent( startTime, eventGwid, params );
        eventBackend.fireAsyncEvents( IS5FrontendRear.NULL, new TimedList<>( event ) );
        throw new S5AccessDeniedException( ERR_WRONG_USER );
      }
      // Пароль или его хэшкод пользователя
      String userPswd = user.attrs().getValue( ISkUser.ATRID_PASSWORD ).asString();
      // Хэшкод пароля
      String pswdHashCode = SkUserService.getPasswordHashCode( pswd );
      // Пароль указан в "сыром" виде
      boolean isPlainPswd = userPswd.equals( pswd );
      // Пароль указан в виде хэшкода
      boolean isHashCodePswd = userPswd.equals( pswdHashCode );
      if( !isPlainPswd && !isHashCodePswd ) { // Доступ запрещен - неверное имя пользователя или пароль
        Gwid eventGwid = Gwid.createEvent( ISkSystem.CLASS_ID, ISkSystem.THIS_SYSTEM, ISkSystem.EVID_LOGIN_FAILED );
        IOptionSetEdit params = new OptionSet();
        params.setStr( ISkSystem.EVPID_LOGIN, login );
        params.setStr( ISkSystem.EVPID_IP, remoteAddress.asString() );
        SkEvent event = new SkEvent( startTime, eventGwid, params );
        eventBackend.fireAsyncEvents( IS5FrontendRear.NULL, new TimedList<>( event ) );
        throw new S5AccessDeniedException( ERR_WRONG_USER );
      }
      if( isPlainPswd ) {
        // Формируем для пароля hashCode
        IOptionSetEdit attrs = new OptionSet( user.attrs() );
        attrs.setValue( ISkUser.ATRID_PASSWORD, avStr( pswdHashCode ) );
        IDpuObject dpu = new DpuObject( user.skid(), attrs );
        objectsBackend.writeObjects( IS5FrontendRear.NULL, ISkidList.EMPTY, new ElemArrayList<>( dpu ), false );
        logger().warning( "init(...): write password hashcode" ); //$NON-NLS-1$
      }
      // 2021-09-18 mvkd требуется отсекать "старых" клиентов
      // Проверка версии клиента
      IAtomicValue clientVersion = OP_CLIENT_VERSION.getValue( options );
      if( clientVersion != null && clientVersion.isAssigned() && clientVersion.atomicType() == EAtomicType.STRING ) {
        // Неподдерживаемая версия клиента
        throw new S5AccessDeniedException( String.format( ERR_WRONG_VERSION, clientVersion ) );
      }

      // Информация о сессии пользователя
      S5RemoteSession prevSession = sessionManager.findSession( sessionID );
      IS5SessionInfo prevSessionInfo = (prevSession != null ? prevSession.info() : null);
      if( prevSessionInfo != null ) {
        // Попытка повторной инициализации сессии пользователя
        logger().error( ERR_DOUBLE_INIT, prevSessionInfo );
      }

      // Формирование локального, удаленного доступа к сессии backend и его расширениям
      IS5BackendLocal local = selfLocal();
      IS5BackendRemote remote = selfRemote();
      TsInternalErrorRtException.checkNull( local );
      TsInternalErrorRtException.checkNull( remote );

      Properties prop = new Properties();
      prop.put( Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming" ); //$NON-NLS-1$
      Context context = new InitialContext( prop );
      try {
        // Доступные расширения бекенда предоставляемые сервером
        IStridablesList<IS5BackendAddon> addons =
            backendCoreSingleton.initialConfig().impl().getBackendAddonsProvider().addons();
        for( String addonId : addons.keys() ) {
          IS5BackendAddonSession addonSession = addons.getByKey( addonId ).createSession( context );
          if( addonSession == null ) {
            // Расширение не работает с сессиями
            continue;
          }
          addonSessions.put( addonSession.id(), addonSession );
        }
      }
      finally {
        context.close();
      }
      IS5SessionInfoEdit sessionInfo = new S5SessionInfo( principal, options );
      sessionInfo.setSessionID( sessionID );
      sessionInfo.setRemoteAddress( remoteAddress.asString(), remotePort.asInt() );
      // Установка топологии кластеров доступных клиенту при создании сессии
      sessionInfo.setClusterTopology( aInitData.clusterTopology() );
      // Запущен процесс создания сессии
      logger().info( MSG_CREATE_SESSION_START, sessionInfo );
      // Сессия
      S5RemoteSession session = new S5RemoteSession( local, sessionInfo );
      // Создание писателя обратных вызовов для сессии и его регистрация в backend
      S5SessionCallbackWriter callbackWriter = null;
      try {
        callbackWriter = sessionManager.createCallbackWriter( session );
      }
      catch( TsItemNotFoundRtException e ) {
        // Не найден канал для обратных вызовов
        logger().error( e.getLocalizedMessage() );
        // Завершение сессии
        selfLocal().removeAsync();
        // Доводим информацию об ошибке до клиента
        throw sessionError( e );
      }

      // 2021-04-10 mvk
      // Сохранение информации о пользователе в кэше
      // sessionManager.createRemoteSession( session );

      // Результат инициализации сессии
      S5SessionInitResult initResult = new S5SessionInitResult();
      // Текущие типы и классы
      initResult.setTypeInfos( sysdescrBackend.readTypeInfos() );
      initResult.setClassInfos( sysdescrBackend.readClassInfos() );
      // Метка времени начала инициализации расширений сессии
      long addonsInitStartTime = System.currentTimeMillis();
      // Инициализация сессии данными клиента
      initResult.setAddons( getRemoteReferences( addonSessions ) );
      // Подписка на события в сессии
      session.frontendData().events().setNeededEventGwids( aInitData.eventGwids() );
      // Инициализация расширений службы
      for( String addonId : addonSessions.keys() ) {
        IS5BackendAddonSession addonSession = addonSessions.getByKey( addonId );
        try {
          addonSession.init( selfLocal(), callbackWriter, aInitData, initResult );
        }
        catch( Throwable e ) {
          // Неожиданная ошибка инициализации расширения
          logger().error( e, ERR_UNEXPECTED_EXTENSION_INIT, addonId, cause( e ) );
        }
      }
      // 2021-04-10 mvk
      // Сохранение информации о пользователе в кэше
      sessionManager.createRemoteSession( session );

      // Время (мсек) инициализации расширений сессии
      Long addonsInitTime = Long.valueOf( System.currentTimeMillis() - addonsInitStartTime );

      // TODO: 2020-05-07 mvk
      // // Установка пользователя сессии
      // DpuLinkFwd dpuUserLink = new DpuLinkFwd( ISkSession.CLASS_ID, ISkSession.LNKID_USER, sessionSkid, user.skid()
      // );
      // linksBackend.writeLinks( new ElemArrayList<>( dpuUserLink ), false ); // false: запрет интерсепторов
      // Оповещение других узлов: boolean remoteOnly = true; boolean primaryOnly = false;
      clusterManager.sendAsyncCommand( createCallbackCommand( sessionID ), true, false );

      // Завершен процесс создания сессии
      Long time = Long.valueOf( System.currentTimeMillis() - startTime );
      logger().info( MSG_CREATE_SESSION_FINISH, sessionInfo, time, addonsInitTime );
      return initResult;
    }
    catch( S5AccessDeniedException e ) {
      // Неверное имя пользователя или пароль
      logger().error( ERR_REJECT_CONNECT, login, remoteAddress, remotePort, cause( e ) );
      // Завершение сессии
      selfLocal().removeAsync();
      throw e;
    }
    catch( S5SessionApiRtException e ) {
      // Отказ подключения к системе клиента
      logger().error( e, ERR_REJECT_CONNECT, login, remoteAddress, remotePort, cause( e ) );
      // Завершение сессии
      selfLocal().removeAsync();
      // Доводим информацию об ошибке до клиента
      throw e;
    }
    catch( Exception e ) {
      // Неожиданная ошибка подключения клиента
      logger().error( e, ERR_UNEXPECTED_CONNECT_FAIL, login, remoteAddress, remotePort, cause( e ) );
      // Завершение сессии
      selfLocal().removeAsync();
      // Доводим информацию об ошибке до клиента
      throw sessionError( e );
    }
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public void setClusterTopology( S5ClusterTopology aClusterTopology ) {
    TsNullArgumentRtException.checkNull( aClusterTopology );
    // Изменение топологии кластеров доступных клиенту
    S5RemoteSession session = sessionManager.findSession( sessionID );
    session.info().setClusterTopology( aClusterTopology );
    sessionManager.updateRemoteSession( session );
    try {
      // Сохранение топологии в ISkSession
      IDpuObject obj = objectsBackend.findObject( sessionID );
      IOptionSetEdit attrs = new OptionSet( obj.attrs() );
      IOptionSetEdit backendSpecificParams = new OptionSet( attrs.getValobj( ATRID_BACKEND_SPECIFIC_PARAMS ) );
      OP_SESSION_CLUSTER_TOPOLOGY.setValue( backendSpecificParams, avValobj( aClusterTopology ) );
      attrs.setValobj( ATRID_BACKEND_SPECIFIC_PARAMS, backendSpecificParams );
      IDpuObject dpu = new DpuObject( sessionID, attrs );
      // Создание объекта сессия. false: интерсепция запрещена
      objectsBackend.writeObjects( IS5FrontendRear.NULL, ISkidList.EMPTY, new ElemArrayList<>( dpu ), false );
    }
    catch( Throwable e ) {
      logger().error( e );
    }
  }

  @Override
  @AccessTimeout( value = CHECK_ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public void verify() {
    // Начало проверки работы сессии
    logger().info( MSG_SESSION_VERIFY_START, sessionID );
    for( String addonId : addonSessions.keys() ) {
      try {
        IS5BackendAddonSession addonSession = addonSessions.getByKey( addonId );
        addonSession.verify();
      }
      catch( NoSuchEJBException e ) {
        // Неожиданное завершение работы расширения сессии
        logger().error( ERR_UNEXPECTED_EXTENSION_CHECK2, addonId, cause( e ) );
        selfLocal().removeAsync();
      }
      catch( Throwable e ) {
        // Неожиданная ошибка проверки расширения сессии
        logger().error( ERR_UNEXPECTED_EXTENSION_CHECK, addonId, cause( e ) );
        selfLocal().removeAsync();
      }
    }
    // Завершение проверки работы сессии
    logger().info( MSG_SESSION_VERIFY_FINISH, sessionID );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public void close( Skid aSessionID ) {
    TsIllegalStateRtException.checkNull( aSessionID );
    S5RemoteSession session = sessionManager.findSession( aSessionID );
    if( session == null ) {
      // Сессия не найдена
      throw new TsIllegalArgumentRtException( ERR_SESSION_NOT_FOUND, aSessionID );
    }
    if( session.info().closeTime() != TimeUtils.MAX_TIMESTAMP ) {
      // Сессия уже завершена
      throw new TsIllegalArgumentRtException( ERR_SESSION_ALREADY_CLOSED, aSessionID );
    }
    if( !aSessionID.equals( sessionID ) ) {
      // TODO: здесь можно проверить права доступа на завершение чужой сессии
    }
    // Установка признака: завершение сессии по инициативе пользователя
    session.info().setCloseByRemote( true );
    sessionManager.updateRemoteSession( session );
    // 2020-10-15 mvk
    // Асинронное удаление сессии
    // selfLocal().removeAsync();
    session.backend().removeAsync();
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IS5BackendLocal
  //
  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IS5SessionInfo sessionInfo() {
    if( sessionID == null ) {
      logger().error( S5BackendSession.class.getSimpleName() + ".sessionInfo().userSessionID = null" ); //$NON-NLS-1$
      return null;
    }
    S5RemoteSession session = sessionManager.findSession( sessionID );
    if( session == null ) {
      return null;
    }
    IS5SessionInfo retValue = session.info();
    return retValue;
  }

  @Override
  @AccessTimeout( value = CHECK_ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
  @TransactionAttribute( TransactionAttributeType.NOT_SUPPORTED )
  public String nodeName() {
    String nodeName = System.getProperty( JBOSS_NODE_NAME );
    return nodeName;
  }

  @Remove
  @Asynchronous
  @AccessTimeout( value = REMOVE_ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
  // @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @TransactionAttribute( TransactionAttributeType.REQUIRES_NEW )
  @Override
  public void removeAsync() {
    // Поступил before-запрос на завершение сессии
    logger().info( MSG_SESSION_QUERY_BEFORE_REMOVE, sessionID );
    try {
      if( removeReady ) {
        // Попытка повторного удаления готовой к завершению сессии (beforeRemove)
        logger().debug( ERR_BEFORE_REMOVE_ALREADY, sessionID );
        return;
      }
      // Запуск процесса на удаление сессии
      logger().info( MSG_BEFORE_REMOVE_SESSION, sessionID );
      // Завершение работы расширений
      for( String addonId : addonSessions.keys() ) {
        try {
          IS5BackendAddonSession addonSession = addonSessions.getByKey( addonId );
          addonSession.close();
        }
        catch( Throwable e ) {
          // Неожиданная ошибка завершения работы расширения
          logger().error( ERR_UNEXPECTED_EXTENSION_CLOSE, addonId, cause( e ) );
        }
      }
      // Сессия и callback закрываются в ejbRemove()
      // sessionManager.closeSession( wildflySessionID );
      // Завершение работы callback сессии
      // sessionManager.closeCallbackWriter( wildflySessionID );
    }
    catch( Exception e ) {
      throw sessionError( e );
    }
    // Сессия готова к удалению
    removeReady = true;
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса ICloseable
  //
  @Asynchronous
  @Override
  public void close() {
    if( sessionID == null ) {
      logger().error( S5BackendSession.class.getSimpleName() + ".close().userSessionID = null" ); //$NON-NLS-1$
      return;
    }
    close( sessionID );
  }

  // ------------------------------------------------------------------------------------
  // Реализация ISkBackend
  //
  @Override
  public boolean isActive() {
    return backendCoreSingleton.isActive();
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public ISkBackendInfo getInfo() {
    // Запрос текущей информации о сервере (backend)
    ISkBackendInfo info = backendCoreSingleton.getInfo();
    // Формирование информации сессии
    SkBackendInfo retValue = new SkBackendInfo( info.id(), info.startTime(), sessionID, info.params() );
    return retValue;
  }

  // ------------------------------------------------------------------------------------
  // Реализация ISkBackend - system description
  //
  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IStridablesList<IDpuSdTypeInfo> readTypeInfos() {
    return sysdescrBackend.getReader().readTypeInfos();
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public void writeTypeInfos( IStringList aRemoveTypeIds, IList<IDpuSdTypeInfo> aNewlyDefinedTypeInfos ) {
    TsNullArgumentRtException.checkNulls( aRemoveTypeIds, aNewlyDefinedTypeInfos );
    sysdescrBackend.writeTypeInfos( aRemoveTypeIds, aNewlyDefinedTypeInfos );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IStridablesList<IDpuSdClassInfo> readClassInfos() {
    return sysdescrBackend.getReader().readClassInfos();
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public void writeClassInfos( IStringList aRemoveClassIds, IStridablesList<IDpuSdClassInfo> aUpdateClassInfos ) {
    TsNullArgumentRtException.checkNulls( aRemoveClassIds, aUpdateClassInfos );
    sysdescrBackend.writeClassInfos( aRemoveClassIds, aUpdateClassInfos );
  }

  // ------------------------------------------------------------------------------------
  // Реализация ISkBackend - objects management
  //
  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IDpuObject findObject( Skid aSkid ) {
    TsNullArgumentRtException.checkNull( aSkid );
    return objectsBackend.findObject( aSkid );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IList<IDpuObject> readObjects( IStringList aClassIds ) {
    TsNullArgumentRtException.checkNull( aClassIds );
    // Некоторые типы реализации IList (например, ElemLinkedList) дают сбой сериализации на больших коллекциях
    return new ElemArrayList<>( objectsBackend.readObjects( aClassIds ) );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IList<IDpuObject> readObjectsByIds( ISkidList aSkids ) {
    TsNullArgumentRtException.checkNull( aSkids );
    // Некоторые типы реализации IList (например, ElemLinkedList) дают сбой сериализации на больших коллекциях
    return new ElemArrayList<>( objectsBackend.readObjectsByIds( aSkids ) );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public void writeObjects( ISkidList aRemovedSkids, IList<IDpuObject> aObjects ) {
    TsNullArgumentRtException.checkNulls( aRemovedSkids, aObjects );
    // Передатчик обратных вызовов (frontend)
    S5SessionCallbackWriter callbackWriter = sessionManager.getCallbackWriter( sessionID );
    // aInterceptable = true
    objectsBackend.writeObjects( callbackWriter, aRemovedSkids, aObjects, true );
  }

  // ------------------------------------------------------------------------------------
  // Реализация ISkBackend - links management
  //
  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IDpuLinkFwd findLink( String aClassId, String aLinkId, Skid aLeftSkid ) {
    TsNullArgumentRtException.checkNulls( aClassId, aLinkId, aLeftSkid );
    return linksBackend.findLink( aClassId, aLinkId, aLeftSkid );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IDpuLinkFwd readLink( String aClassId, String aLinkId, Skid aLeftSkid ) {
    TsNullArgumentRtException.checkNulls( aClassId, aLinkId, aLeftSkid );
    return linksBackend.readLink( aClassId, aLinkId, aLeftSkid );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IDpuLinkRev readReverseLink( String aClassId, String aLinkId, Skid aRightSkid, IStringList aLeftClassIds ) {
    TsNullArgumentRtException.checkNulls( aClassId, aLinkId, aRightSkid, aLeftClassIds );
    return linksBackend.readReverseLink( aClassId, aLinkId, aRightSkid, aLeftClassIds );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public void writeLink( IDpuLinkFwd aLink ) {
    TsNullArgumentRtException.checkNull( aLink );
    linksBackend.writeLink( aLink );
  }

  @Override
  public void writeLinks( IList<IDpuLinkFwd> aLinks ) {
    TsNullArgumentRtException.checkNull( aLinks );
    linksBackend.writeLinks( aLinks, true );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IList<IdPair> listLobIds() {
    return lobsBackend.listLobIds();
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public void writeClob( IdPair aId, String aData ) {
    TsNullArgumentRtException.checkNulls( aId, aData );
    lobsBackend.writeClob( aId, aData );
  }

  @Override
  public boolean copyClob( IdPair aSourceId, IdPair aDestId ) {
    TsNullArgumentRtException.checkNulls( aSourceId, aDestId );
    return lobsBackend.copyClob( aSourceId, aDestId );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public String readClob( IdPair aId ) {
    TsNullArgumentRtException.checkNull( aId );
    return lobsBackend.readClob( aId );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public void removeLob( IdPair aId ) {
    TsNullArgumentRtException.checkNull( aId );
    lobsBackend.removeClob( aId );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public void setNeededEventGwids( IGwidList aNeededGwids ) {
    TsNullArgumentRtException.checkNull( aNeededGwids );
    // Писатель обратных вызовов сессии
    S5SessionCallbackWriter callbackWriter = sessionManager.getCallbackWriter( sessionID );
    // Сессия связанная с писателем обратных вызовов
    S5RemoteSession session = callbackWriter.session();
    // Поддержка подписки на события в сессии
    S5EventSupport eventSupport = callbackWriter.frontendData().events();
    // Реконфигурация набора
    eventSupport.setNeededEventGwids( aNeededGwids );
    // Сохранение измененной сессии в кэше
    sessionManager.updateRemoteSession( session );
    if( logger().isSeverityOn( ELogSeverity.INFO ) || logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
      IGwidList eventIds = eventSupport.gwids();
      // Вывод в журнал информации о регистрации ресурсов в сессии
      StringBuilder sb = new StringBuilder();
      sb.append( format( "setNeededEventGwids(...): sessionID = %s, changed resources:", sessionID ) ); //$NON-NLS-1$
      sb.append( format( "\n   === events (%d) === ", Integer.valueOf( eventIds.size() ) ) ); //$NON-NLS-1$
      for( Gwid gwid : eventIds ) {
        sb.append( format( "\n   %s", gwid ) ); //$NON-NLS-1$
      }
      logger().info( sb.toString() );
    }

  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public ISkExtServicesProvider getExtServicesProvider() {
    return backendCoreSingleton.initialConfig().impl().getExtServicesProvider();
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public <T> T getBackendAddon( String aAddonId, Class<T> aAddonInterface ) {
    TsNullArgumentRtException.checkNulls( aAddonId, aAddonInterface );
    try {
      return aAddonInterface.cast( addonSessions.findByKey( aAddonId ) );
    }
    catch( Exception ex ) {
      throw new TsIllegalArgumentRtException( ex, ex.getMessage() );
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Возвращает ссылку на собственный локальный интерфейс.
   *
   * @return {@link IS5BackendLocal} собственный локальный интерфейс
   */
  private IS5BackendLocal selfLocal() {
    if( selfLocal == null ) {
      selfLocal = sessionContext.getBusinessObject( IS5BackendLocal.class );
    }
    return selfLocal;
  }

  /**
   * Возвращает ссылку на собственный удаленный интерфейс.
   *
   * @return {@link IS5BackendRemote} собственный удаленный интерфейс
   */
  private IS5BackendRemote selfRemote() {
    if( selfRemote == null ) {
      selfRemote = sessionContext.getBusinessObject( IS5BackendRemote.class );
    }
    return selfRemote;
  }

  /**
   * Возвращает карту удаленного доступа к расширениям backend
   *
   * @param aLocalExtensions {@link IStringMap}&lt;{@link IS5BackendAddonSession}&gt; карта локального доступа к
   *          расширениям. <br>
   *          Ключ: идентификатор расширения;<br>
   *          Значение: локальный доступ к расширению.
   * @return {@link IStringMap}&lt;{@link IS5BackendAddonRemote}&gt; карта удаленного доступа к расширениям. <br>
   *         Ключ: идентификатор расширения;<br>
   *         Значение: удаленый доступ к расширению.
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static IStringMap<IS5BackendAddonRemote> getRemoteReferences(
      IStringMapEdit<IS5BackendAddonSession> aLocalExtensions ) {
    TsNullArgumentRtException.checkNull( aLocalExtensions );
    IStringMapEdit<IS5BackendAddonRemote> retValue = new StringMap<>();
    for( IS5BackendAddonSession local : aLocalExtensions ) {
      retValue.put( local.id(), local.selfRemote() );
    }
    return retValue;
  }

  /**
   * Формирует ошибку сессии API из исходного исключения
   *
   * @param aError {@link Exception} исходное исключение
   * @return {@link TsRuntimeException} неожиданная ошибка сессии
   * @throws TsNullArgumentRtException аргумент null
   */
  private TsRuntimeException sessionError( Exception aError ) {
    if( aError == null ) {
      return new TsNullArgumentRtException();
    }
    if( aError instanceof S5SessionApiRtException ) {
      // Уже поднято исключение API сессии
      return (TsRuntimeException)aError;
    }
    S5RemoteSession session = sessionManager.findSession( sessionID );
    logger().error( ERR_API_UNEXPECTED_ERROR, session, cause( aError ) );
    return new S5SessionApiRtException( aError, ERR_API_UNEXPECTED_ERROR, session, cause( aError ) );
  }

  /**
   * Возвращает общий журнал работы
   *
   * @return {@link ILogger} журнал работы
   */
  private ILogger logger() {
    if( logger == null ) {
      logger = getLogger( getClass() );
    }
    return logger;
  }
}
