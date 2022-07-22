package org.toxsoft.uskat.s5.client.local;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.s5.client.local.IS5Resources.*;
import static org.toxsoft.uskat.s5.client.local.S5ClusterCommandWhenObjectsChanged.*;
import static org.toxsoft.uskat.s5.client.local.S5ClusterCommandWhenSysdescrChanged.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.ejb.*;

import org.toxsoft.core.tslib.bricks.ctx.ITsContext;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.ctx.impl.TsContext;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.concurrent.S5SynchronizedConnection;
import org.toxsoft.uskat.core.api.users.ISkUserServiceHardConstants;
import org.toxsoft.uskat.core.backend.ISkBackend;
import org.toxsoft.uskat.core.backend.ISkFrontendRear;
import org.toxsoft.uskat.core.connection.ISkConnection;
import org.toxsoft.uskat.core.impl.ISkCoreConfigConstants;
import org.toxsoft.uskat.core.impl.SkCoreUtils;
import org.toxsoft.uskat.s5.client.IS5ConnectionParams;
import org.toxsoft.uskat.s5.server.backend.IS5BackendCoreSingleton;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonCreator;
import org.toxsoft.uskat.s5.server.backend.supports.objects.IS5BackendObjectsSingleton;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterCommandHandler;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterManager;
import org.toxsoft.uskat.s5.server.singletons.S5SingletonBase;
import org.toxsoft.uskat.s5.utils.jobs.IS5ServerJob;

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
// BACKEND_SYSDESCR_SINGLETON, // уже включено неявным образом
// BACKEND_OBJECTS_SINGLETON, // уже включено неявным образом
// BACKEND_LINKS_SINGLETON, // уже включено неявным образом
// BACKEND_LOBS_SINGLETON, //уже включено неявным образом
// BACKEND_EVENTS_SINGLETON, //уже включено неявным образом
// BACKEND_COMMANDS_SINGLETON, //уже включено неявным образом
// 2022-05-04 mvk события в ядре
// BACKEND_RTDATA_SINGLETON
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
  @EJB
  private IS5BackendObjectsSingleton objectsBackendSupport;

  /**
   * Обработчик команд кластера: изменение описания системы
   */
  private IS5ClusterCommandHandler whenSysdecrChangedHandler;

  /**
   * Обработчик команд кластера: изменение/удаление объектов системы
   */
  private IS5ClusterCommandHandler whenObjectsChangedHandler;

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
    return open( aModuleName, new TsContext(), new ReentrantReadWriteLock() );
  }

  @TransactionAttribute( TransactionAttributeType.NOT_SUPPORTED )
  @SuppressWarnings( "nls" )
  @Override
  public ISkConnection open( String aModuleName, ITsContextRo aArgs, ReentrantReadWriteLock aLock ) {
    TsNullArgumentRtException.checkNulls( aModuleName, aArgs, aLock );
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

    ISkConnection connection =
        S5SynchronizedConnection.createSynchronizedConnection( SkCoreUtils.createConnection(), aLock );
    connection.open( ctx );
    return connection;
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5LocalConnectionProviderSingleton
  //
  @Override
  public ISkBackend createBackend( ISkFrontendRear aFrontend, ITsContextRo aArgs ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aArgs );
    // Доступные расширения бекенда предоставляемые сервером
    IStridablesList<IS5BackendAddonCreator> baCreators = backend.initialConfig().impl().baCreators();
    // Создание локального бекенда
    return new S5BackendLocal( aFrontend, aArgs, backend, baCreators );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5ServerJob
  //
  @Override
  public void doJob() {
    long currTime = System.currentTimeMillis();
    // if( currTime - doJobTimestamp < SECOND.interval() ) {
    if( currTime - doJobTimestamp < 1000 ) {
      // Фоновая работа проводится не раньше чем раз в секунду
      return;
    }
    doJobTimestamp = currTime;
    // TODO:
  }
}
