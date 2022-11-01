package org.toxsoft.uskat.s5.server.backend.impl;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.utils.logs.ELogSeverity.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.IS5ServerHardConstants.*;
import static org.toxsoft.uskat.s5.server.backend.impl.IS5BackendCoreInterceptor.*;
import static org.toxsoft.uskat.s5.server.backend.impl.IS5Resources.*;
import static org.toxsoft.uskat.s5.utils.platform.S5ServerPlatformUtils.*;

import java.sql.*;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.sql.DataSource;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.coll.synch.SynchronizedStringMap;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.classes.IS5ClassNode;
import org.toxsoft.uskat.core.backend.api.ISkBackendInfo;
import org.toxsoft.uskat.core.connection.ISkConnection;
import org.toxsoft.uskat.s5.common.S5Module;
import org.toxsoft.uskat.s5.server.IS5ServerHardConstants;
import org.toxsoft.uskat.s5.server.backend.IS5BackendCoreSingleton;
import org.toxsoft.uskat.s5.server.backend.IS5BackendSupportSingleton;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonCreator;
import org.toxsoft.uskat.s5.server.backend.supports.clobs.IS5BackendClobsSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.links.IS5BackendLinksSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.objects.IS5BackendObjectsSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.IS5BackendSysDescrSingleton;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterManager;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.interceptors.S5InterceptorSupport;
import org.toxsoft.uskat.s5.server.sessions.IS5SessionManager;
import org.toxsoft.uskat.s5.server.singletons.S5SingletonBase;
import org.toxsoft.uskat.s5.server.startup.IS5InitialImplementSingleton;
import org.toxsoft.uskat.s5.server.statistics.IS5StatisticCounter;
import org.toxsoft.uskat.s5.server.statistics.S5StatisticWriter;
import org.toxsoft.uskat.s5.server.transactions.*;
import org.toxsoft.uskat.s5.utils.jobs.IS5ServerJob;
import org.toxsoft.uskat.s5.utils.platform.S5PlatformInfo;
import org.toxsoft.uskat.s5.utils.platform.S5ServerPlatformUtils;
import org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable;

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
    // 2022-08-21 mvk
    // SESSION_MANAGER_SINGLETON, //
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
   * Источники(persitent context + transaction + EntityManager):
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
  private final S5FrontendsSupport frontendsSupport = new S5FrontendsSupport();

  /**
   * Поддержка интерсепторов операций проводимых над объектами
   */
  private final S5InterceptorSupport<IS5BackendCoreInterceptor> interceptors = new S5InterceptorSupport<>();

  /**
   * Информация о бекенде
   */
  private S5BackendInfo backendInfo;

  /**
   * Соединение с сервером
   */
  private ISkConnection connection;

  /**
   * Писатель статитистики объекта {@link IS5ClassNode}. null: нет соединения
   */
  private S5StatisticWriter statisticWriter;

  /**
   * Время (мсек с начала эпохи) перехода синглетона/сервера в режим перегрузки данными
   */
  private long overloadModeStartTime;

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
    // Параметры бекенда
    IOptionSet backendConfigParams = initialConfig.impl().params();
    // Модуль реализующий бекенд
    S5Module module = OP_BACKEND_MODULE.getValue( backendConfigParams ).asValobj();

    // Описание бекенда
    backendInfo = new S5BackendInfo( module.id(), backendConfigParams );
    // Время запуска сервера
    IS5ServerHardConstants.OP_BACKEND_START_TIME.setValue( backendInfo.params(),
        avTimestamp( System.currentTimeMillis() ) );
    // Переход в режим "overload"
    startOverloadMode();
    // Установка режима работы с кэшем сессий (infinispan)
    // ConfigurationBuilder builder = new ConfigurationBuilder();
    // // builder.transaction().lockingMode( LockingMode.PESSIMISTIC );
    // builder.transaction().lockingMode( LockingMode.OPTIMISTIC );

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

  // ------------------------------------------------------------------------------------
  // IS5BackendCoreSingleton
  //
  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public ISkBackendInfo getInfo() {
    // Размещение текущей информации о сервере (backend)
    try {
      OP_BACKEND_ZONE_ID.setValue( backendInfo.params(), avStr( ZoneId.systemDefault().getId() ) );
      OP_BACKEND_CURRENT_TIME.setValue( backendInfo.params(), avTimestamp( System.currentTimeMillis() ) );
      OP_BACKEND_SESSIONS_INFOS.setValue( backendInfo.params(), avValobj( sessionManager().getInfos() ) );
      OP_BACKEND_TRANSACTIONS_INFOS.setValue( backendInfo.params(), avValobj( txManager.getInfos() ) );
      OP_BACKEND_HEAP_MEMORY_USAGE.setValue( backendInfo.params(), avStr( printHeapMemoryUsage() ) );
      OP_BACKEND_NON_HEAP_MEMORY_USAGE.setValue( backendInfo.params(), avStr( printNonHeapMemoryUsage() ) );
      OP_BACKEND_PLATFORM_INFO.setValue( backendInfo.params(), avStr( printOperatingSystemDetails() ) );
    }
    catch( Throwable e ) {
      logger().error( e );
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
  public void add( String aSupportId, IS5BackendSupportSingleton aSupportInterface ) {
    TsNullArgumentRtException.checkNulls( aSupportId, aSupportInterface );
    if( backendSupports.hasKey( aSupportId ) ) {
      // Поддержка уже зарегистрирована
      throw new TsIllegalArgumentRtException( ERR_SUPPORT_ALREADY_REGISTER, aSupportId );
    }
    backendSupports.put( aSupportId, aSupportInterface );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public void remove( String aSupportId ) {
    TsNullArgumentRtException.checkNull( aSupportId );
    backendSupports.removeByKey( aSupportId );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public <T extends IS5BackendSupportSingleton> T get( String aSupportId, Class<T> aSupportInterface ) {
    TsNullArgumentRtException.checkNulls( aSupportId, aSupportInterface );
    try {
      int waitCount = 0;
      long startTime = System.currentTimeMillis();
      while( !isReadySupportSingletons() ) {
        if( System.currentTimeMillis() - startTime > SUPPORT_READY_TIMEOUT ) {
          throw new TsInternalErrorRtException( "supports not ready" ); //$NON-NLS-1$
        }
        logger().warning( "Waiting load support singletons needs for backend implementation. %d", //$NON-NLS-1$
            Integer.valueOf( waitCount++ ) );
        Thread.sleep( SUPPORT_READY_CHECK_INTERVAL );
      }
      return aSupportInterface.cast( backendSupports.getByKey( aSupportId ) );
    }
    catch( Exception ex ) {
      throw new TsIllegalArgumentRtException( ex, ex.getMessage() );
    }
  }

  @Override
  public ISkConnection getConnectionOrNull() {
    return connection;
  }

  @Override
  public void setConnection( ISkConnection aConnection ) {
    TsNullArgumentRtException.checkNull( aConnection );
    ISkConnection oldConnection = connection;
    // Пред-интерсепция
    if( !callBeforeSetConnectionInterceptors( interceptors, oldConnection, aConnection ) ) {
      // Интерспоторы запретили выполнение операции
      return;
    }
    connection = aConnection;
    // Пост-интерсепция
    try {
      callAfterSetConnectionInterceptors( interceptors, oldConnection, aConnection );
    }
    catch( Throwable e ) {
      // Восстановление состояния на любой ошибке
      connection = oldConnection;
      throw e;
    }
    if( statisticWriter != null ) {
      // Завершение работы предыдущего писателя статистики
      statisticWriter.close();
    }
    // Информация о бекенде
    ISkBackendInfo info = getInfo();
    // Идентификатор узла сервера
    Skid nodeId = OP_BACKEND_NODE_ID.getValue( info.params() ).asValobj();
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

  // ------------------------------------------------------------------------------------
  // ISkBackend
  //
  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public boolean isActive() {
    return !isClosed();
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
    // Обновление писателя статистики если он определен
    S5StatisticWriter stat = statisticWriter;
    if( stat != null ) {
      S5PlatformInfo pi = S5ServerPlatformUtils.getPlatformInfo();
      IAtomicValue loadAverage = avFloat( pi.loadAverage() );
      stat.onEvent( STAT_BACKEND_NODE_LOAD_AVERAGE, loadAverage );
      stat.onEvent( STAT_BACKEND_NODE_LOAD_MAX, loadAverage );
      stat.onEvent( STAT_BACKEND_NODE_FREE_PHYSICAL_MEMORY, avFloat( pi.freePhysicalMemory() ) );
      stat.onEvent( STAT_BACKEND_NODE_MAX_HEAP_MEMORY, avFloat( pi.maxHeapMemory() ) );
      stat.onEvent( STAT_BACKEND_NODE_USED_HEAP_MEMORY, avFloat( pi.usedHeapMemory() ) );
      stat.onEvent( STAT_BACKEND_NODE_MAX_NON_HEAP_MEMORY, avFloat( pi.maxNonHeapMemory() ) );
      stat.onEvent( STAT_BACKEND_NODE_USED_NON_HEAP_MEMORY, avFloat( pi.usedNonHeapMemory() ) );
      stat.onEvent( STAT_BACKEND_NODE_OPEN_TX_MAX, avInt( txManager.openCount() ) );
      stat.onEvent( STAT_BACKEND_NODE_OPEN_SESSION_MAX, avInt( sessionManager().openSessionCount() ) );
      stat.update();
    }
    if( overloadModeStartTime > 0 && currTime - overloadModeStartTime > STARTUP_OVERLOAD_TIMEOUT ) {
      // Завершение режима перегрузки данными
      endOverloadMode();
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
   * Запуск режима перегрузки данными
   */
  private void startOverloadMode() {
    if( overloadModeStartTime > 0 ) {
      // Режим уже запущен
      return;
    }
    overloadModeStartTime = System.currentTimeMillis();
    // 2020-07-23 mvk нужно ли это ??? (из-за этого долго искал deadlock)
    // S5Lockable.setAccessTimeoutDefault( STARTUP_OVERLOAD_TIMEOUT );
    logger().info( MSG_START_OVERLOAD, Long.valueOf( STARTUP_OVERLOAD_TIMEOUT ) );
  }

  /**
   * Выход из режима перегрузки данными
   */
  private void endOverloadMode() {
    if( overloadModeStartTime <= 0 ) {
      // Режим уже остановлен
      return;
    }
    overloadModeStartTime = 0;
    S5Lockable.setAccessTimeoutDefault( ACCESS_TIMEOUT_DEFAULT );
    logger().info( MSG_END_OVERLOAD );
  }

  /**
   * Проверяет, все ли сиглетоны поддержки необходимые для реализации бекенда, завершили загрузку
   *
   * @return boolean <b>true</b> все синглетоны завершили загрузку. <b>false</b> не все синглетоны завершили загрузку
   */
  private boolean isReadySupportSingletons() {
    IStringList singletonIds = backendSupports.keys();
    for( IS5BackendAddonCreator creator : initialConfig.impl().baCreators() ) {
      for( String singletonId : creator.supportSingletonIds() ) {
        if( !singletonIds.hasElem( singletonId ) ) {
          return false;
        }
      }
    }
    return true;
  }

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
