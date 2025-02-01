package org.toxsoft.uskat.s5.server.sessions;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.classes.ISkSession.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.IS5ServerHardConstants.*;
import static org.toxsoft.uskat.s5.server.sessions.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.sessions.IS5SessionInterceptor.*;
import static org.toxsoft.uskat.s5.server.sessions.cluster.S5ClusterCommandCloseCallback.*;
import static org.toxsoft.uskat.s5.server.sessions.cluster.S5ClusterCommandCreateCallback.*;
import static org.toxsoft.uskat.s5.server.sessions.cluster.S5ClusterCommandUpdateSession.*;

import javax.annotation.*;
import javax.ejb.*;

import org.infinispan.*;
import org.infinispan.commons.util.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.strid.idgen.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.coll.synch.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.classes.*;
import org.toxsoft.uskat.classes.impl.*;
import org.toxsoft.uskat.core.api.evserv.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.impl.dto.*;
import org.toxsoft.uskat.s5.client.*;
import org.toxsoft.uskat.s5.common.info.*;
import org.toxsoft.uskat.s5.common.sessions.*;
import org.toxsoft.uskat.s5.server.*;
import org.toxsoft.uskat.s5.server.backend.supports.core.*;
import org.toxsoft.uskat.s5.server.backend.supports.events.*;
import org.toxsoft.uskat.s5.server.backend.supports.links.*;
import org.toxsoft.uskat.s5.server.backend.supports.objects.*;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.*;
import org.toxsoft.uskat.s5.server.cluster.*;
import org.toxsoft.uskat.s5.server.frontend.*;
import org.toxsoft.uskat.s5.server.interceptors.*;
import org.toxsoft.uskat.s5.server.sessions.cluster.*;
import org.toxsoft.uskat.s5.server.sessions.pas.*;
import org.toxsoft.uskat.s5.server.singletons.*;
import org.toxsoft.uskat.s5.server.startup.*;
import org.toxsoft.uskat.s5.server.statistics.*;
import org.toxsoft.uskat.s5.utils.jobs.*;
import org.wildfly.clustering.group.*;

/**
 * Управление сессиями сервера
 *
 * @author mvk
 */
