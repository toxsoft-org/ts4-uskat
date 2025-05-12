package org.toxsoft.uskat.s5.server.backend.impl;

import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.core.api.users.ISkUserServiceHardConstants.*;
import static org.toxsoft.uskat.s5.client.IS5ConnectionParams.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.IS5ServerHardConstants.*;
import static org.toxsoft.uskat.s5.server.backend.impl.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.sessions.cluster.S5ClusterCommandCloseCallback.*;
import static org.toxsoft.uskat.s5.server.sessions.cluster.S5ClusterCommandCreateCallback.*;

import java.rmi.*;
import java.util.*;
import java.util.concurrent.*;

import javax.annotation.*;
import javax.ejb.*;
import javax.naming.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.classes.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.evserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.*;
import org.toxsoft.uskat.core.impl.dto.*;
import org.toxsoft.uskat.s5.client.remote.connection.*;
import org.toxsoft.uskat.s5.common.sessions.*;
import org.toxsoft.uskat.s5.server.*;
import org.toxsoft.uskat.s5.server.backend.*;
import org.toxsoft.uskat.s5.server.backend.addons.*;
import org.toxsoft.uskat.s5.server.backend.supports.clobs.*;
import org.toxsoft.uskat.s5.server.backend.supports.core.*;
import org.toxsoft.uskat.s5.server.backend.supports.events.*;
import org.toxsoft.uskat.s5.server.backend.supports.links.*;
import org.toxsoft.uskat.s5.server.backend.supports.objects.*;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.*;
import org.toxsoft.uskat.s5.server.cluster.*;
import org.toxsoft.uskat.s5.server.frontend.*;
import org.toxsoft.uskat.s5.server.sessions.*;
import org.toxsoft.uskat.s5.server.sessions.init.*;
import org.toxsoft.uskat.s5.server.sessions.pas.*;

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
    implements SessionBean, IS5BackendSessionControl, IS5BackendSession {

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
  private IS5BackendClobsSingleton lobsBackend;

  /**
   * Ссылка на собственный локальный интерфейс.
   * <p>
   * Прямой доступ запрещен(transient), используйте {@link #control()}
   */
  private transient IS5BackendSessionControl control;

  /**
   * Ссылка на собственный удаленный интерфейс.
   * <p>
   * Прямой доступ запрещен(transient), используйте {@link #session()}
   */
  private transient IS5BackendSession selfSession;

  /**
   * Карта локального доступа к расширениям.
   * <p>
   * Ключ: идентификатор расширения;<br>
   * Значение: локальный доступ к расширению.
   */
  private final IStringMapEdit<IS5BackendAddonSessionControl> baSessionCtrls = new StringMap<>();

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
    logger().info( MSG_SESSION_INITIALIZE, aContext );
    initialize();
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
  // Реализация интерфейса IS5BackendSession
  //
  @Override
  public void initialize() {
    logger().info( MSG_SESSION_INITIALIZE );
  }

  @Override
  // TODO: 2020-09-02 mvkd
  // @TransactionAttribute( TransactionAttributeType.REQUIRES_NEW )
  @TransactionAttribute( TransactionAttributeType.NOT_SUPPORTED )
  public IS5SessionInitResult init( IS5SessionInitData aInitData ) {
    TsNullArgumentRtException.checkNull( aInitData );
    String login = "???"; //$NON-NLS-1$
    String loginPasswordHash = "???"; //$NON-NLS-1$
    IAtomicValue remoteAddress = IAtomicValue.NULL;
    IAtomicValue remotePort = IAtomicValue.NULL;
    try {
      // Метка времени начала образования сессии
      long startTime = System.currentTimeMillis();

      // 2025-01-15 mvk --- мы больше не используем систему аутентификации wildfly
      // Инициализация интерфейсов доступа
      // String principal = sessionContext.getCallerPrincipal().getName();

      // Идентификатор сессии
      sessionID = aInitData.sessionID();

      // Просто для информации: отсюда можно получить remoteAddr клиента
      // Map<String, Object> contextData = sessionContext.getContextData();
      // Адрес удаленного клиента
      // InetSocketAddress remoteAddr = (InetSocketAddress)contextData.get( "jboss.source-address" );

      // Параметры подключения клиента к серверу
      IOptionSetEdit clientOptions = new OptionSet( aInitData.clientOptions() );
      remoteAddress = OP_CLIENT_ADDRESS.getValue( clientOptions );
      remotePort = OP_CLIENT_PORT.getValue( clientOptions );
      login = OP_USERNAME.getValue( clientOptions ).asString();
      loginPasswordHash = OP_PASSWORD.getValue( clientOptions ).asString();

      // 2025-01-15 mvk --- мы больше не используем систему аутентификации wildfly
      // if( !login.equals( principal ) ) {
      // // Учтеная запись входа не соответствует вызову
      // throw new S5AccessDeniedException( ERR_WRONG_USER );
      // }

      // Вывод в журнал
      logger().info( MSG_CREATE_SESSION_REQUEST, login, sessionID, remoteAddress, remotePort );

      // Информация о бекенде сервера
      ISkBackendInfo info = backendCoreSingleton.getInfo();
      // Идентификатор сервера
      String serverId = ((Skid)OP_SERVER_ID.getValue( info.params() ).asValobj()).strid();

      // Пользователь системы
      IDtoObject user = objectsBackend.findObject( new Skid( ISkUser.CLASS_ID, login ) );
      if( user == null ) {
        // Доступ запрещен - неверное имя пользователя или пароль
        Gwid eventGwid = Gwid.createEvent( ISkServer.CLASS_ID, serverId, ISkServer.EVID_LOGIN_FAILED );
        IOptionSetEdit params = new OptionSet();
        params.setStr( ISkServer.EVPID_LOGIN, login );
        params.setStr( ISkServer.EVPID_IP, remoteAddress.asString() );
        SkEvent event = new SkEvent( startTime, eventGwid, params );
        eventBackend.fireAsyncEvents( IS5FrontendRear.NULL, new SkEventList( event ) );
        throw new S5AccessDeniedException( ERR_WRONG_USER );
      }
      // Флаг активности учетной записи пользователя
      IAtomicValue isEnabled = user.attrs().findValue( ATRID_USER_IS_ENABLED );
      // Хэшкод пароля учетной записи пользователя
      String userPasswordHash = user.attrs().getStr( ATRID_PASSWORD_HASH );
      if( (isEnabled != null && !isEnabled.asBool()) || !userPasswordHash.equals( loginPasswordHash ) ) {
        // Доступ запрещен - запись пользователя неактивна или неверное имя пользователя или пароль
        Gwid eventGwid = Gwid.createEvent( ISkServer.CLASS_ID, serverId, ISkServer.EVID_LOGIN_FAILED );
        IOptionSetEdit params = new OptionSet();
        params.setStr( ISkServer.EVPID_LOGIN, login );
        params.setStr( ISkServer.EVPID_IP, remoteAddress.asString() );
        SkEvent event = new SkEvent( startTime, eventGwid, params );
        eventBackend.fireAsyncEvents( IS5FrontendRear.NULL, new SkEventList( event ) );
        throw new S5AccessDeniedException( ERR_WRONG_USER );
      }
      // 2021-09-18 mvkd требуется отсекать "старых" клиентов
      // Проверка версии клиента
      IAtomicValue clientVersion = OP_CLIENT_VERSION.getValue( clientOptions );
      if( clientVersion != null && clientVersion.isAssigned() && clientVersion.atomicType() == EAtomicType.STRING ) {
        // Неподдерживаемая версия клиента
        throw new S5AccessDeniedException( String.format( ERR_WRONG_VERSION, clientVersion ) );
      }

      // 2025-03-20 mvk role auto definition
      // 2025-05-12 mvk ---+++
      // Skid role = OP_ROLE.getValue( clientOptions ).asValobj();
      Skid role = clientOptions.getValobj( OP_ROLE.id(), ISkUserServiceHardConstants.SKID_ROLE_GUEST );
      if( ROLE_ID_USKAT_DEFAULT.equals( role.strid() ) ) {
        Gwid linkId = Gwid.createLink( ISkUser.CLASS_ID, LNKID_USER_ROLES );
        ISkidList roles = linksBackend.findLinkFwd( linkId, user.skid() ).rightSkids();
        clientOptions.setValobj( OP_ROLE, roles.first() );
      }

      // Информация о сессии пользователя
      S5SessionData prevSession = sessionManager.findSessionData( sessionID );
      IS5SessionInfo prevSessionInfo = (prevSession != null ? prevSession.info() : null);
      if( prevSessionInfo != null ) {
        // Попытка повторной инициализации сессии пользователя
        logger().error( ERR_DOUBLE_INIT, prevSessionInfo );
      }

      // Формирование локального, удаленного доступа к сессии backend и его расширениям
      IS5BackendSessionControl local = control();
      IS5BackendSession remote = session();
      TsInternalErrorRtException.checkNull( local );
      TsInternalErrorRtException.checkNull( remote );

      Properties prop = new Properties();
      prop.put( Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming" ); //$NON-NLS-1$
      Context context = new InitialContext( prop );
      try {
        // Доступные расширения бекенда предоставляемые сервером
        IStridablesList<IS5BackendAddonCreator> baCreators = backendCoreSingleton.initialConfig().impl().baCreators();
        for( String baId : baCreators.keys() ) {
          if( !aInitData.baIds().hasElem( baId ) ) {
            // Клиент не поддерживает расширение бекенда (классов расширения нет в classpath клиента)
            continue;
          }
          IS5BackendAddonSessionControl baSessionCtrl = baCreators.getByKey( baId ).createSessionControl( context );
          if( baSessionCtrl == null ) {
            // Расширение не работает с сессиями
            continue;
          }
          baSessionCtrls.put( baSessionCtrl.id(), baSessionCtrl );
        }
      }
      finally {
        context.close();
      }
      IS5SessionInfoEdit sessionInfo = new S5SessionInfo( login, clientOptions );
      sessionInfo.setSessionID( sessionID );
      sessionInfo.setRemoteAddress( remoteAddress.asString(), remotePort.asInt() );
      // Установка топологии кластеров доступных клиенту при создании сессии
      sessionInfo.setClusterTopology( aInitData.clusterTopology() );
      // Запущен процесс создания сессии
      logger().info( MSG_CREATE_SESSION_START, sessionInfo );
      // Сессия
      S5SessionData session = new S5SessionData( sessionInfo, local );
      // Создание приемопередатчика сообщений для сессии и его регистрация в backend
      S5SessionMessenger messenger = null;
      try {
        messenger = sessionManager.createMessenger( session );
      }
      catch( TsItemNotFoundRtException e ) {
        // Не найден канал для обратных вызовов
        logger().error( e.getLocalizedMessage() );
        // Завершение сессии
        control().removeAsync();
        // Доводим информацию об ошибке до клиента
        throw sessionError( e );
      }

      // 2021-04-10 mvk
      // Сохранение информации о пользователе в кэше
      // sessionManager.createRemoteSession( selfSession );

      // Результат инициализации сессии
      S5SessionInitResult initResult = new S5SessionInitResult();
      // Метка времени начала инициализации расширений сессии
      long addonsInitStartTime = System.currentTimeMillis();
      // Инициализация сессии данными клиента
      initResult.setAddons( getRemoteReferences( baSessionCtrls ) );
      // Инициализация расширений службы
      for( String addonId : baSessionCtrls.keys() ) {
        IS5BackendAddonSessionControl addonSession = baSessionCtrls.getByKey( addonId );
        try {
          addonSession.init( control(), messenger, aInitData, initResult );
        }
        catch( Throwable e ) {
          // Неожиданная ошибка инициализации расширения
          logger().error( e, ERR_UNEXPECTED_EXTENSION_INIT, addonId, cause( e ) );
        }
      }
      // Получение и обработка широковещательных сообщений бекенда от фронтенда
      messenger.broadcastEventer().addListener( aMessage -> {
        backendCoreSingleton.fireBackendMessage( aMessage );
      } );

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
      control().removeAsync();
      throw e;
    }
    catch( S5SessionApiRtException e ) {
      // Отказ подключения к системе клиента
      logger().error( e, ERR_REJECT_CONNECT, login, remoteAddress, remotePort, cause( e ) );
      // Завершение сессии
      control().removeAsync();
      // Доводим информацию об ошибке до клиента
      throw e;
    }
    catch( Exception e ) {
      // Неожиданная ошибка подключения клиента
      logger().error( e, ERR_UNEXPECTED_CONNECT_FAIL, login, remoteAddress, remotePort, cause( e ) );
      // Завершение сессии
      control().removeAsync();
      // Доводим информацию об ошибке до клиента
      throw sessionError( e );
    }
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public void setClusterTopology( S5ClusterTopology aClusterTopology ) {
    TsNullArgumentRtException.checkNull( aClusterTopology );
    // Изменение топологии кластеров доступных клиенту
    S5SessionData session = sessionManager.findSessionData( sessionID );
    session.info().setClusterTopology( aClusterTopology );
    sessionManager.writeSessionData( session );
    try {
      // Сохранение топологии в ISkSession
      IDtoObject obj = objectsBackend.findObject( sessionID );
      IOptionSetEdit attrs = new OptionSet( obj.attrs() );
      IOptionSetEdit backendSpecificParams =
          new OptionSet( attrs.getValobj( ISkSession.ATRID_BACKEND_SPECIFIC_PARAMS ) );
      OP_SESSION_CLUSTER_TOPOLOGY.setValue( backendSpecificParams, avValobj( aClusterTopology ) );
      attrs.setValobj( ISkSession.ATRID_BACKEND_SPECIFIC_PARAMS, backendSpecificParams );
      IDtoObject dpu = new DtoObject( sessionID, attrs, obj.rivets().map() );
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
    for( String addonId : baSessionCtrls.keys() ) {
      try {
        IS5BackendAddonSessionControl addonSession = baSessionCtrls.getByKey( addonId );
        addonSession.verify();
      }
      catch( NoSuchEJBException e ) {
        // Неожиданное завершение работы расширения сессии
        logger().error( ERR_UNEXPECTED_EXTENSION_CHECK2, addonId, cause( e ) );
        control().removeAsync();
      }
      catch( Throwable e ) {
        // Неожиданная ошибка проверки расширения сессии
        logger().error( ERR_UNEXPECTED_EXTENSION_CHECK, addonId, cause( e ) );
        control().removeAsync();
      }
    }
    // Завершение проверки работы сессии
    logger().info( MSG_SESSION_VERIFY_FINISH, sessionID );
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IS5BackendSessionControl
  //
  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IS5SessionInfo sessionInfo() {
    if( sessionID == null ) {
      logger().error( getClass().getSimpleName() + ".sessionInfo().userSessionID = null" ); //$NON-NLS-1$
      return null;
    }
    S5SessionData session = sessionManager.findSessionData( sessionID );
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
      for( String addonId : baSessionCtrls.keys() ) {
        try {
          IS5BackendAddonSessionControl addonSession = baSessionCtrls.getByKey( addonId );
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
      logger().error( getClass().getSimpleName() + ".close().userSessionID = null" ); //$NON-NLS-1$
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
  public ISkBackendInfo getBackendInfo() {
    // Запрос текущей информации о сервере (backend)
    ISkBackendInfo backendInfo = backendCoreSingleton.getInfo();
    // Информация о сессии
    IS5SessionInfo sessionInfo = sessionInfo();
    // Формирование информации сессии бекенда
    S5BackendInfo retValue = new S5BackendInfo( backendInfo );
    // Роль пользователя
    // 2025-05-12 mvk---+++
    // Skid role = OP_ROLE.getValue( clientOptions ).asValobj();
    Skid role = sessionInfo.clientOptions().getValobj( OP_ROLE.id(), ISkUserServiceHardConstants.SKID_ROLE_GUEST );
    // Информация о зарегистрированном пользователе
    SkLoggedUserInfo loggedUserInfo =
        new SkLoggedUserInfo( new Skid( ISkUser.CLASS_ID, sessionInfo.login() ), role, ESkAuthentificationType.SIMPLE );
    ISkBackendHardConstant.OPDEF_SKBI_LOGGED_USER.setValue( retValue.params(), avValobj( loggedUserInfo ) );
    // Идентификатор текущей сессии пользователя
    IS5ServerHardConstants.OP_BACKEND_SESSION_INFO.setValue( retValue.params(), avValobj( sessionInfo ) );

    return retValue;
  }

  @Override
  public ISkFrontendRear frontend() {
    return sessionManager.getMessenger( sessionID );
  }

  @Override
  public ITsContextRo openArgs() {
    // TODO: нет возможности передать на удаленный бекенд ITsContextRo - из-за наличия в нем references. Что можно
    // сделать?
    throw new TsUnsupportedFeatureRtException();
    // return sessionManager.findSession( sessionID ).info().clientOptions();
  }

  @Override
  public IBaClasses baClasses() {
    return findBackendAddon( IBaClasses.ADDON_ID, IBaClasses.class );
  }

  @Override
  public IBaObjects baObjects() {
    return findBackendAddon( IBaObjects.ADDON_ID, IBaObjects.class );
  }

  @Override
  public IBaLinks baLinks() {
    return findBackendAddon( IBaLinks.ADDON_ID, IBaLinks.class );
  }

  @Override
  public IBaEvents baEvents() {
    return findBackendAddon( IBaEvents.ADDON_ID, IBaEvents.class );
  }

  @Override
  public IBaClobs baClobs() {
    return findBackendAddon( IBaClobs.ADDON_ID, IBaClobs.class );
  }

  @Override
  public IBaRtdata baRtdata() {
    return findBackendAddon( IBaRtdata.ADDON_ID, IBaRtdata.class );
  }

  @Override
  public IBaCommands baCommands() {
    return findBackendAddon( IBaCommands.ADDON_ID, IBaCommands.class );
  }

  @Override
  public IBaQueries baQueries() {
    return findBackendAddon( IBaQueries.ADDON_ID, IBaQueries.class );
  }

  @Override
  public IBaGwidDb baGwidDb() {
    return findBackendAddon( IBaGwidDb.ADDON_ID, IBaGwidDb.class );
  }

  @Override
  public IList<ISkServiceCreator<? extends AbstractSkService>> listBackendServicesCreators() {
    IStridablesList<IS5BackendAddonCreator> baCreators = backendCoreSingleton.initialConfig().impl().baCreators();
    IListEdit<ISkServiceCreator<? extends AbstractSkService>> retValue = new ElemLinkedList<>();
    for( IS5BackendAddonCreator baCreator : baCreators ) {
      retValue.add( baCreator.serviceCreator() );
    }
    return retValue;
  }

  @Override
  public <T> T findBackendAddon( String aAddonId, Class<T> aExpectedType ) {
    TsNullArgumentRtException.checkNulls( aAddonId, aExpectedType );
    try {
      return aExpectedType.cast( baSessionCtrls.findByKey( aAddonId ) );
    }
    catch( Exception ex ) {
      throw new TsIllegalArgumentRtException( ex, ex.getMessage() );
    }
  }

  @Override
  public void sendBackendMessage( GtMessage aMessage ) {
    TsNullArgumentRtException.checkNull( aMessage );
    // Метод вызывается через PAS-канал
    throw new TsUnsupportedFeatureRtException();
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  // @Override
  // @TransactionAttribute( TransactionAttributeType.REQUIRED )
  private void close( Skid aSessionID ) {
    TsIllegalStateRtException.checkNull( aSessionID );
    S5SessionData session = sessionManager.findSessionData( aSessionID );
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
    sessionManager.writeSessionData( session );
    // Асинронное удаление сессии
    session.backend().removeAsync();
  }

  /**
   * Возвращает ссылку на управление (собственный локальный интерфейс).
   *
   * @return {@link IS5BackendSessionControl} собственный локальный интерфейс
   */
  private IS5BackendSessionControl control() {
    if( control == null ) {
      control = sessionContext.getBusinessObject( IS5BackendSessionControl.class );
    }
    return control;
  }

  /**
   * Возвращает ссылку на сессию (собственный удаленный интерфейс).
   *
   * @return {@link IS5BackendSession} собственный удаленный интерфейс
   */
  private IS5BackendSession session() {
    if( selfSession == null ) {
      selfSession = sessionContext.getBusinessObject( IS5BackendSession.class );
    }
    return selfSession;
  }

  /**
   * Возвращает карту удаленного доступа к расширениям backend
   *
   * @param aLocalExtensions {@link IStringMap}&lt;{@link IS5BackendAddonSessionControl}&gt; карта локального доступа к
   *          расширениям. <br>
   *          Ключ: идентификатор расширения;<br>
   *          Значение: локальный доступ к расширению.
   * @return {@link IStringMap}&lt;{@link IS5BackendAddonSession}&gt; карта удаленного доступа к расширениям. <br>
   *         Ключ: идентификатор расширения;<br>
   *         Значение: удаленый доступ к расширению.
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static IStringMap<IS5BackendAddonSession> getRemoteReferences(
      IStringMapEdit<IS5BackendAddonSessionControl> aLocalExtensions ) {
    TsNullArgumentRtException.checkNull( aLocalExtensions );
    IStringMapEdit<IS5BackendAddonSession> retValue = new StringMap<>();
    for( IS5BackendAddonSessionControl local : aLocalExtensions ) {
      retValue.put( local.id(), local.session() );
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
    S5SessionData session = sessionManager.findSessionData( sessionID );
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
