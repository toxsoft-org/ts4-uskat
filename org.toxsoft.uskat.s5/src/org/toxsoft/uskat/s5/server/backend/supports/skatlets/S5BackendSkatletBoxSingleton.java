package org.toxsoft.uskat.s5.server.backend.supports.skatlets;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.bricks.wub.IWubConstants.*;
import static org.toxsoft.core.tslib.utils.plugins.IPluginsHardConstants.*;
import static org.toxsoft.uskat.core.devapi.ISkatlet.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.skatlets.IS5Resources.*;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.ejb.*;

import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.bricks.ctx.impl.TsContext;
import org.toxsoft.core.tslib.bricks.wub.WubBox;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.utils.logs.impl.LoggerUtils;
import org.toxsoft.uskat.core.connection.ISkConnection;
import org.toxsoft.uskat.core.impl.SkatletBox;
import org.toxsoft.uskat.s5.client.local.IS5LocalConnectionSingleton;
import org.toxsoft.uskat.s5.server.backend.impl.S5BackendSupportSingleton;
import org.toxsoft.uskat.s5.utils.jobs.IS5ServerJob;

/**
 * Реализация {@link IS5BackendSkatletBoxSingleton}.
 *
 * @author mvk
 */
@Startup
@Singleton
@LocalBean
@DependsOn( { //
    LOCAL_CONNECTIION_SINGLETON //
} )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
@ConcurrencyManagement( ConcurrencyManagementType.CONTAINER )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@Lock( LockType.READ )
public class S5BackendSkatletBoxSingleton
    extends S5BackendSupportSingleton
    implements IS5BackendSkatletBoxSingleton, IS5ServerJob {

  private static final long serialVersionUID = 157157L;

  /**
   * Имя синглетона в контейнере сервера для организации зависимостей (@DependsOn)
   */
  public static final String BACKEND_SKATLET_BOX_ID = "S5BackendSkatletBoxSingleton"; //$NON-NLS-1$

  /**
   * Идентификатор корневого контейнера компонентов синглетона
   */
  public static final String ROOT_BOX_ID = "RootBox"; //$NON-NLS-1$

  /**
   * Идентификатор контейнера скатлетов
   */
  public static final String SKATLET_BOX_ID = "SkatletBox"; //$NON-NLS-1$

  /**
   * Интервал выполнения doJob (мсек)
   */
  private static final long DOJOB_INTERVAL = 1000;

  /**
   * Время ожидания завершения работы контейнера (мсек)
   */
  private static final String SKATLETS_PATH = "../skatlets"; //$NON-NLS-1$

  /**
   * Время ожидания завершения работы контейнера (мсек)
   */
  private static final long SHUTDOWN_TIMEOUT = 60000;

  /**
   * Соединение с локальным сервером
   */
  @EJB
  private IS5LocalConnectionSingleton localConnectionSingleton;

  /**
   * Корневой контейнер компонентов синглетона
   */
  private WubBox rootBox;

  /**
   * Конструктор.
   */
  public S5BackendSkatletBoxSingleton() {
    super( BACKEND_SKATLET_BOX_ID, STR_D_BACKEND_SKATLETS );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendSupportSingleton
  //
  @Override
  protected void doInitSupport() {
    // Инициализация базового класса
    super.doInitSupport();

    // Создание соединения для контейнера скатлетов
    ISkConnection connection = localConnectionSingleton.open( id() );

    // Каталог размещения скатлетов
    File file = new File( SKATLETS_PATH );
    if( !file.isDirectory() ) {
      // Каталог не существует. Создание
      file.mkdir();
      // Запись в журнал
      logger().warning( ERR_CREATE_SKATLET_DIR, SKATLETS_PATH );
    }

    // Параметры создания корневого контейнера и контейнера скатлетов
    IOptionSetEdit params = new OptionSet();
    OPDEF_UNIT_STOPPING_TIMEOUT_MSECS.setValue( params, avInt( SHUTDOWN_TIMEOUT ) );

    // Контекст для инициализации контейнера скатлетов
    TsContext environ = new TsContext();
    REF_SK_CONNECTION.setRef( environ, connection );
    PLUGIN_JAR_PATHS.setValue( environ.params(), avValobj( new StringArrayList( SKATLETS_PATH ) ) );

    // Создание корневого контейнера...
    rootBox = new WubBox( ROOT_BOX_ID, params );
    // Добавление в корневой контейнер контейнера скатлетов
    rootBox.addUnit( new SkatletBox( SKATLET_BOX_ID, params ) );
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
  }

  // ------------------------------------------------------------------------------------
  // IS5BackendSkatletBoxSingleton
  //

  // ------------------------------------------------------------------------------------
  // Реализация IS5ServerJob
  //
  @Override
  public void doJob() {
    // Обработка корневого контейнера
    synchronized (rootBox) {
      rootBox.doJob();
    }
    // Вывод журнала
    logger().debug( MSG_DOJOB );
  }
}
