package org.toxsoft.uskat.s5.server.backend.supports.currdata.impl;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.currdata.impl.IS5CurrDataInterceptor.*;
import static org.toxsoft.uskat.s5.server.backend.supports.currdata.impl.IS5Resources.*;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.ejb.*;

import org.infinispan.Cache;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.gw.IGwHardConstants;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.TsInternalErrorRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.api.BaMsgRtdataCurrData;
import org.toxsoft.uskat.core.backend.api.IBaRtdata;
import org.toxsoft.uskat.s5.common.sysdescr.ISkSysdescrReader;
import org.toxsoft.uskat.s5.server.backend.addons.rtdata.S5BaRtdataData;
import org.toxsoft.uskat.s5.server.backend.impl.S5BackendSupportSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.currdata.IS5BackendCurrDataSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.currdata.impl.cluster.S5ClusterCommandCurrdataUnlockGwids;
import org.toxsoft.uskat.s5.server.backend.supports.objects.IS5BackendObjectsSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.IS5BackendSysDescrSingleton;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.interceptors.S5InterceptorSupport;
import org.toxsoft.uskat.s5.utils.jobs.IS5ServerJob;

/**
 * Реализация синглетона {@link IS5BackendCurrDataSingleton}
 *
 * @author mvk
 */
@Startup
@Singleton
@LocalBean
@DependsOn( { //
    BACKEND_LINKS_SINGLETON //
} )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
// @ConcurrencyManagement( ConcurrencyManagementType.CONTAINER )
@ConcurrencyManagement( ConcurrencyManagementType.BEAN )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@Lock( LockType.READ )
public class S5BackendCurrDataSingleton
    extends S5BackendSupportSingleton
    implements IS5BackendCurrDataSingleton, IS5ServerJob {

  private static final long serialVersionUID = 157157L;

  /**
   * Имя синглетона в контейнере сервера для организации зависимостей (@DependsOn)
   */
  public static final String BACKEND_CURRDATA_ID = "S5BackendCurrDataSingleton"; //$NON-NLS-1$

  /**
   * Таймат (мсек) проверки активной транзакции при ожидании захвата блокировки данных
   */
  private static final int TX_LOCK_CHECK_ACTIVE_TX_TIMEOUT = 1000;

  /**
   * Максимальный таймат (мсек) при ожидании захвата блокировки данных
   */
  private static final int TX_LOCKED_GWIDS_TIMEOUT = 10000;

  /**
   * Карта кэша значений текущих данных.
   * <p>
   * Ключ: {@link Integer} целочисленный индекс текущего данного;<br>
   * Значение: {@link IAtomicValue} значение текущего данного
   */
  @Resource( lookup = INFINISPAN_CACHE_CURRDATA_VALUES )
  private Cache<Gwid, IAtomicValue> currdataValuesCache;

  /**
   * Команда кластера: всем узлам разблокировать доступ к указанным данным;
   */
  private S5ClusterCommandCurrdataUnlockGwids clusterUnlockGwidsCmd;

  /**
   * Набор идентификаторов данных заблокированных для локального доступа
   * <p>
   * Данные блокируются на время выполнения операции добавления/обновления/удаления значений
   */
  private final Set<Gwid> localLockedGwids = new HashSet<>();

  /**
   * Набор идентификаторов данных заблокированных для удаленного доступа
   * <p>
   * Данные блокируются на время выполнения операции добавления/обновления/удаления значений и остаются заблокированными
   * до попытки их блокировки удаленной стороной
   */
  private final Set<Gwid> remoteLockedGwids = new HashSet<>();

  /**
   * backend управления классами системы (интерсепция системного описания)
   */
  @EJB
  private IS5BackendSysDescrSingleton sysdescrBackend;

  /**
   * backend управления классами системы (интерсепция объектов системы)
   */
  @EJB
  private IS5BackendObjectsSingleton objectsBackend;

  /**
   * Читатель системного описания
   */
  private ISkSysdescrReader sysdescrReader;

  /**
   * Интерспетор операций проводимых над типами и классами
   */
  private S5SysdescrInterceptor sysdescrInterceptor;

  /**
   * Интерспетор операций проводимых над объектами
   */
  private S5ObjectsInterceptor objectsInterceptor;

  /**
   * Идентификатор индекса следующего данного добавляемого в кэш. < 0: неопределенно
   */
  private static final Gwid NEXT_DATA_INDEX_GWID = Gwid.createClass( IGwHardConstants.GW_ROOT_CLASS_ID );

  /**
   * Поддержка интерсепторов операций проводимых над данными
   */
  private final S5InterceptorSupport<IS5CurrDataInterceptor> interceptors = new S5InterceptorSupport<>();

  /**
   * Интервал выполнения doJob (мсек)
   */
  private static final long DOJOB_INTERVAL = 1000;

  /**
   * Конструктор.
   */
  public S5BackendCurrDataSingleton() {
    super( BACKEND_CURRDATA_ID, STR_D_BACKEND_CURRDATA );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendSupportSingleton
  //
  @Override
  protected void doInitSupport() {
    // Инициализация базового класса
    super.doInitSupport();
    // Запуск doJob
    addOwnDoJob( DOJOB_INTERVAL );
  }

  @Override
  protected void doCloseSupport() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5BackendCurrDataSingleton
  //
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void reconfigure( IGwidList aRemoveRtdGwids, IMap<Gwid, IAtomicValue> aAddRtdGwids ) {
    TsNullArgumentRtException.checkNulls( aRemoveRtdGwids, aAddRtdGwids );

    // Пред-вызов интерсепторов
    if( !callBeforeReconfigureCurrData( interceptors, aRemoveRtdGwids, aAddRtdGwids ) ) {
      // Интерсепторы отклонили изменение набора
      logger().info( MSG_REJECT_CONFIGURE_BY_INTERCEPTORS );
      return;
    }

    try {
      // Удаление данных из кэша
      for( Gwid gwid : aRemoveRtdGwids ) {
        currdataValuesCache.remove( gwid );
      }

      // Размещение данных в кэше
      for( Gwid gwid : aAddRtdGwids.keys() ) {
        currdataValuesCache.put( gwid, aAddRtdGwids.getByKey( gwid ) );
      }
    }
    catch( Throwable e ) {
      // Неожиданные ошибки
      throw new TsInternalErrorRtException( e );
    }
    // Требование обновить состояние набора
    // TODO: 2020-07-30 mvk ???
    // dataset = null;
    // debugIndexes = null;
    // Оповещение других узлов: boolean remoteOnly = true; boolean primaryOnly = false
    // TODO: 2020-07-30
    // clusterManager().sendAsyncCommand( createUpdateCurrdataCommand(), true, false );
    // Возвращаем полученный результат
    // TODO: 2020-07-30 mvk ???
    // return dataset();

    // Пост-вызов интерсепторов
    callAfterReconfigureCurrData( interceptors, aRemoveRtdGwids, aAddRtdGwids, logger() );

  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void configureCurrDataReader( IS5FrontendRear aFrontend, IGwidList aRtdGwids ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aRtdGwids );

    // Данные фронтенда
    S5BaRtdataData baData = aFrontend.frontendData().findBackendAddonData( IBaRtdata.ADDON_ID, S5BaRtdataData.class );
    // Удаляемые данные из подписки
    GwidList removeRtdGwids = new GwidList( baData.readCurrDataGwids );
    // Добавляемые данные в подписку
    GwidList addRtdGwids = new GwidList();
    for( Gwid rtdGwid : aRtdGwids ) {
      if( removeRtdGwids.remove( rtdGwid ) < 0 ) {
        addRtdGwids.add( rtdGwid );
      }
    }
    // Пред-вызов интерсепторов
    if( !callBeforeConfigureCurrDataReader( interceptors, aFrontend, removeRtdGwids, addRtdGwids ) ) {
      // Интерсепторы отклонили регистрацию читателя данных
      logger().info( MSG_REJECT_READER_BY_INTERCEPTORS );
      return;
    }

    // Пост-вызов интерсепторов
    callAfterConfigureCurrDataReader( interceptors, aFrontend, removeRtdGwids, addRtdGwids, logger() );
    // Фактическое выполнение подписки на данные
    baData.readCurrDataGwids.setAll( aRtdGwids );

    // Планирование передачи текущих значений для вновь подписанных данных
    if( addRtdGwids.size() > 0 ) {
      IMap<Gwid, IAtomicValue> values = readValues( addRtdGwids );
      aFrontend.onBackendMessage( BaMsgRtdataCurrData.INSTANCE.makeMessage( values ) );
    }
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void configureCurrDataWriter( IS5FrontendRear aFrontend, IGwidList aRtdGwids ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aRtdGwids );

    // Данные фронтенда
    S5BaRtdataData baData = aFrontend.frontendData().findBackendAddonData( IBaRtdata.ADDON_ID, S5BaRtdataData.class );
    // Удаляемые данные из подписки
    GwidList removeRtdGwids = new GwidList( baData.writeCurrDataGwids );
    // Добавляемые данные в подписку
    GwidList addRtdGwids = new GwidList();
    for( Gwid rtdGwid : aRtdGwids ) {
      if( removeRtdGwids.remove( rtdGwid ) < 0 ) {
        addRtdGwids.add( rtdGwid );
      }
    }
    // Пред-вызов интерсепторов
    if( !callBeforeConfigureCurrDataWriter( interceptors, aFrontend, removeRtdGwids, addRtdGwids ) ) {
      // Интерсепторы отклонили регистрацию читателя данных
      logger().info( MSG_REJECT_READER_BY_INTERCEPTORS );
      return;
    }

    // Пост-вызов интерсепторов
    callAfterConfigureCurrDataWriter( interceptors, aFrontend, removeRtdGwids, addRtdGwids, logger() );
    // Фактическое выполнение подписки на данные
    baData.writeCurrDataGwids.setAll( aRtdGwids );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public IGwidList readRtdGwids() {
    // TODO Auto-generated method stub
    return null;
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public IGwidList writeRtdGwids() {
    // TODO Auto-generated method stub
    return null;
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public IMap<Gwid, IAtomicValue> readValues( IGwidList aRtdGwids ) {
    // TODO Auto-generated method stub
    return null;
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void writeValues( IMap<Gwid, IAtomicValue> aValues ) {
    // TODO Auto-generated method stub

  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void addCurrDataInterceptor( IS5CurrDataInterceptor aInterceptor, int aPriority ) {
    // TODO Auto-generated method stub

  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void removeCurrDataInterceptor( IS5CurrDataInterceptor aInterceptor ) {
    // TODO Auto-generated method stub

  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public boolean remoteUnlockGwids( IList<Gwid> aRtdGwids ) {
    // TODO Auto-generated method stub
    return false;
  }
}
