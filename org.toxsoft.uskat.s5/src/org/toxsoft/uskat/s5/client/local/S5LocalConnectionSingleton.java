package org.toxsoft.uskat.s5.client.local;

import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.s5.client.local.IS5Resources.*;
import static org.toxsoft.uskat.s5.client.local.S5ClusterCommandWhenObjectsChanged.*;
import static org.toxsoft.uskat.s5.client.local.S5ClusterCommandWhenSysdescrChanged.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;

import java.util.concurrent.*;

import javax.ejb.*;
import javax.enterprise.concurrent.*;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.ctx.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.synch.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.metainf.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.*;
import org.toxsoft.uskat.s5.client.*;
import org.toxsoft.uskat.s5.server.backend.addons.*;
import org.toxsoft.uskat.s5.server.backend.supports.core.*;
import org.toxsoft.uskat.s5.server.cluster.*;
import org.toxsoft.uskat.s5.server.singletons.*;
import org.toxsoft.uskat.s5.server.startup.*;
import org.toxsoft.uskat.s5.utils.jobs.*;

/**
 * Реализация синглтона {@link IS5LocalConnectionSingleton}.
 * <p>
 *
 * @author mvk
 */
@Startup
@Singleton
@LocalBean
@DependsOn( { //
    // Для работы соединений должна быть настроена работа с расширениями (skf/libs)
    PROJECT_INITIAL_IMPLEMENT_SINGLETON,
    // Поддержка событий ядра
    BACKEND_CORE_SINGLETON,
    // Управление сессиями соединений
    SESSION_MANAGER_SINGLETON, //
} )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
@ConcurrencyManagement( ConcurrencyManagementType.CONTAINER )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@Lock( LockType.READ )
// @RunAsPrincipal( "root" ) // @RunAs( "guest,user, PowerUser" )
// @SecurityDomain("mm_security_domain" )
public class S5LocalConnectionSingleton
    extends S5SingletonBase
    implements IS5LocalConnectionSingleton, IS5LocalConnectionProviderSingleton, IS5ServerJob {

  private static final long serialVersionUID = 157157L;

  /**
   * JNDI-имя исполнителя асинхронных задач записи блоков {@link ManagedExecutorService}
   */
  public static final String EXECUTOR_JNDI = "java:jboss/ee/concurrency/executor/s5/uskat/api"; //$NON-NLS-1$

  /**
   * Имя синглетона в контейнере сервера для организации зависимостей (@DependsOn)
   */
  public static final String LOCAL_CONNECTION_ID = "S5LocalConnectionSingleton"; //$NON-NLS-1$

  /**
   * Таймаут(мсек) между выполнением фоновых задач синглетона
   */
  private static final long DO_JOB_TIMEOUT = 1000;

  /**
   * Менеджер управления кластером s5-сервера
   */
  @EJB
  private IS5ClusterManager clusterManager;

  /**
   * s5-backend предоставляемый сервером
   */
  @EJB
  private IS5BackendCoreSingleton backend;

  /**
   * s5-backend предоставляемый сервером
   */
  // @EJB
  // private IS5BackendObjectsSingleton objectsBackendSupport;

  /**
   * Обработчик команд кластера: изменение описания системы
   */
  private IS5ClusterCommandHandler whenSysdecrChangedHandler;

  /**
   * Обработчик команд кластера: изменение/удаление объектов системы
   */
  private IS5ClusterCommandHandler whenObjectsChangedHandler;

  /**
   * Карта синхронизаторов соединений которые должны быть заменены на фоновые.
   * <p>
   * Ключ: имя соединения (потока). Значение: синхронизатор
   */
  private IMapEdit<String, TsThreadExecutor> initExecutors = new SynchronizedMap<>( new ElemMap<>() );

  /**
   * Метка времени последнего выполнения doJob
   */
  private long doJobTimestamp;

  /**
   * Конструктор
   */
  public S5LocalConnectionSingleton() {
    super( LOCAL_CONNECTION_ID, STR_D_LOCAL_CONNECTION );
    // Настройка doJob-задачи
    long currTime = System.currentTimeMillis();
    doJobTimestamp = currTime;
  }

  // ------------------------------------------------------------------------------------
  // Определение шаблонных методов S5SingletonBase
  //
  @Override
  protected void doInit() {
    // Поиск исполнителя потоков объединения блоков
    // TODO: 2023-03-14 mvk ???
    // executorService = S5ServiceSingletonUtils.lookupExecutor( EXECUTOR_JNDI );
    // executorService = Executors.newFixedThreadPool( 1024 );
    // Прием сообщений кластера
    whenSysdecrChangedHandler = new S5ClusterCommandWhenSysdescrChanged( backend );
    whenObjectsChangedHandler = new S5ClusterCommandWhenObjectsChanged( backend );
    clusterManager.addCommandHandler( WHEN_SYSDESCR_CHANGED_METHOD, whenSysdecrChangedHandler );
    clusterManager.addCommandHandler( WHEN_OBJECTS_CHANGED_METHOD, whenObjectsChangedHandler );
    // Запуск фоновой задачи
    addOwnDoJob( DO_JOB_TIMEOUT );
  }

  @Override
  protected void doClose() {
    // Завершение приема сообщений кластера
    clusterManager.removeCommandHandler( WHEN_SYSDESCR_CHANGED_METHOD, whenSysdecrChangedHandler );
    clusterManager.removeCommandHandler( WHEN_OBJECTS_CHANGED_METHOD, whenObjectsChangedHandler );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5LocalConnectionSingleton
  //
  @TransactionAttribute( TransactionAttributeType.NOT_SUPPORTED )
  @Override
  public ISkConnection open( String aModuleName ) {
    return open( aModuleName, new TsContext() );
  }

  @TransactionAttribute( TransactionAttributeType.NOT_SUPPORTED )
  @SuppressWarnings( "nls" )
  @Override
  public ISkConnection open( String aModuleName, ITsContextRo aArgs ) {
    TsNullArgumentRtException.checkNulls( aModuleName, aArgs );
    IS5LocalConnectionProviderSingleton provider =
        sessionContext().getBusinessObject( IS5LocalConnectionProviderSingleton.class );
    // Узел кластера на котором работает модуль
    String moduleName = aModuleName.replaceAll( "-", "." );
    // Узел кластера на котором работает модуль
    String moduleNode = clusterManager.group().getLocalMember().getName().replaceAll( "-", "." );

    ITsContext ctx = new TsContext( aArgs );
    ISkCoreConfigConstants.REFDEF_BACKEND_PROVIDER.setRef( ctx, provider );
    IS5ConnectionParams.OP_USERNAME.setValue( ctx.params(), avStr( ISkUserServiceHardConstants.USER_ID_ROOT ) );
    IS5ConnectionParams.OP_PASSWORD.setValue( ctx.params(), avStr( TsLibUtils.EMPTY_STRING ) );
    IS5ConnectionParams.OP_LOCAL_MODULE.setValue( ctx.params(), avStr( moduleName ) );
    IS5ConnectionParams.OP_LOCAL_NODE.setValue( ctx.params(), avStr( moduleNode ) );
    IS5ConnectionParams.OP_CURRDATA_TIMEOUT.setValue( ctx.params(), avInt( 10 ) );
    IS5ConnectionParams.OP_HISTDATA_TIMEOUT.setValue( ctx.params(), avInt( 10000 ) );

    // Текущий поток используется только для открытия соединения
    TsThreadExecutor executor =
        new TsThreadExecutor( getClass().getSimpleName(), Thread.currentThread(), getLogger( TsThreadExecutor.class ) );
    ISkCoreConfigConstants.REFDEF_THREAD_EXECUTOR.setRef( ctx, executor );

    // Имя соединения
    String connectionName = "locConn[" + moduleName + "@" + moduleNode + "]";
    // Создание соединения
    ISkConnection connection = SkCoreUtils.createConnection();
    connection.open( ctx );
    // Сохранение соединения в списке открытых соединений
    initExecutors.put( connectionName, executor );

    // debugConnection = connection;

    return connection;
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5LocalConnectionProviderSingleton
  //
  @Override
  public ISkBackendMetaInfo getMetaInfo() {
    SkBackendMetaInfo retValue = new SkBackendMetaInfo( id(), nmName(), description(), ESkAuthentificationType.NONE );
    retValue.argOps().add( IS5ConnectionParams.OP_LOCAL_NODE );
    retValue.argOps().add( IS5ConnectionParams.OP_LOCAL_MODULE );
    retValue.argOps().add( IS5ConnectionParams.OP_CONNECT_TIMEOUT );
    retValue.argOps().add( IS5ConnectionParams.OP_FAILURE_TIMEOUT );
    retValue.argOps().add( IS5ConnectionParams.OP_CURRDATA_TIMEOUT );
    retValue.argOps().add( IS5ConnectionParams.OP_HISTDATA_TIMEOUT );
    return retValue;
  }

  /**
   * Следующий код ({@link #createBackend(ISkFrontendRear, ITsContextRo)}) решает следующую задачу: На разных этапах
   * запуска, разные подсистемы могут создавать локальные соединение к серверу.
   * <p>
   * Первым подключением является подключение для проверки (и если необходимо создания) системного описания (sysdescr).
   * При этом для этого подключения требуются только службы ядра ISkSysdescr, ISkObjectService, ISkLinkService.
   * <p>
   * На более поздних этапах загрузки локальное соединение может быть расширено skf-функциональностью, например,
   * S5GatewaySingleton.
   * <p>
   * Чтобы обеспечить в возвращаемом бекенде наличие соответствующей поддержки (синглетона) службы, при разработке
   * синглетонов поддержки служб {@link ISkService} необходимо использовать механизм зависимостей между синглетонами
   * {@link DependsOn}.
   * <p>
   * Например, если некоторая служба(или ее поддержка) использует ISkDataQualityService, то синглетон ее поддержки
   * должен иметь следующую зависимость:<br>
   * <code>@DependsOn( { ...,S5BackendDataQualitySingleton.BACKEND_DATA_QUALITY_ID, ... } )</code>.
   * <p>
   * Этот код позволяет создавать локальные соединение по стратегии "то что уже есть на данный момент, то что уже
   * загружено".
   */
  @Override
  public ISkBackend createBackend( ISkFrontendRear aFrontend, ITsContextRo aArgs ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aArgs );
    // Доступные расширения бекенда предоставляемые сервером
    IStringList availableSupportIds = backend.listSupportIds();
    IS5InitialImplementSingleton initialSingleton = backend.initialConfig();
    IS5InitialImplementation initialImplementation = initialSingleton.impl();

    IStridablesListEdit<IS5BackendAddonCreator> availableCreators = new StridablesList<>();
    for( IS5BackendAddonCreator creator : initialImplementation.baCreators() ) {
      if( isAvailableAddon( availableSupportIds, creator ) ) {
        availableCreators.add( creator );
      }
    }
    // Создание локального бекенда
    return new S5BackendLocal( aFrontend, aArgs, backend, availableCreators );
  }

  // ------------------------------------------------------------------------------------
  // S5SingletonBase
  //
  @Override
  public void doJob() {
    super.doJob();
    // Замена на штатный исполнитель потоков
    for( String connectionName : initExecutors.keys().copyTo( new ElemArrayList<>() ) ) {
      TsThreadExecutor threadExecutor = initExecutors.getByKey( connectionName );
      // TODO: !!! 2025-02-25 mvk при работе с шлюзами в проекте valcom появилось подозрение что executorService может
      // подменять рабочий поток у соединения что может приводить к 'invalid thread access' в соединении.
      // threadExecutor.setExecutor( executorService );
      threadExecutor.setThread( null );
      initExecutors.removeByKey( connectionName );
    }

    // Текущее время
    long currTime = System.currentTimeMillis();
    // if( currTime - doJobTimestamp < SECOND.interval() ) {
    if( currTime - doJobTimestamp < 1000 ) {
      // Фоновая работа проводится не раньше чем раз в секунду
      return;
    }
    doJobTimestamp = currTime;
  }

  // ------------------------------------------------------------------------------------
  // private methods
  //
  /**
   * Проверяет доступность поддержки расширение бекенда в ядре системы
   *
   * @param aSupportIds {@link IStringList} список идентификаторов синглетонов поддержки расширений ядра поддерживаемых
   *          на текущий момент времени
   * @param aCreator {@link IS5BackendAddonCreator} построитель расширения бекенда.
   * @return boolean <b>true</b> расширение доступно и может быть использовано. <b>false</b> расширение не доступно.
   */
  private boolean isAvailableAddon( IStringList aSupportIds, IS5BackendAddonCreator aCreator ) {
    TsNullArgumentRtException.checkNulls( aSupportIds, aCreator );
    for( String supportId : aCreator.supportSingletonIds() ) {
      if( !aSupportIds.hasElem( supportId ) ) {
        return false;
      }
      for( IS5BackendAddonCreator depend : aCreator.depends() ) {
        if( !isAvailableAddon( aSupportIds, depend ) ) {
          return false;
        }
      }
    }
    return true;
  }
}
