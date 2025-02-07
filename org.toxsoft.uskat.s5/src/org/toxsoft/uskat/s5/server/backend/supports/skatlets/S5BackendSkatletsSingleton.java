package org.toxsoft.uskat.s5.server.backend.supports.skatlets;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.bricks.wub.IWubConstants.*;
import static org.toxsoft.core.tslib.utils.plugins.IPluginsHardConstants.*;
import static org.toxsoft.core.tslib.utils.plugins.impl.PluginUtils.*;
import static org.toxsoft.uskat.core.devapi.ISkatlet.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.skatlets.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.skatlets.S5BackendSkatletsConfig.*;

import java.io.*;
import java.util.concurrent.*;

import javax.ejb.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.ctx.impl.*;
import org.toxsoft.core.tslib.bricks.wub.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.core.tslib.utils.plugins.impl.*;
import org.toxsoft.uskat.core.impl.*;
import org.toxsoft.uskat.core.impl.SkatletBox.*;
import org.toxsoft.uskat.s5.client.local.*;
import org.toxsoft.uskat.s5.server.backend.impl.*;
import org.toxsoft.uskat.s5.server.backend.supports.core.*;
import org.toxsoft.uskat.s5.utils.jobs.*;

/**
 * Реализация {@link IS5BackendSkatletsSingleton}.
 *
 * @author mvk
 */