@Startup
@Singleton
@DependsOn( { //
    BACKEND_CORE_SINGLETON, //
    CLUSTER_MANAGER_SINGLETON, //
    BACKEND_SYSDESCR_SINGLETON, //
    BACKEND_OBJECTS_SINGLETON, //
    BACKEND_LINKS_SINGLETON, //
    BACKEND_EVENTS_SINGLETON //
} )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
@ConcurrencyManagement( ConcurrencyManagementType.BEAN )
// @ConcurrencyManagement( ConcurrencyManagementType.CONTAINER )
@Lock( LockType.READ )
@SuppressWarnings( "unused" )
public class S5SessionManager
    extends S5SingletonBase
    implements IS5SessionManager, IS5ServerJob {

  private static final long serialVersionUID = 157157L;

  /**
   * Имя синглетона в контейнере сервера для организации зависимостей (@DependsOn)
   */
  public static final String SESSION_MANAGER_ID = "S5SessionManager"; //$NON-NLS-1$

  /**
   * Таймаут(мсек) разрешения работы doJob
   */
  private static final long DO_JOB_START_TIMEOUT = 5000;

  /**
   * Таймаут(мсек) между выполнением фоновых задач синглетона
   */
  private static final long DO_JOB_TIMEOUT = 100;

  /**
   * Таймаут (мсек) проверки сессий после открытия сессии
   */
  private static final long CHECK_TIMEOUT_AFTER_OPEN = 10 * 1000;

  /**
   * Максимальное количество сессий после которого проводится принудительное удаление из кэша закрытых сессий
   */
  private static final int FORCE_CLOSE_SESSION_COUNT = 1024;

  /**
   * Таймаут очистки закрытых сессий {@link ISkSession} после запуска сервера
   */
  private static final long CLEAN_SESSION_AFTER_START_TIMEOUT = 30 * 60 * 1000;

  /**
   * Начальная, неизменяемая, проектно-зависимая конфигурация реализации бекенда сервера
   */
  @EJB
  private IS5InitialImplementSingleton initialConfig;

  /**
   * Поддержка синглетонов (контейнер)
   */
  @EJB
  private IS5BackendCoreSingleton backendCoreSingleton;

  /**
   * Поддержка классов
   */
  @EJB
  private IS5BackendSysDescrSingleton sysdescrSupport;

  /**
   * Поддержка управления объектами
   */
  @EJB
  private IS5BackendObjectsSingleton objectsSupport;

  /**
   * Поддержка управления связями между объектами
   */
  @EJB
  private IS5BackendLinksSingleton linksSupport;

  /**
   * Поддержка формирования событий системы
   */
  @EJB
  private IS5BackendEventSingleton eventSupport;

  /**
   * Карта открытых сессий.
   * <p>
   * Ключ: {@link Skid} идентификатор сессии {@link ISkSession};<br>
   * Значение: {@link IS5SessionInfo} сессия пользователя
   */
  @Resource( lookup = INFINISPAN_CACHE_OPEN_SESSIONS )
  private Cache<Skid, S5SessionData> remoteOpenSessionCache;

  /**
   * Карта закрытых сессий.
   * <p>
   * Ключ: {@link Skid} идентификатор сессии {@link ISkSession};<br>
   * Значение: {@link IS5SessionInfo} сессия пользователя
   */
  @Resource( lookup = INFINISPAN_CACHE_CLOSED_SESSIONS )
  private Cache<Skid, S5SessionData> closedSessionCache;

  /**
   * Карта открытых локальных сессий.
   * <p>
   * Ключ: {@link Skid} идентификатор сессии {@link ISkSession};<br>
   * Значение: {@link S5LocalSession} локальная сессия пользователя
   */
  private IMapEdit<Skid, S5LocalSession> localOpenSessionCache = new SynchronizedMap<>( new ElemMap<>() );

  /**
   * Менеджер кластера
   */
  @EJB
  private IS5ClusterManager clusterManager;

  /**
   * Сервер каналов используемых передатчиками callback-сообщений
   */
  private S5SessionCallbackServer callbackServer;

  /**
   * Карта писателей через которые пользователям передаются callback-сообщения
   * <p>
   * Ключ: идентификатор сессии пользователя, {@link ISkSession};<br>
   * Значение: отправитель
   */
  private final SynchronizedMap<Skid, S5SessionMessenger> messengerBySessions =
      new SynchronizedMap<>( new ElemMap<>() );

  /**
   * Карта счетчиков данных статистики для открытых сессий
   * <p>
   * Раз в период статистики значения счетчиков суммируются с данными статистики соответствующей сессии и обнуляются
   * <p>
   * Ключ: Идентификатор сессии {@link ISkSession#skid()};<br>
   * Значение: {@link S5Statistic} данные статистики
   */
  private final IMapEdit<Skid, S5Statistic> statisticCountersBySessions = new SynchronizedMap<>( new ElemMap<>() );

  /**
   * Идентификатор сервера в рамках которого работает менеджер сессий
   */
  private String serverId;

  /**
   * Признак того, что после запуска менеджера сессий была выполнена пауза перед запуском doJob
   */
  private boolean doJobInited;

  /**
   * Метка времени последнего выполнения doJob
   */
  private long doJobTimestamp;

  /**
   * Метка времени последней проверки закрытых сессий
   */
  private long cleanSessionsTimestamp;

  /**
   * Поддержка интерсепторов операций проводимых над сессиями
   */
  private final S5InterceptorSupport<IS5SessionInterceptor> interceptors = new S5InterceptorSupport<>();

  /**
   * Интерспетор операций проводимых над объектами (контроль учетных записей пользователей)
   */
  private S5ObjectsInterceptor objectsInterceptor;

  /**
   * Признак того, что после старта был выполнен цикл удаления несуществующих сессий
   */
  private boolean wasCleanSessionsAfterStart;

  /**
   * Генератор идентификаторов сессий, объектов {@link ISkSession}
   */
  private static IStridGenerator stridGenerator = new UuidStridGenerator( UuidStridGenerator.createState( "local" ) ); //$NON-NLS-1$

  /**
   * Таймаут образования сессий. <0: без таймаута
   */
  private static final long CREATE_SESSION_TIMEOUT = 100;

  /**
   * Метка времени образования последнего соединения
   */
  private volatile long lastCreateSessionTimeout = System.currentTimeMillis();

  /**
   * Конструктор
   */
  public S5SessionManager() {
    super( SESSION_MANAGER_ID, STR_D_SESSION_MANAGER );
    // Настройка doJob-задачи
    long currTime = System.currentTimeMillis();
    doJobTimestamp = currTime;
    long checkOpenSessionsTimestamp = currTime;
    cleanSessionsTimestamp = currTime;
  }

  // ------------------------------------------------------------------------------------
  // Определение шаблонных методов S5SingletonBase
  //
  @Override
  protected void doInit() {
    // Информация о бекенде сервера (это необходимо сделать до вызова setSessionManager(...) чтобы не было рекурсии
    ISkBackendInfo info = backendCoreSingleton.getInfo();
    // Бизнес-API
    IS5SessionManager sessionManager = sessionContext().getBusinessObject( IS5SessionManager.class );
    // Регистрация менеджера сессий в ядре бекенда
    backendCoreSingleton.setSessionManager( sessionManager );
    // Регистрация обработчиков уведомлений
    clusterManager.addCommandHandler( CREATE_CALLBACK_METHOD, new S5ClusterCommandCreateCallback( sessionManager ) );
    clusterManager.addCommandHandler( CLOSE_CALLBACK_METHOD, new S5ClusterCommandCloseCallback( sessionManager ) );
    clusterManager.addCommandHandler( UDATE_SESSION_METHOD, new S5ClusterCommandUpdateSession( sessionManager ) );
    // Создание PAS
    callbackServer =
        new S5SessionCallbackServer( initialConfig.impl(), sessionManager, clusterManager, doJobExecutor() );

    // Регистрация на события кластера
    clusterManager.addClusterListener( new IS5ClusterListener() {

      @SuppressWarnings( "synthetic-access" )
      @Override
      public void membershipChanged( Membership aPrevMembership, Membership aNewMembership, boolean aMerged ) {
        // Проверка сессий на предмет того, что все они имеют писателя обратных вызовов
        IS5SessionManager sm = sessionContext().getBusinessObject( IS5SessionManager.class );
        // Список открытых сессий
        IList<S5SessionData> openSessions = openSessions();
        // Журнал
        logger().info( "membershipChanged(...): openSessions size = %d", Integer.valueOf( openSessions.size() ) ); //$NON-NLS-1$
        // Попытка формирования передатчиков для уже открытых сессий
        for( S5SessionData session : openSessions ) {
          logger().info( "membershipChanged(...): createCallbackWriter for = %s", session ); //$NON-NLS-1$
          tryCreateMessenger( session );
        }
      }
    } );
    // Обновление системного описания ядра
    S5ClassUtils.updateCoreSysdescr( info, sysdescrSupport, objectsSupport, linksSupport );
    // Контроль учетных записей пользователей
    ISkClassInfo userClassInfo = sysdescrSupport.getReader().getClassInfo( ISkUser.CLASS_ID );
    objectsInterceptor = new S5ObjectsInterceptor( sessionManager, objectsSupport, linksSupport, userClassInfo );
    objectsSupport.addObjectsInterceptor( objectsInterceptor, 1024 );
    // Запуск фоновой задачи
    addOwnDoJob( DO_JOB_TIMEOUT );
    // Идентификатор сервера (кэширование)
    serverId = ((Skid)OP_SERVER_ID.getValue( info.params() ).asValobj()).strid();
  }

  @Override
  protected void doClose() {
    // Дерегистрация перехвата изменений учетных записей пользователей
    objectsSupport.removeObjectsInterceptor( objectsInterceptor );
    // Завершение работы PAS-сервера
    callbackServer.close();
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5SessionManager
  //
  @Override
  @SuppressWarnings( "nls" )
  public Skid generateSessionID( String aModuleName, String aModuleNode ) {
    TsNullArgumentRtException.checkNulls( aModuleName, aModuleNode );
    synchronized (stridGenerator) {
      return new Skid( CLASS_ID, aModuleName + "." + aModuleNode + "." + stridGenerator.nextId() );
    }
  }

  @Override
  public IS5SessionsInfos getInfos() {
    IListEdit<IS5SessionInfo> openInfos = new ElemArrayList<>();
    IListEdit<IS5SessionInfo> closeInfos = new ElemArrayList<>();
    for( S5SessionData session : openSessions() ) {
      openInfos.add( session.info() );
    }
    // TODO: 2022-09-11 mvkd
    // for( S5SessionData session : closedSessions() ) {
    // closeInfos.add( session.info() );
    // }
    return new S5SessionsInfos( openInfos, closeInfos );
  }

  @Override
  public int openSessionCount() {
    return messengerBySessions.size();
  }

  @Override
  public IList<S5SessionData> openSessions() {
    IListBasicEdit<S5SessionData> openSessions = new SortedElemLinkedBundleList<>();
    try( CloseableIterator<S5SessionData> iterator = remoteOpenSessionCache.values().iterator() ) {
      while( iterator.hasNext() ) {
        openSessions.add( iterator.next() );
      }
    }
    return new ElemArrayList<>( openSessions );
  }

  @Override
  public IList<S5SessionData> closedSessions() {
    // Список закрытых сессий
    IListBasicEdit<S5SessionData> closedSessions = new SortedElemLinkedBundleList<>();
    try( CloseableIterator<S5SessionData> iterator = closedSessionCache.values().iterator() ) {
      while( iterator.hasNext() ) {
        closedSessions.add( iterator.next() );
      }
    }
    return new ElemArrayList<>( closedSessions );
  }

  @Override
  public S5SessionData findSessionData( Skid aSessionID ) {
    TsNullArgumentRtException.checkNull( aSessionID );
    return remoteOpenSessionCache.get( aSessionID );
  }

  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public boolean createRemoteSession( S5SessionData aSession ) {
    TsNullArgumentRtException.checkNull( aSession );
    long currTime = System.currentTimeMillis();

    // TODO: 2023-11-21 mvk: ts4, проверить необходимость этого
    // Контроль за частотой подключения к серверу
    // if( currTime - lastCreateSessionTimeout < CREATE_SESSION_TIMEOUT ) {
    // // Доступ запрещен - сервер перегружен
    // throw new S5AccessDeniedException( ERR_SERVER_OVERLOAD, Integer.valueOf( remoteOpenSessionCache.size() ) );
    // }

    lastCreateSessionTimeout = currTime;

    startCacheBatch( remoteOpenSessionCache );
    try {
      IS5SessionInfo info = aSession.info();
      Skid sessionID = info.sessionID();
      callBeforeCreateSession( interceptors, sessionID );
      IS5FrontendRear frontend = messengerBySessions.getByKey( sessionID );
      String login = info.login();
      long createTime = info.openTime();
      IOptionSetEdit backendSpecificParams = new OptionSet();
      OP_SESSION_CLUSTER_TOPOLOGY.setValue( backendSpecificParams, avValobj( info.clusterTopology() ) );
      IOptionSetEdit connectionCreationParams = new OptionSet();
      OP_SESSION_ADDRESS.setValue( connectionCreationParams, avStr( info.remoteAddress() ) );
      OP_SESSION_PORT.setValue( connectionCreationParams, avInt( info.remotePort() ) );
      // Создание или восстановление сессии
      boolean created =
          createSkSession( sessionID, login, createTime, backendSpecificParams, connectionCreationParams );
      S5SessionData prevSession = remoteOpenSessionCache.put( info.sessionID(), aSession );
      callAfterCreateSession( interceptors, sessionID, logger() );

      // Формирование события
      Gwid eventGwid = Gwid.createEvent( ISkServer.CLASS_ID, serverId,
          created ? ISkServer.EVID_SESSION_CREATED : ISkServer.EVID_SESSION_RESTORED );
      IOptionSetEdit params = new OptionSet();
      params.setStr( ISkServer.EVPID_LOGIN, aSession.info().login() );
      params.setStr( ISkServer.EVPID_IP, aSession.info().remoteAddress() );
      params.setValobj( ISkServer.EVPID_SESSION_ID, sessionID );
      SkEvent event = new SkEvent( createTime, eventGwid, params );
      eventSupport.fireEvents( frontend, new TimedList<>( event ) );

      return (prevSession == null);
    }
    finally {
      endCacheBatch( remoteOpenSessionCache, true );
    }
  }

  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public boolean createLocalSession( S5LocalSession aSession ) {
    TsNullArgumentRtException.checkNull( aSession );
    Skid sessionID = aSession.sessionID();
    IS5FrontendRear frontend = aSession.frontend();
    String login = ISkUserServiceHardConstants.USER_ID_ROOT;
    callBeforeCreateSession( interceptors, sessionID );
    long createTime = System.currentTimeMillis();
    // TODO: ???
    IOptionSet backendSpecificParams = IOptionSet.NULL;
    IOptionSetEdit connectionCreationParams = new OptionSet();
    IS5ConnectionParams.OP_LOCAL_MODULE.setValue( connectionCreationParams, avStr( aSession.module() ) );
    IS5ConnectionParams.OP_LOCAL_NODE.setValue( connectionCreationParams, avStr( aSession.node() ) );
    createSkSession( sessionID, login, createTime, backendSpecificParams, connectionCreationParams );
    S5LocalSession prevSession = localOpenSessionCache.put( sessionID, aSession );
    callAfterCreateSession( interceptors, sessionID, logger() );
    return (prevSession == null);
  }

  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public void writeSessionData( S5SessionData aSession ) {
    TsNullArgumentRtException.checkNull( aSession );
    // Идентификатор сессии
    Skid sessionID = aSession.info().sessionID();
    // Обновление сессии в кэше
    startCacheBatch( remoteOpenSessionCache );
    try {
      // TODO: 2020-04-06 mvk проблема в том, что иногда проводится попытка чтения backendCoreSingleton которого уже нет
      // S5SessionData prevSession = remoteOpenSessionCache.replace( aSession.info().id(), aSession );
      remoteOpenSessionCache.put( sessionID, aSession );
    }
    finally {
      endCacheBatch( remoteOpenSessionCache, true );
    }
    // Команда узлам кластера на обновление сессии. aRemoteOnly = true, aPrimaryOnly = fasle
    clusterManager.sendAsyncCommand( updateSessionCommand( sessionID ), true, false );
  }

  @TransactionAttribute( TransactionAttributeType.REQUIRES_NEW )
  @Override
  public void closeRemoteSession( Skid aSessionID ) {
    TsNullArgumentRtException.checkNull( aSessionID );
    try {
      startCacheBatch( remoteOpenSessionCache );
      try {
        S5SessionData session = remoteOpenSessionCache.get( aSessionID );
        if( session == null ) {
          return;
        }
        session.info().close();
        long endTime = session.info().closeTime();
        boolean loss = !session.info().closeByRemote();
        Skid sessionID = session.info().sessionID();

        callBeforeCloseSession( interceptors, sessionID, logger() );

        closeMessenger( aSessionID );

        closeSkSession( sessionID, session, endTime, loss );

        remoteOpenSessionCache.remove( aSessionID );
        // Сессия найдена и закрыта (перемещается в кэш закрытых сессий)
        closedSessionCache.put( aSessionID, session );

        // Формирование события
        String evId = (loss ? ISkServer.EVID_SESSION_BREAKED : ISkServer.EVID_SESSION_CLOSED);
        Gwid eventGwid = Gwid.createEvent( ISkServer.CLASS_ID, serverId, evId );
        IOptionSetEdit params = new OptionSet();
        params.setStr( ISkServer.EVPID_LOGIN, session.info().login() );
        params.setStr( ISkServer.EVPID_IP, session.info().remoteAddress() );
        params.setValobj( ISkServer.EVPID_SESSION_ID, sessionID );
        SkEvent event = new SkEvent( endTime, eventGwid, params );
        eventSupport.fireEvents( IS5FrontendRear.NULL, new TimedList<>( event ) );

        callAfterCloseSession( interceptors, sessionID, logger() );
      }
      finally {
        endCacheBatch( remoteOpenSessionCache, true );
      }
    }
    catch( RuntimeException e ) {
      // Ошибка завершения remote-сессии
      logger().error( e, ERR_CLOSE_REMOTE_SESSION, cause( e ) );
    }
  }

  @TransactionAttribute( TransactionAttributeType.REQUIRES_NEW )
  @Override
  public void closeLocalSession( Skid aSessionID ) {
    TsNullArgumentRtException.checkNull( aSessionID );
    try {
      S5LocalSession session = localOpenSessionCache.findByKey( aSessionID );
      if( session == null ) {
        return;
      }
      Skid sessionID = session.sessionID();
      callBeforeCloseSession( interceptors, sessionID, logger() );
      long endTime = System.currentTimeMillis();
      boolean loss = false;
      closeSkSession( sessionID, null, endTime, loss );
      localOpenSessionCache.removeByKey( aSessionID );
      // Сессия найдена и закрыта (перемещается в кэш закрытых сессий)
      callAfterCloseSession( interceptors, sessionID, logger() );
    }
    catch( RuntimeException e ) {
      // Ошибка завершения local-сессии
      logger().error( e, ERR_CLOSE_LOCAL_SESSION, cause( e ) );
    }
  }

  @Override
  public S5SessionMessenger findMessenger( Skid aSessionID ) {
    TsNullArgumentRtException.checkNull( aSessionID );
    return messengerBySessions.findByKey( aSessionID );
  }

  @Override
  public S5SessionMessenger getMessenger( Skid aSessionID ) {
    TsNullArgumentRtException.checkNull( aSessionID );
    S5SessionMessenger retValue = messengerBySessions.findByKey( aSessionID );
    if( retValue != null ) {
      return retValue;
    }
    S5SessionData session = findSessionData( aSessionID );
    if( session == null ) {
      // Сессия не найдена
      throw new TsIllegalArgumentRtException( ERR_SESSION_NOT_FOUND, "callbackWriter(...)", aSessionID ); //$NON-NLS-1$
    }
    retValue = tryCreateMessenger( session );
    if( retValue == null ) {
      // Не найден канал для обратных вызовов
      throw new TsIllegalArgumentRtException( ERR_CALLBACK_NOT_FOUND, aSessionID );
    }
    return retValue;
  }

  @Override
  public S5SessionMessenger createMessenger( S5SessionData aSession ) {
    TsNullArgumentRtException.checkNull( aSession );
    // Описание сессии
    IS5SessionInfo sessionInfo = aSession.info();
    // Идентификатор сессии
    Skid sessionID = sessionInfo.sessionID();
    // Если писатель уже создан, то он удаляется
    boolean wasSessionWriter = (closeMessenger( sessionID ) != IS5FrontendRear.NULL);
    if( wasSessionWriter ) {
      logger().error( "createCallbackWriter(...): the session already had callback writer. sessionID = %s", sessionID ); //$NON-NLS-1$
    }
    // Попытка создания писателя
    S5SessionMessenger messenger = tryCreateMessenger( aSession );
    // Анализ результата
    if( messenger == null ) {
      // Messenger не создан
      throw new TsIllegalArgumentRtException( ERR_CALLBACK_NOT_FOUND, aSession );
    }
    return messenger;
  }

  @Override
  public S5SessionMessenger tryCreateMessenger( S5SessionData aSession ) {
    TsNullArgumentRtException.checkNull( aSession );
    // Описание сессии
    IS5SessionInfo sessionInfo = aSession.info();
    // Идентификатор сессии
    Skid sessionID = sessionInfo.sessionID();
    // Писатель обратных вызовов
    S5SessionMessenger messenger = messengerBySessions.findByKey( sessionID );
    if( messenger != null ) {
      // Обновление данных передатчика обратных вызов сессии
      logger().info( MSG_UPDATE_SESSION_CALLBACK_WRITER, sessionID );
      messenger.updateSessionData( aSession );
      return messenger;
    }
    // Адрес клиента
    String remoteAddr = sessionInfo.remoteAddress();
    int remotePort = sessionInfo.remotePort();
    // Создание передатчика обратных вызовов сессии
    logger().info( MSG_CREATE_SESSION_CALLBACK, sessionID, remoteAddr, Integer.valueOf( remotePort ) );
    // Поиск канала для создания писателей обратных вызовов
    S5SessionCallbackChannel callbackChannel = callbackServer.findSessionChannel( sessionID, remoteAddr, remotePort );
    if( callbackChannel == null ) {
      // Не найден канал для обратных вызовов
      logger().warning( ERR_CALLBACK_NOT_FOUND, sessionID );
      return null;
    }
    // Создание писателя обратных вызовов
    messenger = new S5SessionMessenger( backendCoreSingleton, aSession, callbackChannel );
    // Регистрация писателя обратных вызовов
    S5SessionMessenger oldCallbackWriter = messengerBySessions.put( sessionID, messenger );
    if( oldCallbackWriter != null && !messenger.equals( oldCallbackWriter ) ) {
      // Для сессии уже существовал писатель обратных вызовов
      logger().error( ERR_CALLBACK_WRITER_ALREADY_EXIST, aSession );
      oldCallbackWriter.close();
    }
    // 2021-02-13 mvk перемещено из конструктора S5SessionMessenger
    // Регистрация писателя обратных вызовов как frontend бекенда сервера
    backendCoreSingleton.attachFrontend( messenger );
    // Создание передатчика обратных вызовов сессии завершено
    logger().info( MSG_CREATE_SESSION_CALLBACK_FINISH, sessionID );
    return messenger;
  }

  @Override
  public IS5FrontendRear closeMessenger( Skid aSessionID ) {
    TsNullArgumentRtException.checkNull( aSessionID );
    S5SessionMessenger callbackWriter = messengerBySessions.removeByKey( aSessionID );
    if( callbackWriter != null ) {
      callbackWriter.close();
    }
    return (callbackWriter != null ? callbackWriter : IS5FrontendRear.NULL);
  }

  @Override
  public IS5StatisticCounter findStatisticCounter( Skid aSessionID ) {
    TsNullArgumentRtException.checkNull( aSessionID );
    S5Statistic retValue = statisticCountersBySessions.findByKey( aSessionID );

    // 2021-04-09 mvk
    // Статистика сессии
    if( retValue == null ) {
      // Если сессия открыта, то создаем для нее статистику
      // IDpuObject session = objectsSupport.findObject( aSessionID );
      // if( session != null && session.attrs().getValue( ATRID_ENDTIME ).isAssigned() == false ) {
      retValue = new S5Statistic( STAT_SESSION_PARAMS );
      statisticCountersBySessions.put( aSessionID, retValue );
      // }
    }
    return retValue;
  }

  @Override
  public void addSessionInterceptor( IS5SessionInterceptor aInterceptor, int aPriority ) {
    TsNullArgumentRtException.checkNull( aInterceptor );
    interceptors.add( aInterceptor, aPriority );
  }

  @Override
  public void removeSessionInterceptor( IS5SessionInterceptor aInterceptor ) {
    TsNullArgumentRtException.checkNull( aInterceptor );
    interceptors.remove( aInterceptor );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5ServerJob
  //
  @Override
  public void doJob() {
    super.doJob();
    // Текущее время
    long currTime = System.currentTimeMillis();
    // Обработка doJob открытых писателей обратных вызовов
    IList<S5SessionMessenger> callbacks = messengerBySessions.values().copyTo( new ElemArrayList<>() );
    for( S5SessionMessenger callback : callbacks ) {
      callback.doJob();
    }
    // if( currTime - doJobTimestamp < SECOND.interval() ) {
    if( currTime - doJobTimestamp < 1000 ) {
      // Фоновая работа проводится не раньше чем раз в секунду
      return;
    }
    doJobTimestamp = currTime;
    ILogger logger = logger();
    StringBuilder sbDoJobLog = null;
    if( logger.isSeverityOn( ELogSeverity.DEBUG ) ) {
      // Вывод в журнал открытых сессий
      sbDoJobLog = new StringBuilder();
    }
    // Список открытых удаленных сессий
    IListEdit<S5SessionData> remoteOpenSessions = new ElemLinkedList<>();
    // Количество закрытых сессий
    int closedSessionsCount;
    try( CloseableIterator<S5SessionData> iterator = remoteOpenSessionCache.values().iterator() ) {
      while( iterator.hasNext() ) {
        S5SessionData session = iterator.next();
        remoteOpenSessions.add( session );
        if( sbDoJobLog != null ) {
          sbDoJobLog.append( session.toString() + '\n' );
        }
      }
    }
    closedSessionsCount = closedSessionCache.size();
    if( sbDoJobLog != null ) {
      // Выполнение doJob()
      Node node = clusterManager.group().getLocalMember();
      Node coordinator = clusterManager.group().getMembership().getCoordinator();
      Long duration = Long.valueOf( System.currentTimeMillis() - currTime );
      Integer oc = Integer.valueOf( remoteOpenSessions.size() );
      Integer cc = Integer.valueOf( closedSessionsCount );
      logger.debug( MSG_DOJOB_RUN, duration, node, coordinator, oc, cc, sbDoJobLog );
    }
    // 2020-03-21, mvk, писателя обратных вызовов может не быть - узел работает в другой сети и у клиента нет
    // к нему доступа (пример: Тбилиси, локальные сервера станции, клиенты программа НУ). С другой стороны callback-ов
    // не может быть больше чем сессий
    // if( openSessions.size() != callbacks.size() ) {
    if( remoteOpenSessions.size() < callbacks.size() ) {
      // Количество сессий в кэше не соответствует количеству писателей обратных вызовов
      Integer oc = Integer.valueOf( remoteOpenSessions.size() );
      Integer cc = Integer.valueOf( callbacks.size() );
      logger.error( ERR_WRONG_SESSIONS_CALLBACKS, oc, cc );
      // Проверка работоспособности всех открытых сессий.
      verifySessions( remoteOpenSessions );
      // Проверка существования сессий для всех callbacks
      verifyCallbacks();
    }
    // Очистка старых сессий
    cleanSessions( remoteOpenSessions );
    // Обработка статистики
    for( S5SessionData session : remoteOpenSessions ) {
      Skid sessionID = session.info().sessionID();
      // Признак необходимости обновить статистику сессии
      boolean needUpdate = false;
      // Счетчик сессии
      S5Statistic counter = statisticCountersBySessions.findByKey( sessionID );
      if( counter != null ) {
        // Значения накопленные с момента прошлого вызова doJob
        IOptionSet params = counter.params( EStatisticInterval.ALL );
        // Статистика сессии
        IS5StatisticCounter statistic = (IS5StatisticCounter)session.info().statistics();
        // Перенос значений в статистику сессии
        for( String paramName : params.keys() ) {
          IAtomicValue paramValue = params.getValue( paramName );
          needUpdate |= statistic.onEvent( paramName, paramValue );
        }
        // Суммирование отдельных значений в статистику узла бекенда
        IS5StatisticCounter backendStatistic = backendCoreSingleton.statisticCounter();
        backendStatistic.onEvent( STAT_BACKEND_NODE_PAS_RECEIVED, params.getByKey( STAT_SESSION_RECEVIED.id() ) );
        backendStatistic.onEvent( STAT_BACKEND_NODE_PAS_SEND, params.getByKey( STAT_SESSION_SENDED.id() ) );
        // Сброс счетчика
        counter.reset();

        // if( session.info().remoteAddress().equals( "10.150.0.44" ) ) {
        // System.err.println( "10.150.0.44: sessionID = " + sessionID + ", statistic = " + statistic );
        // }

        // Обновление параметров статистики сессии
        needUpdate |= statistic.update();
        if( needUpdate ) {
          // Обновление сессии в кэше
          // 2021-04-15 обновление без передачи команды кластеры "обновить сессию"
          // updateRemoteSession( session );
          remoteOpenSessionCache.put( sessionID, session );

        }
      }
    }

    // Если запустился первый узел кластера сервера, то удаление всех старых сессий
    if( !wasCleanSessionsAfterStart && //
        currTime - launchTimestamp() > CLEAN_SESSION_AFTER_START_TIMEOUT && //
        clusterManager.group().getMembership().getMembers().size() <= 1 ) {
      wasCleanSessionsAfterStart = true;
      // Проверка существования старых сессий
      logger().warning( ERR_REMOVE_OLD_SESSIONS_CHECK );
      // Удаление сессий прошлой работы сервера
      IList<IDtoObject> sessions = objectsSupport.readObjects( new StringArrayList( ISkSession.CLASS_ID ) );
      SkidList sessionIds = new SkidList();
      for( IDtoObject session : sessions ) {
        Skid sessionID = session.skid();
        boolean needRemove = (localOpenSessionCache.findByKey( sessionID ) == null);
        for( S5SessionData openSession : remoteOpenSessions ) {
          if( openSession.info().sessionID().equals( sessionID ) ) {
            needRemove = false;
            break;
          }
        }
        if( needRemove ) {
          sessionIds.add( session.skid() );
        }
      }
      // Завершение удаление устаревших сессий пользователя (ISkSession)
      logger().warning( ERR_REMOVE_OLD_SESSIONS_START, Integer.valueOf( sessionIds.size() ) );
      if( sessionIds.size() > 0 ) {
        // aInterceptable = true (нужен перехват S5BackendLinkSingleton, иначе не будут удалены связи)
        objectsSupport.writeObjects( IS5FrontendRear.NULL, sessionIds, IList.EMPTY, true );
      }
      // Завершение удаление устаревших сессий пользователя (ISkSession)
      logger().warning( ERR_REMOVE_OLD_SESSIONS_FINISH, Integer.valueOf( sessionIds.size() ) );
    }
  }

  @Override
  public boolean completed() {
    return isClosed();
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Начать пакетное изменение значений кэша
   *
   * @param aCache {@link Cache} кэш значений
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static void startCacheBatch( Cache<?, ?> aCache ) {
    TsNullArgumentRtException.checkNull( aCache );
    // aCache.startBatch();
  }

  /**
   * Завершить пакетное изменение значений кэша
   *
   * @param aCache {@link Cache} кэш значений
   * @param aSuccess boolean <b>true</b> успешное завершение;<b>false</b> откат изменений.
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static void endCacheBatch( Cache<?, ?> aCache, boolean aSuccess ) {
    TsNullArgumentRtException.checkNull( aCache );
    // aCache.endBatch( aSuccess );
  }

  /**
   * Проверка работоспособности сессии
   *
   * @param aSessions {@link IList}&lt;{@link S5SessionData}&gt; список открытых сессий
   * @throws TsNullArgumentRtException аргумент = null
   */
  private void verifySessions( IList<S5SessionData> aSessions ) {
    TsNullArgumentRtException.checkNull( aSessions );
    long currTime = System.currentTimeMillis();
    for( S5SessionData session : aSessions ) {
      Skid sessionID = session.info().sessionID();
      try {
        // Признак отработки завершения таймаута после открытия сессии (сессия недавно создана и, возможно, еще не
        // размещена в кэше remoteOpenSessionCache)
        boolean afterOpenTimeout = (currTime - session.info().openTime() < CHECK_TIMEOUT_AFTER_OPEN);
        if( afterOpenTimeout ) {
          // Сессия недавно создана не мешаем ее инициализации
          continue;
        }
        // Проверка сессии
        session.backend().verify();

        // 2020-03-21, mvk, писателя обратных вызовов может не быть - узел работает в другой сети и у клиента нет
        // к нему доступа (пример: Тбилиси, локальные сервера станции, клиенты программа НУ).
        // // Проверка существования писателя сессии
        // tryLockWrite( lock );
        // try {
        // if( messengerBySessions.hasKey( sessionID ) == false ) {
        // // Проверка сессии. Создание писателя обратных вызовов
        // logger().error( ERR_CHECK_SESSION_CREATE_CALLBACK, sessionID );
        // // Создание писателя
        // createCallbackWriter( session );
        // }
        // }
        // finally {
        // unlockWrite( lock );
        // }
      }
      catch( Throwable e ) {
        // 2020-03-21, mvk, клиент может не иметь доступа к первичному узлу кластера - узел работает в другой сети и у
        // клиента нет к нему доступа (пример: Тбилиси, локальные сервера станции, клиенты программа НУ).
        // Сессия проверяется только первичным узлом в кластере
        // if( clusterManager.isPrimary() == false ) {
        // // Сессия неработоспособна. Только первичный узел может удалить сессию
        // logger().error( ERR_CHECK_SESSION_NOT_PRIMARY, sessionID );
        // continue;
        // }
        // Сессия неработоспособна. Сессия будет удалена
        logger().error( ERR_CHECK_SESSION_REMOVE, sessionID );
        closeRemoteSession( sessionID );
      }
    }
  }

  /**
   * Проверка существования сессий для каждого писателя обратных вызовов
   */
  private void verifyCallbacks() {
    // Текущее время
    long currTime = System.currentTimeMillis();
    // Карта писателей обратных вызовов
    IMap<Skid, S5SessionMessenger> callbacks = messengerBySessions.copyTo( new ElemMap<>() );
    // Проверка писателей обратных вызовов
    for( Skid sessionID : callbacks.keys() ) {
      S5SessionMessenger callback = callbacks.getByKey( sessionID );
      // Признак отработки завершения таймаута после открытия сессии (сессия недавно создана и, возможно, еще не
      // размещена в кэше remoteOpenSessionCache)
      boolean afterOpenTimeout = (currTime - callback.sessionData().info().openTime() < CHECK_TIMEOUT_AFTER_OPEN);
      if( !afterOpenTimeout && remoteOpenSessionCache.get( sessionID ) == null ) {
        // Сессия не найдена в кэше, проверка ее существования и размещения ее в кэше
        logger().error( ERR_CHECK_CALLBACK, sessionID );
        // Проверяемая сессия
        S5SessionData session = callback.sessionData();
        try {
          // Проверка существования сессии
          session.backend().verify();
        }
        catch( NoSuchEJBException e ) {
          // Невалидная сессия. Callback будет закрыт
          logger().error( ERR_CHECK_CALLBACK_SESSION_INVALID, sessionID );
          closeMessenger( sessionID );
          continue;
        }
        catch( Throwable e ) {
          // Неожиданная ошибка доступа к сессии
          logger().error( e );
          closeMessenger( sessionID );
          continue;
        }
        // Размещение сессии в кэше
        remoteOpenSessionCache.put( sessionID, session );
        logger().error( ERR_CHECK_CALLBACK_CACHED, sessionID );
      }
    }
  }

  /**
   * Очистка старых или потерянных сесссий
   *
   * @param aOpenSessions {@link IList}&lt;{@link S5SessionData}&gt; список открытых сессий
   * @throws TsNullArgumentRtException аргумент = null
   */
  private void cleanSessions( IList<S5SessionData> aOpenSessions ) {
    TsNullArgumentRtException.checkNull( aOpenSessions );
    // Текущее время
    long currTime = System.currentTimeMillis();
    // Количество дней хранения завершенных сессий
    long closeSessionKeepTimeout =
        OP_BACKEND_SESSION_KEEP_DAYS.getValue( initialConfig.impl().params() ).asInt() * 24 * 60 * 60 * 1000;
    // Признак необходимости провести проверку сессий (ДВОЙНОЙ таймаут вывода SFSB в пул контейнера)
    boolean needClean = (currTime - cleanSessionsTimestamp > 2 * STATEFULL_TIMEOUT);
    cleanSessionsTimestamp = (needClean ? currTime : cleanSessionsTimestamp);
    if( !needClean ) {
      return;
    }
    // Проверка открытых сессий
    verifySessions( aOpenSessions );
    // Список закрытых сессий
    IListBasicEdit<S5SessionData> closedSessions = new SortedElemLinkedBundleList<>();
    try( CloseableIterator<S5SessionData> iterator = closedSessionCache.values().iterator() ) {
      while( iterator.hasNext() ) {
        closedSessions.add( iterator.next() );
      }
    }
    // Количество закрытых сессий подлежащих обязательному удалению
    int forceRemoveCount = closedSessions.size() - FORCE_CLOSE_SESSION_COUNT;
    // Список удаляемых объектов сессий
    SkidList removedSessionIds = new SkidList();
    // Удаление сессий
    for( S5SessionData session : closedSessions ) {
      Skid sessionID = session.info().sessionID();
      if( forceRemoveCount-- > 0 ) {
        closedSessionCache.remove( sessionID );
        removedSessionIds.add( session.info().sessionID() );
        continue;
      }
      if( currTime - session.info().closeTime() < closeSessionKeepTimeout ) {
        // Список отсортирован, далее идут сессии с более поздним временем завершения
        break;
      }
    }
    if( removedSessionIds.size() > 0 ) {
      objectsSupport.writeObjects( IS5FrontendRear.NULL, removedSessionIds, IList.EMPTY, true );
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Создание нового объекта {@link ISkSession}
   *
   * @param aSessionID {@link Skid} идентификатор сессии
   * @param aLogin String логин пользователя подключенного к системе
   * @param aCreationTime long метка времени создания сессии
   * @param aBackendSpecificParams {@link IOptionSet} параметры бекенда
   * @param aConnectionCreationParams {@link IOptionSet} параметры создания соединения
   * @return boolean <b>true</b> создан объект сессии; <b>false</b> объект сессии уже существует
   * @throws TsNullArgumentRtException аргумент = null
   */
  private boolean createSkSession( Skid aSessionID, String aLogin, long aCreationTime,
      IOptionSet aBackendSpecificParams, IOptionSet aConnectionCreationParams ) {
    TsNullArgumentRtException.checkNulls( aSessionID, aBackendSpecificParams, aConnectionCreationParams );
    IDtoObject obj = objectsSupport.findObject( aSessionID );
    if( obj != null ) {
      // Сессия уже существует
      IOptionSetEdit attrs = new OptionSet( obj.attrs() );
      attrs.setValue( ATRID_ENDTIME, IAtomicValue.NULL );
      IDtoObject dto = new DtoObject( aSessionID, attrs, obj.rivets().map() );
      // Создание объекта сессия. aInterceptable = false
      objectsSupport.writeObjects( IS5FrontendRear.NULL, ISkidList.EMPTY, new ElemArrayList<>( dto ), false );
      return false;
    }
    // Создание новой сессии
    IOptionSetEdit attrs = new OptionSet();
    attrs.setTime( ATRID_STARTTIME, aCreationTime );
    attrs.setValobj( ATRID_BACKEND_SPECIFIC_PARAMS, aBackendSpecificParams );
    attrs.setValobj( ATRID_CONNECTION_CREATION_PARAMS, aConnectionCreationParams );
    IDtoObject dto = new DtoObject( aSessionID, attrs, IStringMap.EMPTY );
    // Создание объекта сессия. aInterceptable = false
    objectsSupport.writeObjects( IS5FrontendRear.NULL, ISkidList.EMPTY, new ElemArrayList<>( dto ), false );
    // Установка пользователя сессии
    Skid userID = new Skid( ISkUser.CLASS_ID, aLogin );
    DtoLinkFwd dtoUserLink = new DtoLinkFwd( //
        Gwid.createLink( ISkSession.CLASS_ID, ISkSession.LNKID_USER ), aSessionID, new SkidList( userID ) );
    linksSupport.writeLinksFwd( new ElemArrayList<>( dtoUserLink ) ); // false: запрет интерсепторов
    return true;
  }

  /**
   * Обработка завершения работы объекта сессия {@link ISkSession}
   *
   * @param aSessionID {@link Skid} идентификатор {@link ISkSession}
   * @param aRemoteSessionOrNull {@link S5SessionData} удаленная сессия. null: локальная сессия
   * @param aEndTime long метка времени завершения сессии
   * @param aLoss boolean <b>true</b> потеря соединения; <b>false</b> штатное завершение соединения
   * @throws TsNullArgumentRtException аргумент = null
   */
  private void closeSkSession( Skid aSessionID, S5SessionData aRemoteSessionOrNull, long aEndTime, boolean aLoss ) {
    TsNullArgumentRtException.checkNull( aSessionID );
    // Завершение формирования статистики по сессии
    S5Statistic counter = statisticCountersBySessions.removeByKey( aSessionID );
    if( counter != null ) {
      if( aLoss ) {
        // Запись факта о нештатном завершении
        counter.onEvent( IS5ServerHardConstants.STAT_SESSION_ERRORS.id(), avInt( 1 ) );
      }
      // Синхронизация с данными сессии
      if( aRemoteSessionOrNull != null ) {
        IS5StatisticCounter statistic = (IS5StatisticCounter)aRemoteSessionOrNull.info().statistics();
        counter.update();
        counter.write( statistic );
        statistic.update();
        writeSessionData( aRemoteSessionOrNull );
      }
      // Установка признака того, что счетчик завершил свою работу
      counter.close();
    }
    // Обработка объекта сессия
    IDtoObject obj = objectsSupport.findObject( aSessionID );
    if( obj == null ) {
      // Не найден объект сессии ISkSession
      logger().error( ERR_SK_SESSION_NOT_FOUND, aSessionID );
      return;
    }
    try {
      IOptionSetEdit attrs = new OptionSet( obj.attrs() );
      attrs.setTime( ATRID_ENDTIME, aEndTime );
      IDtoObject dto = new DtoObject( aSessionID, attrs, obj.rivets().map() );
      // Создание объекта сессия. aInterceptable = false
      objectsSupport.writeObjects( IS5FrontendRear.NULL, ISkidList.EMPTY, new ElemArrayList<>( dto ), false );
    }
    catch( Throwable e ) {
      logger().error( e );
    }
  }

  /**
   * Перехват изменений объектов системы (контроль пользвателей)
   */
  private static final class S5ObjectsInterceptor
      implements IS5ObjectsInterceptor {

    private final IS5SessionManager          sessionManager;
    private final IS5BackendObjectsSingleton objectsSupport;
    private final IS5BackendLinksSingleton   linksSupport;
    private final ISkClassInfo               userClassInfo;

    /**
     * Constructor.
     *
     * @param aSessionManager {@link IS5SessionInfo} менеджер сессий
     * @param aObjectsSupport {@link IS5BackendObjectsSingleton} поддержка службы объектов
     * @param aLinksSupport {@link IS5BackendLinksSingleton} поддержка службы связей между объектами
     * @param aUserClassInfo {@link ISkClassInfo} описание класса пользователя
     * @throws TsNullArgumentRtException любой аргумент = null
     */
    public S5ObjectsInterceptor( IS5SessionManager aSessionManager, IS5BackendObjectsSingleton aObjectsSupport,
        IS5BackendLinksSingleton aLinksSupport, ISkClassInfo aUserClassInfo ) {
      TsNullArgumentRtException.checkNulls( aSessionManager, aObjectsSupport, aLinksSupport, aUserClassInfo );
      sessionManager = aSessionManager;
      objectsSupport = aObjectsSupport;
      linksSupport = aLinksSupport;
      userClassInfo = aUserClassInfo;
    }

    // ------------------------------------------------------------------------------------
    // Реализация интерфейса IS5ObjectsInterceptor
    //
    @Override
    public IDtoObject beforeFindObject( Skid aSkid, IDtoObject aObj ) {
      return aObj;
    }

    @Override
    public IDtoObject afterFindObject( Skid aSkid, IDtoObject aObj ) {
      return aObj;
    }

    @Override
    public void beforeReadObjects( IStringList aClassIds, IListEdit<IDtoObject> aObjs ) {
      // nop
    }

    @Override
    public void afterReadObjects( IStringList aClassIds, IListEdit<IDtoObject> aObjs ) {
      // nop
    }

    @Override
    public void beforeReadObjectsByIds( ISkidList aSkids, IListEdit<IDtoObject> aObjs ) {
      // nop
    }

    @Override
    public void afterReadObjectsByIds( ISkidList aSkids, IListEdit<IDtoObject> aObjs ) {
      // nop
    }

    @Override
    public void beforeWriteObjects( IMap<ISkClassInfo, IList<IDtoObject>> aRemovedObjs,
        IMap<ISkClassInfo, IList<Pair<IDtoObject, IDtoObject>>> aUpdatedObjs,
        IMap<ISkClassInfo, IList<IDtoObject>> aCreatedObjs ) {
      IList<IDtoObject> removedUsers = aRemovedObjs.findByKey( userClassInfo );
      IList<Pair<IDtoObject, IDtoObject>> changedUsers = aUpdatedObjs.findByKey( userClassInfo );
      if( (removedUsers == null || removedUsers.size() == 0) && (changedUsers == null || changedUsers.size() == 0) ) {
        return;
      }
      IList<S5SessionData> openSessions = sessionManager.openSessions();
      // Карта идентификаторов пользователей открытых сессий. Ключ: логин пользователя. Значение: идентификатор сессии
      IStringMapEdit<ISkidList> sessionIdsByLogins = new StringMap<>();
      // Формирование карты
      for( S5SessionData session : openSessions ) {
        IS5SessionInfo info = session.info();
        String login = info.login();
        SkidList sessionIds = (SkidList)sessionIdsByLogins.findByKey( login );
        if( sessionIds == null ) {
          sessionIds = new SkidList();
          sessionIdsByLogins.put( login, sessionIds );
        }
        sessionIds.add( info.sessionID() );
      }
      // Завершение всех сессий удаленных пользователей
      if( removedUsers != null ) {
        for( IDtoObject user : removedUsers ) {
          String login = user.strid();
          ISkidList sessionIds = sessionIdsByLogins.findByKey( login );
          if( sessionIds != null ) {
            for( Skid sessionId : sessionIds ) {
              sessionManager.closeRemoteSession( sessionId );
            }
          }
          // Чтобы удалить пользователя, необходимо удалить все его сессии связанные с ним по внешнему ключу
          String classId = ISkSession.CLASS_ID;
          Gwid linkGwid = Gwid.createLink( classId, ISkSession.LNKID_USER );
          IDtoLinkRev linkRev = linksSupport.findLinkRev( linkGwid, user.skid(), new StringArrayList( classId ) );
          for( Skid sessionId : linkRev.leftSkids() ) {
            // Удаление объекта сессия чтобы можно было удалить пользователя. aInterceptable = true
            objectsSupport.writeObjects( IS5FrontendRear.NULL, new SkidList( sessionId ), IList.EMPTY, true );
          }
        }
      }
      // Завершение всех сессий пользователей у которых изменился пароль или учетные записи стали неактивны
      if( changedUsers != null ) {
        for( Pair<IDtoObject, IDtoObject> userPair : changedUsers ) {
          IDtoObject oldUser = userPair.left();
          IDtoObject newUser = userPair.right();
          IOptionSet oldAttrs = oldUser.attrs();
          IOptionSet newAttrs = newUser.attrs();
          IAtomicValue oldPasswordHash = oldAttrs.findValue( ISkUserServiceHardConstants.ATRID_PASSWORD_HASH );
          IAtomicValue newPasswordHash = newAttrs.findValue( ISkUserServiceHardConstants.ATRID_PASSWORD_HASH );
          IAtomicValue newEnabled = newAttrs.findValue( ISkUserServiceHardConstants.ATRID_ROLE_IS_ENABLED );
          if( oldPasswordHash != null && !oldPasswordHash.equals( newPasswordHash )
              || newEnabled != null && !newEnabled.asBool() ) {
            ISkidList sessionIds = sessionIdsByLogins.findByKey( newUser.strid() );
            if( sessionIds != null ) {
              for( Skid sessionId : sessionIds ) {
                sessionManager.closeRemoteSession( sessionId );
              }
            }
          }
        }
      }
    }

    @Override
    public void afterWriteObjects( IMap<ISkClassInfo, IList<IDtoObject>> aRemovedObjs,
        IMap<ISkClassInfo, IList<Pair<IDtoObject, IDtoObject>>> aUpdatedObjs,
        IMap<ISkClassInfo, IList<IDtoObject>> aCreatedObjs ) {
      // nop
    }

    // ------------------------------------------------------------------------------------
    // Внутренние методы
    //

  }
}
