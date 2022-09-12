package org.toxsoft.uskat.s5.server.sessions;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.common.sessions.ISkSession.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.IS5ServerHardConstants.*;
import static org.toxsoft.uskat.s5.server.sessions.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.sessions.IS5SessionInterceptor.*;
import static org.toxsoft.uskat.s5.server.sessions.cluster.S5ClusterCommandCloseCallback.*;
import static org.toxsoft.uskat.s5.server.sessions.cluster.S5ClusterCommandCreateCallback.*;
import static org.toxsoft.uskat.s5.server.sessions.cluster.S5ClusterCommandUpdateSession.*;

import javax.annotation.Resource;
import javax.ejb.*;

import org.infinispan.Cache;
import org.infinispan.commons.util.CloseableIterator;
import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.impl.DataDef;
import org.toxsoft.core.tslib.av.impl.DataType;
import org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.bricks.strid.idgen.IStridGenerator;
import org.toxsoft.core.tslib.bricks.strid.idgen.UuidStridGenerator;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.IGwHardConstants;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ELogSeverity;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.core.api.objserv.IDtoObject;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoClassInfo;
import org.toxsoft.uskat.core.api.users.ISkUser;
import org.toxsoft.uskat.core.api.users.ISkUserServiceHardConstants;
import org.toxsoft.uskat.core.impl.SkCoreServUsers;
import org.toxsoft.uskat.core.impl.dto.*;
import org.toxsoft.uskat.s5.client.IS5ConnectionParams;
import org.toxsoft.uskat.s5.common.info.IS5SessionsInfos;
import org.toxsoft.uskat.s5.common.sessions.IS5SessionInfo;
import org.toxsoft.uskat.s5.common.sessions.ISkSession;
import org.toxsoft.uskat.s5.legacy.ISkSystem;
import org.toxsoft.uskat.s5.legacy.SynchronizedMap;
import org.toxsoft.uskat.s5.server.IS5ServerHardConstants;
import org.toxsoft.uskat.s5.server.backend.IS5BackendCoreSingleton;
import org.toxsoft.uskat.s5.server.backend.impl.S5AccessDeniedException;
import org.toxsoft.uskat.s5.server.backend.impl.S5BackendSession;
import org.toxsoft.uskat.s5.server.backend.supports.events.IS5BackendEventSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.links.IS5BackendLinksSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.objects.IS5BackendObjectsSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.IS5BackendSysDescrSingleton;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterListener;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterManager;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.interceptors.S5InterceptorSupport;
import org.toxsoft.uskat.s5.server.sessions.cluster.*;
import org.toxsoft.uskat.s5.server.sessions.pas.*;
import org.toxsoft.uskat.s5.server.singletons.S5SingletonBase;
import org.toxsoft.uskat.s5.server.startup.IS5InitialImplementSingleton;
import org.toxsoft.uskat.s5.server.statistics.*;
import org.toxsoft.uskat.s5.utils.jobs.IS5ServerJob;
import org.wildfly.clustering.group.Membership;
import org.wildfly.clustering.group.Node;

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

  private static final String STR_N_ROOT_USER       = "Root";                                      //$NON-NLS-1$
  private static final String STR_D_ROOT_USER       = "Root - superuser";                          //$NON-NLS-1$
  private static final String DEFAULT_ROOT_PASSWORD = S5BackendSession.getPasswordHashCode( "1" ); //$NON-NLS-1$

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
    // Проверка и, если необходимо, обовление sysdescr
    checkAndUpdateSysdecr( sysdescrSupport, objectsSupport );
    // Запуск фоновой задачи
    addOwnDoJob( DO_JOB_TIMEOUT );
  }

  @Override
  protected void doClose() {
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

    // Контроль за частотой подключения к серверу
    if( currTime - lastCreateSessionTimeout < CREATE_SESSION_TIMEOUT ) {
      // Доступ запрещен - сервер перегружен
      throw new S5AccessDeniedException( ERR_SERVER_OVERLOAD, Integer.valueOf( remoteOpenSessionCache.size() ) );
    }
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
          createSkSession( sessionID, frontend, login, createTime, backendSpecificParams, connectionCreationParams );
      S5SessionData prevSession = remoteOpenSessionCache.put( info.sessionID(), aSession );
      callAfterCreateSession( interceptors, sessionID, logger() );

      // TODO: формировать события
      // Формирование события
      // Gwid eventGwid = Gwid.createEvent( ISkUser.CLASS_ID, login,
      // created ? ISkUser.EVID_SESSION_CREATED : ISkUser.EVID_SESSION_RESTORED );
      // IOptionSetEdit params = new OptionSet();
      // params.setStr( ISkUser.EVPID_IP, aSession.info().remoteAddress() );
      // params.setValobj( ISkUser.EVPID_SESSION_ID, aSession.info().sessionID() );
      // SkEvent event = new SkEvent( createTime, eventGwid, params );
      // eventSupport.fireEvents( frontend, new TimedList<>( event ) );

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
    createSkSession( sessionID, frontend, login, createTime, backendSpecificParams, connectionCreationParams );
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

  // 2022-07-09 mvk
  // @TransactionAttribute( TransactionAttributeType.REQUIRED )
  // @Override
  // public void updateLocalSession( S5LocalSession aSession ) {
  // TsNullArgumentRtException.checkNull( aSession );
  // localOpenSessionCache.put( aSession.sessionID(), aSession );
  // }

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

        IS5FrontendRear frontend = closeMessenger( aSessionID );
        closeSkSession( frontend, session, sessionID, endTime, loss );

        remoteOpenSessionCache.remove( aSessionID );
        // Сессия найдена и закрыта (перемещается в кэш закрытых сессий)
        closedSessionCache.put( aSessionID, session );

        // TODO: формировать события
        // Формирование события
        // String evId = (loss ? ISkUser.EVID_SESSION_BREAKED : ISkUser.EVID_SESSION_CLOSED);
        // Gwid eventGwid = Gwid.createEvent( ISkUser.CLASS_ID, session.info().login(), evId );
        // IOptionSetEdit params = new OptionSet();
        // params.setStr( ISkUser.EVPID_IP, session.info().remoteAddress() );
        // params.setValobj( ISkUser.EVPID_SESSION_ID, session.info().sessionID() );
        // SkEvent event = new SkEvent( endTime, eventGwid, params );
        // eventSupport.fireEvents( frontend, new TimedList<>( event ) );

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
      IS5FrontendRear frontend = session.frontend();
      long endTime = System.currentTimeMillis();
      boolean loss = false;
      closeSkSession( frontend, null, sessionID, endTime, loss );
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
      // if( session != null && session.attrs().getValue( AID_ENDTIME ).isAssigned() == false ) {
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
  // Внутренние методы. Формирование SkConnection
  //
  /**
   * Создание нового объекта {@link ISkSession}
   *
   * @param aSessionID {@link Skid} идентификатор сессии
   * @param aFrontend {@link IS5FrontendRear} фронтенд сессии
   * @param aLogin String логин пользователя подключенного к системе
   * @param aCreationTime long метка времени создания сессии
   * @param aBackendSpecificParams {@link IOptionSet} параметры бекенда
   * @param aConnectionCreationParams {@link IOptionSet} параметры создания соединения
   * @return boolean <b>true</b> создан объект сессии; <b>false</b> объект сессии уже существует
   * @throws TsNullArgumentRtException аргумент = null
   */
  private boolean createSkSession( Skid aSessionID, IS5FrontendRear aFrontend, String aLogin, long aCreationTime,
      IOptionSet aBackendSpecificParams, IOptionSet aConnectionCreationParams ) {
    TsNullArgumentRtException.checkNulls( aSessionID, aFrontend, aBackendSpecificParams, aConnectionCreationParams );
    IDtoObject obj = objectsSupport.findObject( aSessionID );
    if( obj != null ) {
      // Сессия уже существует
      IOptionSetEdit attrs = new OptionSet( obj.attrs() );
      attrs.setValue( AID_ENDTIME, IAtomicValue.NULL );
      IDtoObject dto = new DtoObject( aSessionID, attrs, obj.rivets().map() );
      // Создание объекта сессия. aInterceptable = false
      objectsSupport.writeObjects( IS5FrontendRear.NULL, ISkidList.EMPTY, new ElemArrayList<>( dto ), false );
      // 2021-04-09 mvk создание статистики проводится как lazy через findStatisticCounter
      // Статистика сессии
      // if( statisticCountersBySessions.findByKey( aSessionID ) == null ) {
      // statisticCountersBySessions.put( aSessionID, new S5Statistic( STAT_SESSION_PARAMS ) );
      // }

      // TODO: 2021-08-23 mvk подготовлено для удаления
      // Формирование события
      // Gwid eventGwid = Gwid.createEvent( CLASS_ID, aSessionID.strid(), EVID_STATE_CHANGED );
      // IOptionSetEdit params = new OptionSet();
      // params.setValobj( EVPID_NEW_STATE, ESkConnState.ACTIVE );
      // SkEvent event = new SkEvent( aCreationTime, eventGwid, params );
      // eventSupport.fireEvents( aFrontend, new TimedList<>( event ) );
      return false;
    }
    // Создание новой сессии
    IOptionSetEdit attrs = new OptionSet();
    attrs.setTime( AID_STARTTIME, aCreationTime );
    attrs.setValobj( AID_BACKEND_SPECIFIC_PARAMS, aBackendSpecificParams );
    attrs.setValobj( AID_CONNECTION_CREATION_PARAMS, aConnectionCreationParams );
    IDtoObject dto = new DtoObject( aSessionID, attrs, IStringMap.EMPTY );
    // Создание объекта сессия. aInterceptable = false
    objectsSupport.writeObjects( IS5FrontendRear.NULL, ISkidList.EMPTY, new ElemArrayList<>( dto ), false );
    // Установка пользователя сессии
    Skid userID = new Skid( ISkUser.CLASS_ID, aLogin );
    DtoLinkFwd dtoUserLink = new DtoLinkFwd( //
        Gwid.createLink( ISkSession.CLASS_ID, ISkSession.LNKID_USER ), aSessionID, new SkidList( userID ) );
    linksSupport.writeLinksFwd( new ElemArrayList<>( dtoUserLink ) ); // false: запрет интерсепторов
    // 2021-04-09 mvk создание статистики проводится как lazy через findStatisticCounter
    // Статистика сессии
    // if( statisticCountersBySessions.findByKey( aSessionID ) == null ) {
    // statisticCountersBySessions.put( aSessionID, new S5Statistic( STAT_SESSION_PARAMS ) );
    // }
    return true;
  }

  /**
   * Обработка завершения работы объекта сессия {@link ISkSession}
   *
   * @param aFrontend {@link IS5FrontendRear} фронтенд сессии
   * @param aRemoteSessionOrNull {@link S5SessionData} удаленная сессия. null: локальная сессия
   * @param aSessionID {@link Skid} идентификатор {@link ISkSession}
   * @param aEndTime long метка времени завершения сессии
   * @param aLoss boolean <b>true</b> потеря соединения; <b>false</b> штатное завершение соединения
   * @throws TsNullArgumentRtException аргумент = null
   */
  private void closeSkSession( IS5FrontendRear aFrontend, S5SessionData aRemoteSessionOrNull, Skid aSessionID,
      long aEndTime, boolean aLoss ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aSessionID );
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
      attrs.setTime( AID_ENDTIME, aEndTime );
      IDtoObject dto = new DtoObject( aSessionID, attrs, obj.rivets().map() );
      // Создание объекта сессия. aInterceptable = false
      objectsSupport.writeObjects( IS5FrontendRear.NULL, ISkidList.EMPTY, new ElemArrayList<>( dto ), false );

      // TODO: 2021-08-23 mvk подготовлено для удаления
      // // Формирование события
      // Gwid eventGwid = Gwid.createEvent( CLASS_ID, aSessionID.strid(), EVID_STATE_CHANGED );
      // IOptionSetEdit params = new OptionSet();
      // params.setValobj( EVPID_NEW_STATE, (aLoss == false ? ESkConnState.CLOSED : ESkConnState.INACTIVE) );
      // SkEvent event = new SkEvent( aEndTime, eventGwid, params );
      // eventSupport.fireEvents( aFrontend, new TimedList<>( event ) );
    }
    catch( Throwable e ) {
      logger().error( e );
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы. Формирование событий
  //
  /**
   * Создает идентифицирующую информацию о сессии пользователя
   *
   * @param aSession {@link IS5SessionInfo} информация о сессии пользователя
   * @return {@link S5SessionIdentity} идентифицирующая информация о сессии пользователя
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static S5SessionIdentity createSessionIdentity( IS5SessionInfo aSession ) {
    TsNullArgumentRtException.checkNull( aSession );
    String login = aSession.login();
    long openTime = aSession.openTime();
    String ip = aSession.remoteAddress();
    int port = aSession.remotePort();
    // Сохранение свойств клиента в событии
    IOptionSetEdit features = new OptionSet();
    // IListEdit<ITypedOptionSetEdit<IFeatureOptions>> fs = IClientInfoOptions.FEATURES.get( aSession.client() );
    // for( ITypedOptionSetEdit<IFeatureOptions> f : fs ) {
    // String id = IFeatureOptions.ID.getValue( f ).asString();
    // IAtomicValue value = IFeatureOptions.VALUE.getValue( f );
    // features.setValue( id, value );
    // }
    S5SessionIdentity sessionIdentity =
        new S5SessionIdentity( aSession.sessionID(), login, openTime, ip, port, features );
    return sessionIdentity;
  }

  /**
   * Формирует сообщение об открытии сессии пользователя
   *
   * @param aSession {@link S5SessionData} сессия
   * @throws TsNullArgumentRtException аргумент = null
   */
  private void fireOpenSessionEvent( S5SessionData aSession ) {
    TsNullArgumentRtException.checkNull( aSession );
    // Формирование события о об открытии сессии
    logger().info( MSG_CREATED_SESSION, aSession );
    // Описание сессии
    IS5SessionInfo sessionInfo = aSession.info();
    // Идентификатор сессии
    S5SessionIdentity session = createSessionIdentity( sessionInfo );
    // Формирование параметров события
    IStringMapEdit<IAtomicValue> params = new StringMap<>();
    // TODO:
    // Fimbed fimbedSession = new Fimbed( session, S5SessionIdentity.KEEPER_ID );
    // params.put( IUser.EP_SESSION_IDENTITY, DvUtils.avFimbed( fimbedSession ) );
    // Асинхронное(!) создание события для пользователей. 0: время сервера
    // eventServiceSingleton.fireAsyncEvent( sessionInfo.userId(), IUser.EVENT_ID_CONNECTED, params, 0 );
  }

  /**
   * Формирует сообщение об закрытии сессии пользователя
   *
   * @param aSession {@link S5SessionData} сессия
   * @throws TsNullArgumentRtException аргумент = null
   */
  private void fireCloseSessionEvent( S5SessionData aSession ) {
    TsNullArgumentRtException.checkNull( aSession );
    // Формирование события о завершение сессии
    logger().info( MSG_REMOVED_SESSION, aSession );
    // Описание сессии
    IS5SessionInfo sessionInfo = aSession.info();
    // Идентификатор сессии
    S5SessionIdentity session = createSessionIdentity( sessionInfo );
    // Формирование параметров события
    IStringMapEdit<IAtomicValue> params = new StringMap<>();
    // TODO:
    // Fimbed fimbedSession = new Fimbed( session, S5SessionIdentity.KEEPER_ID );
    // params.put( IUser.EP_SESSION_IDENTITY, DvUtils.avFimbed( fimbedSession ) );
    // Идентификатор события (обрыв или штатное завершение связи)
    // String eventId = (sessionInfo.closeByRemote() ? IUser.EVENT_ID_DISCONNECTED : IUser.EVENT_ID_CONN_LOST);
    // Асинхронное(!) создание события для пользователей. 0: время сервера
    // eventServiceSingleton.fireAsyncEvent( sessionInfo.userId(), eventId, params, 0 );
  }

  /**
   * Проверяет и, если необходимо, обновляет системное описание для работы менеджера сессий
   *
   * @param aSysdescrSupport {@link IS5BackendSysDescrSingleton} поддержка классов
   * @param aObjectsSupport {@link IS5BackendObjectsSingleton} поддержка объектов
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static void checkAndUpdateSysdecr( IS5BackendSysDescrSingleton aSysdescrSupport,
      IS5BackendObjectsSingleton aObjectsSupport ) {
    TsNullArgumentRtException.checkNulls( aSysdescrSupport, aObjectsSupport );
    // Существующие классы
    IStridablesList<IDtoClassInfo> classes = aSysdescrSupport.readClassInfos();
    // Добавляемые класы
    IStridablesListEdit<IDtoClassInfo> newClasses = new StridablesList<>();
    // Создание класса система
    if( !classes.hasKey( ISkSystem.CLASS_ID ) ) {
      // Создание класса ISkSystem
      DtoClassInfo dtoSystem = new DtoClassInfo( ISkSystem.CLASS_ID, IGwHardConstants.GW_ROOT_CLASS_ID, //
          OptionSetUtils.createOpSet( //
              IAvMetaConstants.TSID_NAME, STR_N_SYSTEM, //
              IAvMetaConstants.TSID_DESCRIPTION, STR_D_SYSTEM //
          ) );
      dtoSystem.eventInfos().addAll( //
          DtoEventInfo.create1( ISkSystem.EVID_LOGIN_FAILED, true, //
              new StridablesList<>( //
                  DataDef.create( ISkSystem.EVPID_LOGIN, EAtomicType.STRING, TSID_NAME, STR_N_EV_PARAM_LOGIN, //
                      TSID_DESCRIPTION, STR_D_EV_PARAM_LOGIN, //
                      TSID_IS_NULL_ALLOWED, AV_FALSE, //
                      TSID_DEFAULT_VALUE, AV_STR_EMPTY ), //
                  DataDef.create( ISkSystem.EVPID_IP, EAtomicType.STRING, TSID_NAME, STR_N_EV_PARAM_IP, //
                      TSID_DESCRIPTION, STR_D_EV_PARAM_IP, //
                      TSID_IS_NULL_ALLOWED, AV_FALSE, //
                      TSID_DEFAULT_VALUE, AV_STR_EMPTY ) //
              ), //
              OptionSetUtils.createOpSet( //
                  IAvMetaConstants.TSID_NAME, STR_N_EV_LOGIN_FAILED, //
                  IAvMetaConstants.TSID_DESCRIPTION, STR_D_EV_LOGIN_FAILED //
              ) ), //
          DtoEventInfo.create1( ISkSystem.EVID_SYSDESCR_CHANGED, true, //
              new StridablesList<>( //
                  DataDef.create( ISkSystem.EVPID_USER, EAtomicType.STRING, TSID_NAME, STR_N_EV_PARAM_USER, //
                      TSID_DESCRIPTION, STR_D_EV_PARAM_USER, //
                      TSID_IS_NULL_ALLOWED, AV_FALSE, //
                      TSID_DEFAULT_VALUE, AV_STR_EMPTY ), //
                  DataDef.create( ISkSystem.EVPID_DESCR, EAtomicType.STRING, TSID_NAME, STR_N_EV_PARAM_DESCR, //
                      TSID_DESCRIPTION, STR_D_EV_PARAM_DESCR, //
                      TSID_IS_NULL_ALLOWED, AV_FALSE, //
                      TSID_DEFAULT_VALUE, AV_STR_EMPTY ), //
                  DataDef.create( ISkSystem.EVPID_EDITOR, EAtomicType.STRING, TSID_NAME, STR_N_EV_PARAM_EDITOR, //
                      TSID_DESCRIPTION, STR_D_EV_PARAM_EDITOR, //
                      TSID_IS_NULL_ALLOWED, AV_FALSE, //
                      TSID_DEFAULT_VALUE, AV_STR_EMPTY ) //
              ), //
              OptionSetUtils.createOpSet( //
                  IAvMetaConstants.TSID_NAME, STR_N_EV_SYSDESCR_CHANGED, //
                  IAvMetaConstants.TSID_DESCRIPTION, STR_D_EV_SYSDESCR_CHANGED //
              ) //
          )//
      );
      newClasses.add( dtoSystem );
    }
    // Создание класса пользователя
    if( !classes.hasKey( ISkUser.CLASS_ID ) ) {
      newClasses.add( SkCoreServUsers.internalCreateUserClassDto() );
    }
    // Создание класса сессия
    if( !classes.hasKey( ISkSession.CLASS_ID ) ) {
      // Создание класса ISkSession
      DtoClassInfo dtoSession = new DtoClassInfo( ISkSession.CLASS_ID, IGwHardConstants.GW_ROOT_CLASS_ID, //
          OptionSetUtils.createOpSet( //
              IAvMetaConstants.TSID_NAME, STR_N_SESSION, //
              IAvMetaConstants.TSID_DESCRIPTION, STR_D_SESSION //
          ) );

      // AID_STARTTIME
      dtoSession.attrInfos().add( DtoAttrInfo.create1( ISkSession.AID_STARTTIME, DataType.create( TIMESTAMP, //
          TSID_NAME, STR_N_AID_STARTTIME, //
          TSID_DESCRIPTION, STR_D_AID_STARTTIME //
      ), //
          IOptionSet.NULL ) );
      // AID_ENDTIME
      dtoSession.attrInfos().add( DtoAttrInfo.create1( ISkSession.AID_ENDTIME, DataType.create( TIMESTAMP, //
          TSID_NAME, STR_N_AID_ENDTIME, //
          TSID_DESCRIPTION, STR_D_AID_ENDTIME //
      ), IOptionSet.NULL ) );
      // AID_BACKEND_SPECIFIC_PARAMS
      dtoSession.attrInfos().add( DtoAttrInfo.create1( ISkSession.AID_BACKEND_SPECIFIC_PARAMS, DataType.create( VALOBJ, //
          TSID_NAME, STR_N_AID_BACKEND_SPECIFIC_PARAMS, //
          TSID_DESCRIPTION, STR_D_AID_BACKEND_SPECIFIC_PARAMS, //
          TSID_KEEPER_ID, OptionSetKeeper.KEEPER_ID, //
          TSID_IS_NULL_ALLOWED, AV_FALSE, //
          TSID_DEFAULT_VALUE, avValobj( new OptionSet() ) //
      ), IOptionSet.NULL ) );
      // AID_CONNECTION_CREATION_PARAMS
      dtoSession.attrInfos()
          .add( DtoAttrInfo.create1( ISkSession.AID_CONNECTION_CREATION_PARAMS, DataType.create( VALOBJ, //
              TSID_NAME, STR_N_AID_CONNECTION_CREATION_PARAMS, //
              TSID_DESCRIPTION, STR_D_AID_CONNECTION_CREATION_PARAMS, //
              TSID_KEEPER_ID, OptionSetKeeper.KEEPER_ID, //
              TSID_IS_NULL_ALLOWED, AV_FALSE, //
              TSID_DEFAULT_VALUE, avValobj( new OptionSet() ) //
          ), IOptionSet.NULL ) );
      // TODO:
      // dtoSession.linkInfos().addAll( ISkUserServiceConstants.LNKINF_USER );
      // dtoSession.rtdataInfos().addAll( ISkUserServiceConstants.RTDINF_STATE );
      // dtoSession.eventInfos().addAll( ISkUserServiceConstants.EVINF_STATE_CHANGED );
      newClasses.add( dtoSession );
    }
    if( newClasses.size() > 0 ) {
      aSysdescrSupport.writeClassInfos( IStringList.EMPTY, newClasses );
    }
    // Создание пользователя root
    if( aObjectsSupport.findObject( ISkUserServiceHardConstants.SKID_USER_ROOT ) == null ) {
      IOptionSetEdit attrs = new OptionSet();
      attrs.setStr( AID_NAME, STR_N_ROOT_USER );
      attrs.setStr( AID_DESCRIPTION, STR_D_ROOT_USER );
      attrs.setStr( ISkUserServiceHardConstants.ATRID_PASSWORD_HASH, DEFAULT_ROOT_PASSWORD );
      IDtoObject root = new DtoObject( ISkUserServiceHardConstants.SKID_USER_ROOT, attrs, IStringMap.EMPTY );
      aObjectsSupport.writeObjects( IS5FrontendRear.NULL, ISkidList.EMPTY, new ElemArrayList<>( root ), true );
    }
  }
}
