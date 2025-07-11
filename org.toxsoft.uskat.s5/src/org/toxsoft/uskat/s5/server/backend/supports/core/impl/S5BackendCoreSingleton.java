package org.toxsoft.uskat.s5.server.backend.supports.core.impl;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.utils.logs.ELogSeverity.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.IS5ServerHardConstants.*;
import static org.toxsoft.uskat.s5.server.backend.ES5ServerMode.*;
import static org.toxsoft.uskat.s5.server.backend.supports.core.IS5BackendCoreInterceptor.*;
import static org.toxsoft.uskat.s5.server.backend.supports.core.S5BackendCoreConfig.*;
import static org.toxsoft.uskat.s5.server.backend.supports.core.impl.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.transactions.ES5TransactionResources.*;
import static org.toxsoft.uskat.s5.utils.platform.S5ServerPlatformUtils.*;

import java.sql.*;
import java.time.*;
import java.util.concurrent.*;

import javax.annotation.*;
import javax.ejb.*;
import javax.persistence.*;
import javax.sql.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.bricks.validator.vrl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.coll.synch.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.classes.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.*;
import org.toxsoft.uskat.s5.common.*;
import org.toxsoft.uskat.s5.server.*;
import org.toxsoft.uskat.s5.server.backend.*;
import org.toxsoft.uskat.s5.server.backend.supports.clobs.*;
import org.toxsoft.uskat.s5.server.backend.supports.core.*;
import org.toxsoft.uskat.s5.server.backend.supports.links.*;
import org.toxsoft.uskat.s5.server.backend.supports.objects.*;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.*;
import org.toxsoft.uskat.s5.server.cluster.*;
import org.toxsoft.uskat.s5.server.frontend.*;
import org.toxsoft.uskat.s5.server.interceptors.*;
import org.toxsoft.uskat.s5.server.sessions.*;
import org.toxsoft.uskat.s5.server.singletons.*;
import org.toxsoft.uskat.s5.server.startup.*;
import org.toxsoft.uskat.s5.server.statistics.*;
import org.toxsoft.uskat.s5.server.transactions.*;
import org.toxsoft.uskat.s5.utils.jobs.*;
import org.toxsoft.uskat.s5.utils.platform.*;
import org.toxsoft.uskat.s5.utils.threads.impl.*;

/**
 * Реализация синглтона {@link IS5BackendCoreSingleton}.
 * <p>
 *
 * @author mvk
 */
