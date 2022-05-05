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
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.IS5BackendCoreSingleton;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddon;
import org.toxsoft.uskat.s5.server.backend.supports.objects.IS5BackendObjectsSingleton;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterCommandHandler;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterManager;
import org.toxsoft.uskat.s5.server.sessions.S5BackendAddonLocal;
import org.toxsoft.uskat.s5.server.singletons.S5SingletonBase;
import org.toxsoft.uskat.s5.utils.jobs.IS5ServerJob;

import ru.uskat.backend.ISkFrontendRear;
import ru.uskat.common.dpu.IDpuObject;
import ru.uskat.core.api.ISkBackend;
import ru.uskat.core.api.users.ISkUser;
import ru.uskat.core.connection.ISkConnection;
import ru.uskat.core.impl.SkUtils;

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
    return open( aModuleName, TsLibUtils.EMPTY_STRING, new ReentrantReadWriteLock() );
  }

  @TransactionAttribute( TransactionAttributeType.NOT_SUPPORTED )
  @Override
  public ISkConnection open( String aModuleName, String aExtServiceProvider ) {
    return open( aModuleName, aExtServiceProvider, new ReentrantReadWriteLock() );
  }

  @TransactionAttribute( TransactionAttributeType.NOT_SUPPORTED )
  @SuppressWarnings( "nls" )
  @Override
  public ISkConnection open( String aModuleName, String aExtServiceProvider, ReentrantReadWriteLock aLock ) {
    TsNullArgumentRtException.checkNull( aModuleName, aExtServiceProvider, aLock );
    IS5LocalConnectionProviderSingleton provider =
        sessionContext().getBusinessObject( IS5LocalConnectionProviderSingleton.class );
    // Пробуем найти пользователя root и получить его пароль для подключения
    IDpuObject user = objectsBackendSupport.findObject( new Skid( ISkUser.CLASS_ID, ISkUser.ROOT_USER_LOGIN ) );
    // Узел кластера на котором работает модуль
    String moduleName = aModuleName.replaceAll( "-", "." );
    // Узел кластера на котором работает модуль
    String moduleNode = clusterManager.group().getLocalMember().getName().replaceAll( "-", "." );

    ITsContext ctx = new TsContext();
    SkUtils.REF_BACKEND_PROVIDER.setRef( ctx, provider );
    SkUtils.OP_LOGIN.setValue( ctx.params(), avStr( ISkUser.ROOT_USER_LOGIN ) );
    SkUtils.OP_PASSWORD.setValue( ctx.params(), avStr( TsLibUtils.EMPTY_STRING ) );
    if( user != null ) {
      // Пользователь root уже существует (есть sysdescr), определяем его текущий пароль
      SkUtils.OP_PASSWORD.setValue( ctx.params(), avStr( user.attrs().getStr( ISkUser.ATRID_PASSWORD ) ) );
    }
    if( aExtServiceProvider.length() > 0 ) {
      // Для создания соединения используется инициализатор
      SkUtils.OP_EXT_SERV_PROVIDER_CLASS.setValue( ctx.params(), avStr( aExtServiceProvider ) );
    }
    IS5LocalBackendHardConstants.OP_LOCAL_MODULE.setValue( ctx.params(), avStr( moduleName ) );
    IS5LocalBackendHardConstants.OP_LOCAL_NODE.setValue( ctx.params(), avStr( moduleNode ) );

    ISkConnection connection = SkUtils.createConnection( aLock );
    connection.open( ctx );
    return connection;
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5LocalConnectionProviderSingleton
  //
  @Override
  public ISkBackend createBackend( ISkFrontendRear aFrontend, ITsContextRo aArgs ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aArgs );
    IStridablesListEdit<S5BackendAddonLocal> addonLocalClients = new StridablesList<>();
    // Доступные расширения бекенда предоставляемые сервером
    IStridablesList<IS5BackendAddon> addons = backend.initialConfig().impl().getBackendAddonsProvider().addons();
    for( IS5BackendAddon addon : addons ) {
      S5BackendAddonLocal addonLocalClient = addon.createLocalClient( aArgs );
      if( addonLocalClient == null ) {
        // Расширение не работает с локальными клиентами
        continue;
      }
      addonLocalClients.put( addonLocalClient );
    }
    // Создание локального бекенда
    return new S5LocalBackend( aArgs, backend, aFrontend, addonLocalClients );
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

  // ------------------------------------------------------------------------------------
  // Шаблонные методы
  //
  /**
   * Возвращает список расширений бекендов необходимых наследнику
   *
   * @return {@link IStridablesList}&lt;{@link S5BackendAddonLocal}&gt; список расширений
   */
  protected IStridablesList<S5BackendAddonLocal> doGetAddons() {
    return IStridablesList.EMPTY;
  }
}