@Startup
@Singleton
@LocalBean
@DependsOn( { //
    // Внимание! Чтобы использовать локальное соединение нужно минимальное описание системы для подключения (ISkUser):
    PROJECT_INITIAL_SYSDESCR_SINGLETON, //
    // Скатлеты используют локальные соединения
    LOCAL_CONNECTIION_SINGLETON //
} )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
// 2025-01-27 mvk TODO: ???
// @ConcurrencyManagement( ConcurrencyManagementType.CONTAINER )
@ConcurrencyManagement( ConcurrencyManagementType.BEAN )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@Lock( LockType.READ )
public class S5BackendSkatletsSingleton
    extends S5BackendSupportSingleton
    implements IS5BackendSkatletsSingleton, IS5ServerJob {

  private static final long serialVersionUID = 157157L;

  /**
   * Имя синглетона в контейнере сервера для организации зависимостей (@DependsOn)
   */
  public static final String BACKEND_SKATLET_BOX_ID = "S5BackendSkatletsSingleton"; //$NON-NLS-1$

  /**
   * Идентификатор корневого контейнера компонентов синглетона
   */
  public static final String ROOT_BOX_ID = "RootBox"; //$NON-NLS-1$

  /**
   * Идентификатор контейнера скатлетов
   */
  public static final String SKATLET_BOX_ID = "SkatletBox"; //$NON-NLS-1$

  /**
   * Тип обрабатываемх плагинов
   */
  private static final String PLUGIN_TYPE = "Skatlet"; //$NON-NLS-1$

  /**
   * Рабочий каталог скатлетов
   */
  private static final String SKATLETS_DIR = ".." + File.separator + "skatlets"; //$NON-NLS-1$ //$NON-NLS-2$

  /**
   * Подкаталог {@link #SKATLETS_DIR} размещения плагинов скатлетов (*-skatlet.jar)
   */
  private static final String SKATLETS_DEPLOYMENTS_DIR = SKATLETS_DIR + File.separator + "deployments"; //$NON-NLS-1$

  /**
   * Подкаталог {@link #SKATLETS_DIR} размещения временных файлов для работы {@link PluginBox}.
   */
  private static final String SKATLETS_TEMP_DIR = SKATLETS_DIR + File.separator + "temp"; //$NON-NLS-1$

  /**
   * Интервал выполнения doJob (мсек)
   */
  private static final long DOJOB_INTERVAL = 1000;

  /**
   * Время ожидания завершения работы контейнера (мсек)
   */
  private static final long SHUTDOWN_TIMEOUT = 60000;

  /**
   * Ядро сервера
   */
  @EJB
  private IS5BackendCoreSingleton backendCore;

  /**
   * Соединение с локальным сервером
   */
  @EJB
  private IS5LocalConnectionSingleton localConnectionSingleton;

  /**
   * Общее, разделяемое между модулями сервера, соединение
   */
  private SharedConnection sharedConnection;

  /**
   * Корневой контейнер компонентов синглетона
   */
  private WubBox rootBox;

  /**
   * Конструктор.
   */
  public S5BackendSkatletsSingleton() {
    super( BACKEND_SKATLET_BOX_ID, STR_D_BACKEND_SKATLETS );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendSupportSingleton
  //
  @Override
  protected void doInitSupport() {
    // Инициализация базового класса
    super.doInitSupport();

    // Проверка/настройка файловой системы
    createDirIfNotExist( SKATLETS_DIR );
    createDirIfNotExist( SKATLETS_DEPLOYMENTS_DIR );
    createDirIfNotExist( SKATLETS_TEMP_DIR );

    // Конфигурация синглетона
    IOptionSet configuration = configuration();

    // Параметры создания корневого контейнера и контейнера скатлетов
    IOptionSetEdit params = new OptionSet();
    OPDEF_UNIT_STOPPING_TIMEOUT_MSECS.setValue( params, avInt( SHUTDOWN_TIMEOUT ) );

    // Контекст для инициализации контейнера скатлетов
    ITsContext environ = createContext( configuration );
    PLUGIN_TYPE_ID.setValue( environ.params(), avStr( PLUGIN_TYPE ) );
    PLUGINS_DIR.setValue( environ.params(), avValobj( new StringArrayList( SKATLETS_DEPLOYMENTS_DIR ) ) );
    TMP_DIR.setValue( environ.params(), avStr( SKATLETS_TEMP_DIR ) );
    CLEAN_TMP_DIR.setValue( environ.params(), avBool( true ) );
    REF_SKATLET_SUPPORT.setRef( environ, new S5BackendSkatletSupport( localConnectionSingleton, logger() ) );
    OPDEF_SKATLETS_LOAD_ORDER.setValue( params, SKATLETS_LOAD_ORDERSKATLETS_LOAD_ORDER.getValue( configuration ) );

    // Создание корневого контейнера...
    rootBox = new WubBox( ROOT_BOX_ID, params );
    // Добавление в корневой контейнер контейнера скатлетов
    rootBox.addUnit( new SkatletBox( SKATLET_BOX_ID, params ) {

      @Override
      protected void doCreateSharedConnection( SharedConnection aConnection ) {
        // Создание общего (разделяемого между модулями системы) соединения
        sharedConnection = aConnection;
        // Установка общего(разделяемого между модулями) соединения в ядре и определяя его готовность к использованию
        backendCore.setSharedConnection( sharedConnection );
      }

    } );
    // ...инициализация...
    rootBox.init( environ );
    // ...запуск
    rootBox.start();
    // Запуск doJob
    addOwnDoJob( DOJOB_INTERVAL );
  }

  @Override
  protected void doCloseSupport() {
    synchronized (rootBox) {
      rootBox.queryStop();
      while( !rootBox.isStopped() ) {
        doJob();
        try {
          Thread.sleep( DOJOB_INTERVAL );
        }
        catch( InterruptedException ex ) {
          LoggerUtils.errorLogger().error( ex );
        }
      }
    }
    sharedConnection.shutdown();
  }

  @Override
  protected IStringList doConfigurationPaths() {
    return new StringArrayList( S5BackendSkatletsConfig.SYBSYSTEM_ID_PREFIX );
  }

  // ------------------------------------------------------------------------------------
  // IS5BackendSkatletsSingleton
  //

  // ------------------------------------------------------------------------------------
  // IS5ServerJob
  //
  @Override
  public void doJob() {
    // Обработка корневого контейнера
    if( !rootBox.isStopped() ) {
      synchronized (rootBox) {
        rootBox.doJob();
      }
    }
    // Вывод журнала
    logger().debug( MSG_DOJOB );
  }

  // ------------------------------------------------------------------------------------
  // private methods
  //
  private static ITsContext createContext( IOptionSet aConfiguration ) {
    TsNullArgumentRtException.checkNull( aConfiguration );
    ITsContext ctx = new TsContext( new IAskParent() {

      @Override
      public IAtomicValue findOp( String aId ) {
        return aConfiguration.findByKey( aId );
      }

      @Override
      public Object findRef( String aKey ) {
        return null;
      }

    } );
    return ctx;
  }
}