@Startup
@Singleton
// @LocalBean
@DependsOn( { //
    TRANSACTION_MANAGER_SINGLETON, //
    CLUSTER_MANAGER_SINGLETON, //
    PROJECT_INITIAL_IMPLEMENT_SINGLETON //
} )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
@ConcurrencyManagement( ConcurrencyManagementType.BEAN )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@Lock( LockType.READ )
public class S5BackendCoreSingleton
    extends S5SingletonBase
    implements IS5BackendCoreSingleton, IS5ServerJob {

  private static final long serialVersionUID = 157157L;

  /**
   * Имя синглетона в контейнере сервера для организации зависимостей (@DependsOn)
   */
  public static final String BACKEND_CORE_ID = "S5BackendCoreSingleton"; //$NON-NLS-1$

  /**
   * Таймаут ожидания загрузки поддержки (мсек)
   */
  public static final long SUPPORT_READY_CHECK_INTERVAL = 100;

  /**
   * Таймаут ожидания загрузки поддержки (мсек)
   */
  public static final long SUPPORT_READY_TIMEOUT = 60 * 1000;

  /**
   * Соединение с базой данных
   */
  @Resource
  private DataSource dataSource;

  /**
   * Фабрика менеджеров постоянства Application Managed Entity Manager (используемых для многопоточной записи)
   * <p>
   * Источники(persitent context + transaction + AbstractSkObjectManager):
   * http://www.kumaranuj.com/2013/06/jpa-2-entitymanagers-transactions-and.html
   * https://docs.oracle.com/cd/E19798-01/821-1841/bnbra/index.html
   */
  @PersistenceUnit
  private EntityManagerFactory entityManagerFactory;

  /**
   * Менеджер транзакций
   */
  @EJB
  private IS5TransactionManagerSingleton txManager;

  /**
   * Менеджер кластера s5-сервера
   */
  @EJB
  private IS5ClusterManager clusterManager;

  /**
   * Менеджер сессий
   */
  // 2022-08-21 mvk ---
  // @EJB
  private IS5SessionManager sessionManager;

  /**
   * Начальная, неизменяемая, проектно-зависимая конфигурация реализации бекенда сервера
   */
  @EJB
  private IS5InitialImplementSingleton initialConfig;

  /**
   * backend системного описания
   */
  @EJB
  private IS5BackendSysDescrSingleton sysdescrBackend;

  /**
   * backend управления объектами
   */
  @EJB
  private IS5BackendObjectsSingleton objectsBackend;

  /**
   * backend управления связями между объектами
   */
  @EJB
  private IS5BackendLinksSingleton linksBackend;

  /**
   * backend управления большими объектами объектами (Large OBject - LOB) системы
   */
  @EJB
  private IS5BackendClobsSingleton lobsBackend;

  /**
   * Карта синглетонов поддержки бекенда
   * <p>
   * Ключ: идентификатор поддержки {@link IS5BackendSupportSingleton#id()}<br>
   * Значение: интерфейс доступа к поддержке
   */
  private final IStringMapEdit<IS5BackendSupportSingleton> backendSupports =
      new SynchronizedStringMap<>( new StringMap<>() );

  /**
   * Поддержка frontends
   */
  private final IS5FrontendAttachable frontendsSupport = new S5FrontendsSupport();

  /**
   * Поддержка интерсепторов операций проводимых над объектами
   */
  private final S5InterceptorSupport<IS5BackendCoreInterceptor> interceptors = new S5InterceptorSupport<>();

  /**
   * Текущее состояние сервера (управляемое ядром).
   */
  private ES5ServerMode controlledMode = LOADING;

  /**
   * Метка времени (мсек с начала эпохи) последнего изменения состояния сервера
   */
  private long modeTime = System.currentTimeMillis();

  /**
   * Информация о бекенде
   */
  private SkBackendInfo backendInfo;

  /**
   * Общее(разделяемое между модулями) соединение с сервером
   */
  private ISkConnection sharedConnection;

  /**
   * Писатель статитистики объекта {@link ISkServerNode}. null: нет соединения
   */
  private S5StatisticWriter statisticWriter;

  /**
   * Кэширование значений параметров конфигурации.
   */
  private long   startTimeMin;
  private long   startTimeMax;
  private double boostedAverage;
  private long   overloadDelay;
  private double overloadAverage;

  /**
   * Метка времени когда загрузка стала равной или больше чем {@link #overloadAverage}. TimeUtils.MIN_TIMESTAMP: не
   * установлено.
   */
  private long overloadTimestamp = TimeUtils.MIN_TIMESTAMP;

  /**
   * Конструктор.
   */
  public S5BackendCoreSingleton() {
    super( BACKEND_CORE_ID, STR_D_BACKEND_CORE );
  }

  // ------------------------------------------------------------------------------------
  // IS5SingletonCtrl
  //
  @Override
  protected void doInit() {
    logger().log( INFO, MSG_INIT_BACKEND );
    // Информация о сервере
    try( Connection dbConnection = dataSource.getConnection() ) {
      DatabaseMetaData meta = dbConnection.getMetaData();
      logger().info( "DatabaseProductName: " + meta.getDatabaseProductName() ); //$NON-NLS-1$
      logger().info( "DatabaseMajorVersion: " + meta.getDatabaseMajorVersion() ); //$NON-NLS-1$
      logger().info( "DatabaseMinorVersion: " + meta.getDatabaseMinorVersion() ); //$NON-NLS-1$
      logger().info( "DatabaseProductVersion: " + meta.getDatabaseProductVersion() ); //$NON-NLS-1$
    }
    catch( SQLException e ) {
      logger().error( e );
    }
    long currTime = System.currentTimeMillis();
    // Параметры бекенда
    IOptionSet initialConfigParams = initialConfig.impl().params();
    // Модуль реализующий бекенд
    S5Module module = OP_BACKEND_MODULE.getValue( initialConfigParams ).asValobj();
    // Описание бекенда
    backendInfo = new SkBackendInfo( module.id(), currTime );
    // Перекрываем параметры
    backendInfo.params().addAll( initialConfigParams );
    // Время запуска сервера
    IS5ServerHardConstants.OP_BACKEND_START_TIME.setValue( backendInfo.params(), avTimestamp( currTime ) );
    // Установка режима работы с кэшем сессий (infinispan)
    // ConfigurationBuilder builder = new ConfigurationBuilder();
    // // builder.transaction().lockingMode( LockingMode.PESSIMISTIC );
    // builder.transaction().lockingMode( LockingMode.OPTIMISTIC );

    IOptionSet configuration = configuration();
    startTimeMin = CORE_START_TIME_MIN.getValue( configuration ).asLong() * 1000;
    startTimeMax = CORE_START_TIME_MAX.getValue( configuration ).asLong() * 1000;
    boostedAverage = CORE_BOOSTED_AVERAGE.getValue( configuration ).asDouble();
    overloadDelay = CORE_OVERLOADED_DELAY.getValue( configuration ).asLong() * 1000;
    overloadAverage = CORE_OVERLOADED_AVERAGE.getValue( configuration ).asDouble();

    // Инициализация backend у менеджера сессий
    // 2022-08-21 mvk
    // sessionManager.setBackend( sessionContext().getBusinessObject( IS5BackendCoreSingleton.class ) );
    // Запуск фоновой задачи
    addOwnDoJob( BACKEND_JOB_TIMEOUT );
    // Разрешение регистрации пользователей
    // TODO: S5ServerLoginModule упразднен, нового пока нет.
    // S5ServerLoginModule.setEnabled( true );
    // Слушатель формирущий статистику
    IntervalStatisticListener statisticListener = new IntervalStatisticListener();
    // Регистрация слушателя транзакций для формирования по ним статистики
    txManager.addTransactionListener( statisticListener );
    // Вывод в журнал сообщения о запуске бекенда
    logger().log( INFO, MSG_CREATE_BACKEND, module.id(), module.description(), version,
        IS5ServerHardConstants.version );
  }

  @Override
  protected void doClose() {
    logger().log( INFO, MSG_CLOSE_BACKEND );
    // Запрет регистрации пользователей
    // TODO: S5ServerLoginModule упразднен, нового пока нет.
    // S5ServerLoginModule.setEnabled( false );
    super.doClose();
  }

  @Override
  protected IStringList doConfigurationPaths() {
    return new StringArrayList( ALL_CORE_OPDEFS.keys() );
  }

  @Override
  protected void onConfigChanged( IOptionSet aPrevConfig, IOptionSet aNewConfig ) {
    startTimeMin = CORE_START_TIME_MIN.getValue( aNewConfig ).asLong() * 1000;
    startTimeMax = CORE_START_TIME_MAX.getValue( aNewConfig ).asLong() * 1000;
    boostedAverage = CORE_BOOSTED_AVERAGE.getValue( aNewConfig ).asDouble();
    overloadDelay = CORE_OVERLOADED_DELAY.getValue( aNewConfig ).asLong() * 1000;
    overloadAverage = CORE_OVERLOADED_AVERAGE.getValue( aNewConfig ).asDouble();
  }

  // ------------------------------------------------------------------------------------
  // IS5BackendCoreSingleton
  //
  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public ISkBackendInfo getInfo() {
    // Размещение текущей информации о сервере (backend)
    synchronized (backendInfo) {
      try {
        OP_BACKEND_ZONE_ID.setValue( backendInfo.params(), avStr( ZoneId.systemDefault().getId() ) );
        OP_BACKEND_CURRENT_TIME.setValue( backendInfo.params(), avTimestamp( System.currentTimeMillis() ) );
        if( sessionManager != null ) {
          // Доступен (уже загружен) менеджер сессий
          OP_BACKEND_SESSIONS_INFOS.setValue( backendInfo.params(), avValobj( sessionManager().getInfos() ) );
        }
        OP_BACKEND_TRANSACTIONS_INFOS.setValue( backendInfo.params(), avValobj( txManager.getInfos() ) );
        OP_BACKEND_HEAP_MEMORY_USAGE.setValue( backendInfo.params(), avStr( printHeapMemoryUsage() ) );
        OP_BACKEND_NON_HEAP_MEMORY_USAGE.setValue( backendInfo.params(), avStr( printNonHeapMemoryUsage() ) );
        OP_BACKEND_PLATFORM_INFO.setValue( backendInfo.params(), avStr( printOperatingSystemDetails() ) );
      }
      catch( Throwable e ) {
        logger().error( e );
      }
    }

    return backendInfo;
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public EntityManagerFactory entityManagerFactory() {
    return entityManagerFactory;
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IS5TransactionManagerSingleton txManager() {
    return transactionManager();
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IS5ClusterManager clusterManager() {
    return clusterManager;
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IS5SessionManager sessionManager() {
    TsInternalErrorRtException.checkNull( sessionManager );
    return sessionManager;
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IS5InitialImplementSingleton initialConfig() {
    return initialConfig;
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public void setSessionManager( IS5SessionManager aSessionManager ) {
    TsNullArgumentRtException.checkNull( aSessionManager );
    TsIllegalStateRtException.checkNoNull( sessionManager );
    sessionManager = aSessionManager;
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IStringList listSupportIds() {
    return backendSupports.keys();
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public void addSupport( String aSupportId, IS5BackendSupportSingleton aSupportInterface ) {
    TsNullArgumentRtException.checkNulls( aSupportId, aSupportInterface );
    if( backendSupports.hasKey( aSupportId ) ) {
      // Поддержка уже зарегистрирована
      throw new TsIllegalArgumentRtException( ERR_SUPPORT_ALREADY_REGISTER, aSupportId );
    }
    backendSupports.put( aSupportId, aSupportInterface );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public void removeSupport( String aSupportId ) {
    TsNullArgumentRtException.checkNull( aSupportId );
    backendSupports.removeByKey( aSupportId );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public <T extends IS5BackendSupportSingleton> T findSupport( String aSupportId, Class<T> aSupportInterface ) {
    TsNullArgumentRtException.checkNulls( aSupportId, aSupportInterface );
    try {
      IS5BackendSupportSingleton support = null;
      int waitCount = 0;
      long startTime = System.currentTimeMillis();
      // while( !isReadySupportSingletons() ) {
      while( support == null ) {
        if( System.currentTimeMillis() - startTime > SUPPORT_READY_TIMEOUT ) {
          throw new TsInternalErrorRtException( ERR_SUPPORT_IS_NOT_AVAILABLE );
        }
        support = backendSupports.findByKey( aSupportId );
        if( support == null ) {
          logger().warning( ERR_WAITING_SUPPORT, aSupportId, Integer.valueOf( waitCount++ ) );
          Thread.sleep( SUPPORT_READY_CHECK_INTERVAL );
        }
      }
      T retValue = aSupportInterface.cast( support );
      return retValue;
    }
    catch( Exception ex ) {
      throw new TsIllegalArgumentRtException( ex, ex.getMessage() );
    }
  }

  @Lock( LockType.WRITE )
  @Override
  public boolean setMode( ES5ServerMode aMode ) {
    TsNullArgumentRtException.checkNull( aMode );
    if( controlledMode == aMode ) {
      return true;
    }
    ES5ServerMode oldMode = controlledMode;
    long oldTimeout = S5Lockable.accessTimeoutDefault();
    // Пред-интерсепция
    IVrList vr = callBeforeChangeServerModeInterceptors( interceptors, oldMode, aMode );
    if( vr.firstWorstResult().isError() ) {
      // Интерсепторы запретили выполнение операции
      logger().error( ERR_REJECT_CHANGE_MODE, oldMode, aMode, vr.items() );
      return false;
    }
    controlledMode = aMode;
    modeTime = System.currentTimeMillis();
    switch( controlledMode ) {
      case LOADING:
        // Выход из режима загрузки, разрешение doJob-потоков
        S5SingletonLocker.setDoJobEnable( true );
        //$FALL-THROUGH$
      case STARTING:
      case BOOSTED:
      case OVERLOADED:
        S5Lockable.setAccessTimeoutDefault( ACCESS_BOOST_TIMEOUT );
        break;
      case WORKING:
      case SHUTDOWNING:
      case OFF:
        S5Lockable.setAccessTimeoutDefault( ACCESS_TIMEOUT_DEFAULT );
        break;
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
    // Пост-интерсепция
    try {
      callAfterChangeServerModeInterceptors( interceptors, oldMode, aMode );
      switch( aMode ) {
        case LOADING:
          // Запрещено переключаться в режим загрузки.
          throw new TsInternalErrorRtException();
        case STARTING:
        case WORKING:
        case SHUTDOWNING:
        case OFF:
          logger().info( MSG_CHANGE_MODE, oldMode, aMode, Double.valueOf( loadAverage() ) );
          break;
        case BOOSTED:
          logger().warning( MSG_CHANGE_MODE, oldMode, aMode, Double.valueOf( loadAverage() ) );
          break;
        case OVERLOADED:
          logger().error( MSG_CHANGE_MODE, oldMode, aMode, Double.valueOf( loadAverage() ) );
          break;
        default:
          throw new TsNotAllEnumsUsedRtException();
      }
    }
    catch( Throwable e ) {
      // Восстановление состояния на любой ошибке
      controlledMode = oldMode;
      S5Lockable.setAccessTimeoutDefault( oldTimeout );
      logger().error( e );
      throw e;
    }
    return true;
  }

  @Override
  public ES5ServerMode mode() {
    return controlledMode;
  }

  @Override
  public ISkConnection getSharedConnection() {
    TsIllegalArgumentRtException.checkFalse( isActive() );
    return sharedConnection;
  }

  @Lock( LockType.WRITE )
  @Override
  public void setSharedConnection( ISkConnection aConnection ) {
    TsNullArgumentRtException.checkNull( aConnection );
    TsItemAlreadyExistsRtException.checkNoNull( sharedConnection );
    // Пред-интерсепция
    IVrList vr = callBeforeSetSharedConnectionInterceptors( interceptors, aConnection );
    if( vr.firstWorstResult().isError() ) {
      // Интерсепторы запретили выполнение операции
      logger().error( ERR_REJECT_CHANGE_CONNECTION, aConnection, vr.items() );
      return;
    }
    sharedConnection = aConnection;
    // Пост-интерсепция
    try {
      callAfterSetSharedConnectionInterceptors( interceptors, aConnection );
    }
    catch( Throwable e ) {
      sharedConnection = null;
      throw e;
    }
    // Информация о бекенде
    ISkBackendInfo info = getInfo();
    // Идентификатор узла сервера
    Skid nodeId = OP_SERVER_NODE_ID.getValue( info.params() ).asValobj();
    // Создание писателя статистики узла сервера
    statisticWriter = new S5StatisticWriter( aConnection, nodeId, STAT_BACKEND_NODE_PARAMS );
  }

  @Override
  public IS5StatisticCounter statisticCounter() {
    return statisticWriter;
  }

  @Override
  public void addBackendCoreInterceptor( IS5BackendCoreInterceptor aInterceptor, int aPriority ) {
    TsNullArgumentRtException.checkNull( aInterceptor );
    interceptors.add( aInterceptor, aPriority );
  }

  @Override
  public void removeBackendCoreInterceptor( IS5BackendCoreInterceptor aInterceptor ) {
    TsNullArgumentRtException.checkNull( aInterceptor );
    interceptors.remove( aInterceptor );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public void fireBackendMessage( GtMessage aMessage ) {
    TsNullArgumentRtException.checkNull( aMessage );
    IS5Transaction tx = transactionManager().findTransaction();
    if( tx == null ) {
      // Нет текущей транзакции, немедленная передача широковещательного сообщения всем фронтендам
      for( IS5FrontendRear frontend : attachedFrontends() ) {
        frontend.onBackendMessage( aMessage );
      }
      return;
    }
    // Размещение сообщений в текущей транзакции
    IListEdit<GtMessage> txMessages = tx.findResource( TX_FIRED_BACKEND_MESSAGES );
    if( txMessages == null ) {
      // Сообщений еще нет в транзакции. Создание нового списка и размещение его в транзакции
      txMessages = new ElemArrayList<>();
      tx.putResource( TX_FIRED_BACKEND_MESSAGES, txMessages );
      // Регистрируемся на получение событий об изменении состояния транзакции
      tx.addListener( new IS5TransactionListener() {

        @Override
        public void changeTransactionStatus( IS5Transaction aTransaction ) {
          if( aTransaction.getStatus() != ETransactionStatus.COMMITED ) {
            return;
          }
          // Размещение сообщений в текущей транзакции
          IList<GtMessage> txMessages2 = tx.getResource( TX_FIRED_BACKEND_MESSAGES, IList.EMPTY );
          for( GtMessage message : txMessages2 ) {
            for( IS5FrontendRear frontend : attachedFrontends() ) {
              frontend.onBackendMessage( message );
            }
          }
        }

      } );
    }
    txMessages.add( aMessage );
  }

  // ------------------------------------------------------------------------------------
  // ISkBackend
  //
  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public boolean isActive() {
    return (sharedConnection != null && !isClosed());
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IList<IS5FrontendRear> attachedFrontends() {
    return frontendsSupport.attachedFrontends();
  }

  @Override
  // 2021-02-13 mvk ---
  // @Asynchronous
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public void attachFrontend( IS5FrontendRear aFrontend ) {
    TsNullArgumentRtException.checkNull( aFrontend );
    frontendsSupport.attachFrontend( aFrontend );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public void detachFrontend( IS5FrontendRear aFrontend ) {
    TsNullArgumentRtException.checkNull( aFrontend );
    frontendsSupport.detachFrontend( aFrontend );
  }

  // ------------------------------------------------------------------------------------
  // IS5ServerJob
  //
  @AccessTimeout( value = CHECK_ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
  @TransactionAttribute( TransactionAttributeType.NOT_SUPPORTED )
  @Override
  public void doJob() {
    super.doJob();
    // Текущее время
    long currTime = System.currentTimeMillis();
    // Текущая информация по системе
    S5PlatformInfo pi = S5ServerPlatformUtils.getPlatformInfo();
    // Текущая загрузка сервера
    double la = pi.loadAverage();
    // Обновление писателя статистики если он определен
    S5StatisticWriter stat = statisticWriter;
    if( stat != null ) {
      stat.onEvent( STAT_BACKEND_NODE_LOAD_AVERAGE, avFloat( la ) );
      stat.onEvent( STAT_BACKEND_NODE_FREE_PHYSICAL_MEMORY, avFloat( pi.freePhysicalMemory() ) );
      stat.onEvent( STAT_BACKEND_NODE_MAX_HEAP_MEMORY, avFloat( pi.maxHeapMemory() ) );
      stat.onEvent( STAT_BACKEND_NODE_USED_HEAP_MEMORY, avFloat( pi.usedHeapMemory() ) );
      stat.onEvent( STAT_BACKEND_NODE_MAX_NON_HEAP_MEMORY, avFloat( pi.maxNonHeapMemory() ) );
      stat.onEvent( STAT_BACKEND_NODE_USED_NON_HEAP_MEMORY, avFloat( pi.usedNonHeapMemory() ) );
      stat.onEvent( STAT_BACKEND_NODE_OPEN_TX_MAX, avInt( txManager.openCount() ) );
      stat.onEvent( STAT_BACKEND_NODE_OPEN_SESSION_MAX, avInt( sessionManager().openSessionCount() ) );
      stat.update();
    }
    // Проверка условий перехода в режим OVERLOADED
    if( la < overloadAverage ) {
      overloadTimestamp = TimeUtils.MIN_TIMESTAMP;
    }
    else {
      if( overloadTimestamp == TimeUtils.MIN_TIMESTAMP ) {
        overloadTimestamp = currTime;
      }
    }
    boolean needOverload = (currTime - overloadTimestamp >= overloadDelay);
    // Обработка текущего состояния сервера
    switch( mode() ) {
      case LOADING:
        // doJob запрещен
        throw new TsInternalErrorRtException();
      case STARTING:
        if( currTime - modeTime >= startTimeMin && la < boostedAverage ) {
          setMode( WORKING );
        }
        if( currTime - modeTime >= startTimeMax ) {
          setMode( (la < boostedAverage ? WORKING : (needOverload ? OVERLOADED : BOOSTED)) );
        }
        break;
      case WORKING:
      case BOOSTED:
      case OVERLOADED:
        setMode( (la < boostedAverage ? WORKING : (needOverload ? OVERLOADED : BOOSTED)) );
        break;
      case SHUTDOWNING:
      case OFF:
        break;
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public boolean completed() {
    return (isClosing() || isClosed());
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Слушатель транзакций
   */
  private class IntervalStatisticListener
      implements IS5TransactionListener {

    // ------------------------------------------------------------------------------------
    // IS5TransactionListener
    //
    @Override
    public void changeTransactionStatus( IS5Transaction aTransaction ) {
      S5StatisticWriter stat = statisticWriter;
      if( stat == null ) {
        return;
      }
      switch( aTransaction.getStatus() ) {
        case COMMITED:
          stat.onEvent( STAT_BACKEND_NODE_COMMIT_TX, AV_1 );
          break;
        case ROLLEDBACK:
          stat.onEvent( STAT_BACKEND_NODE_ROLLBACK_TX, AV_1 );
          break;
        case ACTIVE:
        case PREPARED:
        case PREPARING:
        case COMMITTING:
        case MARKED_ROLLBACK:
        case ROLLING_BACK:
        case NO_TRANSACTION:
        case UNKNOWN:
          // nop
          break;
        default:
          throw new TsNotAllEnumsUsedRtException();
      }
    }
  }
}
