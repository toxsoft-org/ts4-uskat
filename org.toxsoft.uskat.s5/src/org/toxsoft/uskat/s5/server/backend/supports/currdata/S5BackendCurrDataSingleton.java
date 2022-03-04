package org.toxsoft.uskat.s5.server.backend.supports.currdata;

import static java.lang.String.*;
import static org.toxsoft.core.tslib.gw.gwid.Gwid.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.addons.realtime.S5RealtimeUtils.*;
import static org.toxsoft.uskat.s5.server.backend.supports.currdata.IS5CurrDataInterceptor.*;
import static org.toxsoft.uskat.s5.server.backend.supports.currdata.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.currdata.cluster.S5ClusterCommandCurrdataUpdate.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.ejb.*;

import org.infinispan.Cache;
import org.toxsoft.core.pas.tj.ITjValue;
import org.toxsoft.core.pas.tj.impl.TjUtils;
import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.IntMap;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.gw.IGwHardConstants;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ELogSeverity;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.server.backend.addons.realtime.S5RealtimeFrontendData;
import org.toxsoft.uskat.s5.server.backend.impl.S5BackendSupportSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.currdata.cluster.S5ClusterCommandCurrdataUnlockGwids;
import org.toxsoft.uskat.s5.server.backend.supports.currdata.cluster.S5ClusterCommandCurrdataUpdate;
import org.toxsoft.uskat.s5.server.backend.supports.objects.IS5BackendObjectsSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.IS5BackendSysDescrSingleton;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterCommand;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.interceptors.S5InterceptorSupport;
import org.toxsoft.uskat.s5.utils.collections.WrapperMap;
import org.toxsoft.uskat.s5.utils.datasets.S5DatasetSupport;

import ru.uskat.backend.addons.realtime.ISkBackendAddonRealtime;
import ru.uskat.backend.messages.SkMessageWhenCurrdataChanged;
import ru.uskat.common.dpu.IDpuObject;
import ru.uskat.common.dpu.rt.events.SkCurrDataValues;
import ru.uskat.core.api.sysdescr.ISkClassInfo;
import ru.uskat.core.api.sysdescr.ISkRtdataInfo;
import ru.uskat.core.common.helpers.sysdescr.ISkSysdescrReader;

/**
 * Реализация {@link IS5BackendCurrDataSingleton}.
 *
 * @author mvk
 */
@Startup
@Singleton
@LocalBean
@DependsOn( { //
    BACKEND_COMMANDS_SINGLETON //
} )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
@ConcurrencyManagement( ConcurrencyManagementType.CONTAINER )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@Lock( LockType.READ )
public class S5BackendCurrDataSingleton
    extends S5BackendSupportSingleton
    implements IS5BackendCurrDataSingleton {

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
   * Карта кэша целочисленных индексов текущих данных.
   * <p>
   * Ключ: {@link Gwid}-идентификатор текущих данных;<br>
   * Значение: {@link Integer} целочисленный индекс текущего данного
   */
  @Resource( lookup = INFINISPAN_CACHE_CURRDATA_INDEXES )
  private Cache<Gwid, Integer> currdataIndexesCache;

  /**
   * Карта кэша значений текущих данных.
   * <p>
   * Ключ: {@link Integer} целочисленный индекс текущего данного;<br>
   * Значение: {@link IAtomicValue} значение текущего данного
   */
  @Resource( lookup = INFINISPAN_CACHE_CURRDATA_VALUES )
  private Cache<Integer, IAtomicValue> currdataValuesCache;

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
   * Набор данных (оптимизация)
   * <p>
   * Ключ: {@link Gwid}-идентификатор текущих данных;<br>
   * Значение: {@link Integer} целочисленный индекс текущего данного
   */
  private IMap<Gwid, Integer> dataset;

  /**
   * Индексы набор данных (оптимизация).
   * <p>
   * Ключ: {@link Integer} целочисленный индекс текущего данного;<br>
   * Значение: {@link Gwid}-идентификатор текущих данных
   * <p>
   * Используется только(!) для отладочных целей (журнала) и может несоответствовать текущему описанию системы
   */
  private final Map<Integer, Gwid> debugIndexes = new ConcurrentHashMap<>();

  // TODO: 2020-12-07 mvkd
  /**
   * Атомарные типы данных (отладка).
   * <p>
   * Ключ: {@link Integer} целочисленный индекс текущего данного;<br>
   * Значение: {@link EAtomicType}-тип данного
   * <p>
   * Используется только(!) для отладочных целей (журнала) и может несоответствовать текущему описанию системы
   */
  private final Map<Integer, EAtomicType> typeIndexes = new ConcurrentHashMap<>();

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
   * Конструктор.
   */
  public S5BackendCurrDataSingleton() {
    super( BACKEND_CURRDATA_ID, STR_D_BACKEND_CURRDATA );
  }

  // ------------------------------------------------------------------------------------
  // Определение шаблонных методов S5BackendSupportSingleton
  //
  @Override
  protected void doInitSupport() {
    // Инициализация базового класса
    super.doInitSupport();
    // Читатель системного описания
    sysdescrReader = sysdescrBackend.getReader();
    IS5BackendCurrDataSingleton currdataSingleton =
        sessionContext().getBusinessObject( IS5BackendCurrDataSingleton.class );
    sysdescrInterceptor =
        new S5SysdescrInterceptor( transactionManager(), sysdescrReader, objectsBackend, currdataSingleton );
    sysdescrBackend.addClassInterceptor( sysdescrInterceptor, 2 );
    objectsInterceptor = new S5ObjectsInterceptor( sysdescrReader, currdataSingleton );
    objectsBackend.addObjectsInterceptor( objectsInterceptor, 2 );
    initCache( sysdescrReader, objectsBackend, currdataIndexesCache, currdataValuesCache, logger() );
    dataset = new WrapperMap<>( currdataIndexesCache );
    // Команда кластера: всем узлам разблокировать доступ к указанным данным
    clusterUnlockGwidsCmd = new S5ClusterCommandCurrdataUnlockGwids( this );
    // Регистрация исполнителя команды
    clusterManager().addCommandHandler( S5ClusterCommandCurrdataUnlockGwids.REMOTE_UNLOCKS_GWIDS_METHOD,
        clusterUnlockGwidsCmd );
    // Регистрация обработчиков уведомлений
    clusterManager().addCommandHandler( UPDATE_CURRDATA_METHOD,
        new S5ClusterCommandCurrdataUpdate( currdataSingleton ) );
  }

  @Override
  protected void doCloseSupport() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // IS5BackendCurrDataSingleton
  //
  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public void reconfigure( IGwidList aRemovedGwids, IMap<Gwid, IAtomicValue> aAddedGwids ) {
    TsNullArgumentRtException.checkNulls( aRemovedGwids, aAddedGwids );

    // Пред-вызов интерсепторов
    if( !callBeforeReconfigureCurrData( interceptors, aRemovedGwids, aAddedGwids ) ) {
      // Интерсепторы отклонили изменение набора
      logger().info( MSG_REJECT_CONFIGURE_BY_INTERCEPTORS );
      return;
    }

    // Обработка запроса
    for( Gwid gwid : aRemovedGwids ) {
      Integer index = currdataIndexesCache.remove( gwid );
      if( index != null ) {
        currdataValuesCache.remove( index );
      }
    }

    try {
      // Формирование индексов для новых данных
      int nextDataIndex = generateIndexes( aAddedGwids.size() );
      // Размещение данных в кэше
      for( int index = 0, n = aAddedGwids.size(); index < n; index++ ) {
        Integer currdataIndex = Integer.valueOf( nextDataIndex + index );
        Gwid gwid = aAddedGwids.keys().get( index );
        currdataIndexesCache.put( gwid, currdataIndex );
        currdataValuesCache.put( currdataIndex, aAddedGwids.values().get( index ) );
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
    callAfterReconfigureCurrData( interceptors, aRemovedGwids, aAddedGwids, logger() );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IIntMap<Gwid> configureCurrDataReader( IS5FrontendRear aFrontend, IGwidList aToRemove, IGwidList aToAdd ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aToAdd );

    // Пред-вызов интерсепторов
    if( !callBeforeConfigureCurrDataReader( interceptors, aFrontend, aToRemove, aToAdd ) ) {
      // Интерсепторы отклонили регистрацию читателя данных
      logger().info( MSG_REJECT_READER_BY_INTERCEPTORS );
      return IIntMap.EMPTY;
    }

    // Данные фронтенда
    S5RealtimeFrontendData frontendData = aFrontend.frontendData()
        .getAddonData( ISkBackendAddonRealtime.SK_BACKEND_ADDON_ID, S5RealtimeFrontendData.class );
    // Текущий набор чтения текущих данных сессии
    S5DatasetSupport currdata = frontendData.readCurrdata;
    // Реконфигурация набора
    IIntMap<Gwid> retValue = currdata.reconfigure( aToRemove, aToAdd, dataset );

    // Пост-вызов интерсепторов
    callAfterConfigureCurrDataReader( interceptors, aFrontend, aToRemove, aToAdd, logger() );

    // Планирование передачи текущих значений для вновь подписанных данных
    if( aToAdd.size() > 0 ) {
      SkMessageWhenCurrdataChanged.send( aFrontend, getValues( currdata.dataset( aToAdd ).keys() ) );
    }
    return retValue;
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public IIntMap<Gwid> configureCurrDataWriter( IS5FrontendRear aFrontend, IGwidList aToRemove, IGwidList aToAdd ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aToAdd );

    // Пред-вызов интерсепторов
    if( !callBeforeConfigureCurrDataWriter( interceptors, aFrontend, aToRemove, aToAdd ) ) {
      // Интерсепторы отклонили регистрацию писателя данных
      logger().info( MSG_REJECT_READER_BY_INTERCEPTORS );
      return IIntMap.EMPTY;
    }

    // Данные фронтенда
    S5RealtimeFrontendData frontendData = aFrontend.frontendData()
        .getAddonData( ISkBackendAddonRealtime.SK_BACKEND_ADDON_ID, S5RealtimeFrontendData.class );
    // Текущий набор записи текущих данных сессии
    S5DatasetSupport currdata = frontendData.writeCurrdata;
    // Реконфигурация набора
    IIntMap<Gwid> retValue = currdata.reconfigure( aToRemove, aToAdd, dataset );

    // Пост-вызов интерсепторов
    callAfterConfigureCurrDataWriter( interceptors, aFrontend, aToRemove, aToAdd, logger() );

    return retValue;
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IGwidList readCurrDataIds() {
    Set<Gwid> gwids = new HashSet<>();
    for( IS5FrontendRear frontend : backend().attachedFrontends() ) {
      S5RealtimeFrontendData frontendData = getRealtimeFrontendData( frontend );
      if( frontendData == null ) {
        // фронтенд не поддерживает реальное время
        continue;
      }
      // Фильтрация интересуемых текущих данных
      for( Gwid gwid : frontendData.readCurrdata.dataset().values() ) {
        gwids.add( gwid );
      }
    }
    return new GwidList( gwids );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IGwidList writeCurrDataIds() {
    Set<Gwid> gwids = new HashSet<>();
    for( IS5FrontendRear frontend : backend().attachedFrontends() ) {
      S5RealtimeFrontendData frontendData = getRealtimeFrontendData( frontend );
      if( frontendData == null ) {
        // фронтенд не поддерживает реальное время
        continue;
      }
      // Фильтрация интересуемых текущих данных
      for( Gwid gwid : frontendData.writeCurrdata.dataset().values() ) {
        gwids.add( gwid );
      }
    }
    return new GwidList( gwids );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public Pair<Integer, IAtomicValue> getValue( Gwid aGwid ) {
    TsNullArgumentRtException.checkNull( aGwid );
    Integer currDataIndex = currdataIndexesCache.get( aGwid );
    if( currDataIndex == null ) {
      // В системе нет текущего данного
      throw new TsIllegalArgumentRtException( ERR_CURRDATA_NOT_FOUND, aGwid );
    }
    IAtomicValue currDataValue = TsInternalErrorRtException.checkNull( currdataValuesCache.get( currDataIndex ) );
    return new Pair<>( currDataIndex, currDataValue );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public SkCurrDataValues getValues( IIntList aIndexes ) {
    TsNullArgumentRtException.checkNull( aIndexes );
    SkCurrDataValues retValue = new SkCurrDataValues();
    // Начало пакетного изменения значений в кэше
    for( int index : aIndexes ) {
      retValue.put( index, currdataValuesCache.get( Integer.valueOf( index ) ) );
    }
    return retValue;
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public void writeCurrData( IIntMap<IAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNulls( aValues );
    IIntMapEdit<IAtomicValue> changedValues = new IntMap<>();
    Map<Integer, IAtomicValue> newCachedValues = new HashMap<>();
    for( int index = 0, n = aValues.size(); index < n; index++ ) {
      Integer valueIndex = aValues.keys().get( index );
      IAtomicValue newValue = aValues.values().get( index );
      IAtomicValue prevValue = currdataValuesCache.get( valueIndex );
      if( prevValue == null ) {
        // В кэше не найдено значение для данного по индексу
        logger().error( ERR_CACHE_VALUE_NOT_FOUND, valueIndex, newValue );
        continue;
        // throw new TsIllegalArgumentRtException( ERR_CACHE_VALUE_NOT_FOUND, valueIndex, newValue );
      }

      // TODO: 2020-12-07 mvkd
      EAtomicType type = typeIndexes.get( valueIndex );
      EAtomicType valueType = newValue.atomicType();
      if( type == null ) {
        Gwid gwid = getGwid( valueIndex, currdataIndexesCache, debugIndexes );
        ISkClassInfo classInfo = sysdescrReader.getClassInfo( gwid.classId() );
        ISkRtdataInfo dataInfo = classInfo.rtdInfos().getByKey( gwid.propId() );
        type = dataInfo.dataDef().atomicType();
        typeIndexes.put( valueIndex, type );
      }
      if( newValue.isAssigned() && valueType != type ) {
        // Недопустимый тип значения
        Gwid gwid = getGwid( valueIndex, currdataIndexesCache, debugIndexes );
        throw new TsIllegalArgumentRtException( ERR_WRONG_VALUE_TYPE, valueIndex, gwid, type, valueType, newValue );
      }

      if( !prevValue.equals( newValue ) ) {
        // Изменилось значение текущего данного
        newCachedValues.put( valueIndex, newValue );
        changedValues.put( valueIndex.intValue(), newValue );
      }
    }
    if( changedValues.size() <= 0 ) {
      // Данные не изменились
      return;
    }

    // Пред-вызов интерсепторов
    if( !callBeforeWriteCurrData( interceptors, changedValues ) ) {
      // Интерсепторы отклонили запись значений текущих данных
      logger().info( MSG_REJECT_CURRDATA_WRITE_BY_INTERCEPTORS );
      return;
    }

    // Оповещение об изменении значений
    if( logger().isSeverityOn( ELogSeverity.INFO ) ) {
      // Вывод в лог сохраняемых данных
      logger().info( toStr( MSG_WRITE_CURRDATA_VALUES, currdataIndexesCache, debugIndexes, newCachedValues ) );
    }
    // Запись значений в кэш
    currdataValuesCache.putAll( newCachedValues );

    // Пост-вызов интерсепторов
    callAfterWriteCurrData( interceptors, changedValues, logger() );

    for( IS5FrontendRear frontend : backend().attachedFrontends() ) {
      S5RealtimeFrontendData frontendData = getRealtimeFrontendData( frontend );
      if( frontendData == null ) {
        // фронтенд не поддерживает реальное время
        continue;
      }
      // Фильтрация интересуемых текущих данных
      SkCurrDataValues values = frontendData.readCurrdata.filter( changedValues );

      if( logger().isSeverityOn( ELogSeverity.INFO ) || logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
        // Вывод в журнал информации о регистрации ресурсов в сессии
        StringBuilder sb = new StringBuilder();
        sb.append( format( "writeCurrData(...): sessionID = %s, readCurrdata (%d), filtered (%d)", //$NON-NLS-1$
            frontend.sessionID(), Integer.valueOf( frontendData.readCurrdata.dataset().values().size() ),
            Integer.valueOf( values.size() ) ) );
        for( int index : frontendData.readCurrdata.dataset().keys() ) {
          Gwid gwid = frontendData.readCurrdata.dataset().getByKey( index );
          boolean found = values.hasKey( index );
          sb.append( format( "\n  %s %s", (found ? "* " : "  "), gwid ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        logger().info( sb.toString() );
      }

      if( values.size() == 0 ) {
        // Текущему френтенду нечего отправлять
        continue;
      }
      SkMessageWhenCurrdataChanged.send( frontend, values );
    }
  }

  @Override
  public void addCurrDataInterceptor( IS5CurrDataInterceptor aInterceptor, int aPriority ) {
    TsNullArgumentRtException.checkNull( aInterceptor );
    interceptors.add( aInterceptor, aPriority );
  }

  @Override
  public void removeCurrDataInterceptor( IS5CurrDataInterceptor aInterceptor ) {
    TsNullArgumentRtException.checkNull( aInterceptor );
    interceptors.remove( aInterceptor );
  }

  @Override
  public boolean remoteUnlockGwids( IList<Gwid> aGwids ) {
    TsNullArgumentRtException.checkNull( aGwids );
    synchronized (localLockedGwids) {
      for( Gwid gwid : aGwids ) {
        if( remoteLockedGwids.contains( gwid ) ) {
          return false;
        }
      }
    }
    return true;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Инициализация работы с кэшем текущих данных
   *
   * @param aSysdescrReader {@link ISkSysdescrReader} читатель системного описания
   * @param aObjectsBackend {@link IS5BackendObjectsSingleton} поддержка доступа к объектам системы
   * @param aIndexesCache {@link Cache}&lt;{@link Gwid},{@link Integer}&gt; карта кэша индексов данных. <br>
   *          Ключ: {@link Gwid}-идентификатор текущего данного;<br>
   *          Значение: {@link Integer} целочисленный индекс текущего данного
   * @param aValuesCache {@link Cache}&lt;{@link Gwid},{@link Integer}&gt; карта кэша значений данных. <br>
   *          Ключ: {@link Integer} целочисленный индекс текущего данного;<br>
   *          Значение: {@link IAtomicValue} значение текущего данного
   * @param aLogger {@link ILogger} журнал работы
   * @return int количество данных в кэше
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static int initCache( ISkSysdescrReader aSysdescrReader, IS5BackendObjectsSingleton aObjectsBackend,
      Cache<Gwid, Integer> aIndexesCache, Cache<Integer, IAtomicValue> aValuesCache, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aSysdescrReader, aObjectsBackend, aIndexesCache, aValuesCache, aLogger );
    long traceStartTime = System.currentTimeMillis();
    IMap<Gwid, IAtomicValue> defaultValues = getDefaultValues( aSysdescrReader, aObjectsBackend, aLogger );
    int size = aIndexesCache.size();
    Long traceLoadTime = Long.valueOf( System.currentTimeMillis() - traceStartTime );
    if( size > 0 ) {
      // Кэш уже сформирован кластером
      aLogger.info( MSG_CACHE_ALREADY_INITED, Integer.valueOf( size ), traceLoadTime );
      if( defaultValues.size() != size ) {
        // Размер кэша текущих данных не соотвествует количеству текущих данных в системе
        aLogger.error( ERR_WRONG_CACHE_SIZE, Integer.valueOf( size ), Integer.valueOf( defaultValues.size() ) );
      }
      return size;
    }
    // TODO: требуется блокировка доступа к кэшу текущих данных на уровне кластера
    size = defaultValues.size();
    for( int index = 0; index < size; index++ ) {
      Gwid gwid = defaultValues.keys().get( index );
      IAtomicValue value = defaultValues.values().get( index );
      Integer valueIndex = Integer.valueOf( index );
      aIndexesCache.put( gwid, valueIndex );
      aValuesCache.put( valueIndex, value );
    }
    // Сохранение следующего индекса данных
    aIndexesCache.put( NEXT_DATA_INDEX_GWID, Integer.valueOf( size ) );
    // Сформирован кэш текущих данных кластера
    Long initTime = Long.valueOf( System.currentTimeMillis() - traceStartTime );
    aLogger.info( MSG_CACHE_INITED, Integer.valueOf( size ), traceLoadTime, initTime );
    return size;
  }

  /**
   * Возвращает значения текущих данных по умолчанию для всех объектов системы
   *
   * @param aSysdescrReader {@link ISkSysdescrReader} читатель системного описания
   * @param aObjectsBackend {@link IS5BackendObjectsSingleton} поддержка доступа к объектам системы
   * @param aLogger {@link ILogger} журнал работы
   * @return {@link IMap}&lt;{@link Gwid},{@link IAtomicValue}&gt; карта значений по умолчанию<br>
   *         Ключ: идентификатор текущего данного;<br>
   *         Значение: атомарное значение по умолчанию.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static IMap<Gwid, IAtomicValue> getDefaultValues( ISkSysdescrReader aSysdescrReader,
      IS5BackendObjectsSingleton aObjectsBackend, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aSysdescrReader, aObjectsBackend, aLogger );
    IMapEdit<Gwid, IAtomicValue> retValue = new ElemMap<>();
    for( ISkClassInfo classInfo : aSysdescrReader.getClassInfos() ) {
      retValue.putAll( getClassDefaultValues( aObjectsBackend, classInfo, aLogger ) );
    }
    return retValue;
  }

  /**
   * Возвращает значения текущих данных по умолчанию для всех объектов указанного класса без наследников
   *
   * @param aObjectsBackend {@link IS5BackendObjectsSingleton} поддержка доступа к объектам системы
   * @param aClassInfo {@link ISkClassInfo} описание класса
   * @param aLogger {@link ILogger} журнал работы
   * @return {@link IMap}&lt;{@link Gwid},{@link IAtomicValue}&gt; карта значений по умолчанию<br>
   *         Ключ: идентификатор текущего данного;<br>
   *         Значение: атомарное значение по умолчанию.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static IMap<Gwid, IAtomicValue> getClassDefaultValues( IS5BackendObjectsSingleton aObjectsBackend,
      ISkClassInfo aClassInfo, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aObjectsBackend, aClassInfo, aLogger );
    IStridablesList<ISkRtdataInfo> infos = classCurrDataInfos( aClassInfo );
    if( infos.size() == 0 ) {
      // В классе нет текущих данных
      return IMap.EMPTY;
    }
    // Идентификатор класса
    String classId = aClassInfo.id();
    // Запрос объектов класса. false: без объектов классов-наследников
    IList<IDpuObject> objs = aObjectsBackend.readObjects( new StringArrayList( classId ) );
    if( objs.size() == 0 ) {
      // Нет объектов класса
      return IMap.EMPTY;
    }
    IMapEdit<Gwid, IAtomicValue> retValue = new ElemMap<>();
    for( ISkRtdataInfo info : infos ) {
      if( !info.isCurr() ) {
        // Загружаются только текущие данные
        continue;
      }
      String dataId = info.id();
      IDataDef dataDef = info.dataDef();
      IAtomicValue defaultValue = dataDef.defaultValue();
      // TODO: 2019-12-28 mvk надо поднимать ошибку если defaultValue = IAtomicValue.NULL
      // Но сейчас это мешает запуску uskat-tm. Заменяем на вывод ошибки в лог
      // if( defaultValue == IAtomicValue.NULL && dataDef.params().getBool( TSID_IS_NULL_ALLOWED, true ) == false ) {
      // // Текущее данное имеет тип для которого требуется, но неопределено значение по умолчанию
      // throw new TsInternalErrorRtException( ERR_NO_DEFAULT_VALUE, classId, dataId, info.typeId() );
      // }
      aLogger.warning( ERR_NO_DEFAULT_VALUE, classId, dataId, info.typeId() );
      for( IDpuObject obj : objs ) {
        retValue.put( createRtdata( classId, obj.strid(), dataId ), defaultValue );
      }
    }
    return retValue;
  }

  /**
   * Возвращает список описаний текущих данных класса
   *
   * @param aClassInfo {@link ISkClassInfo} - описание класса
   * @return {@link IList}&lt;{@link ISkRtdataInfo}&gt; список описаний текущих данных класса
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static IStridablesList<ISkRtdataInfo> classCurrDataInfos( ISkClassInfo aClassInfo ) {
    TsNullArgumentRtException.checkNull( aClassInfo );
    // Список описаний всех(текущих, исторических) данных с учетом данных родительских классов
    IStridablesList<ISkRtdataInfo> rtdataInfos = aClassInfo.rtdInfos();
    // Список описаний текущих данных
    IStridablesListEdit<ISkRtdataInfo> retValue = new StridablesList<>();
    for( ISkRtdataInfo rtdataInfo : rtdataInfos ) {
      if( rtdataInfo.isCurr() ) {
        retValue.add( rtdataInfo );
      }
    }
    return retValue;
  }

  /**
   * Формирование индексов для данных
   *
   * @param aCount int количество формируемых индексов
   * @return int значение первого индекса
   */
  private int generateIndexes( int aCount ) {
    // Блокировка доступа к данному
    tryLockGwid( NEXT_DATA_INDEX_GWID );
    try {
      int nextDataIndex = currdataIndexesCache.get( NEXT_DATA_INDEX_GWID ).intValue();
      // Сохранение следующего индекса данных
      currdataIndexesCache.put( NEXT_DATA_INDEX_GWID, Integer.valueOf( nextDataIndex + aCount ) );
      // Возвращение результата
      return nextDataIndex;
    }
    finally {
      // Разблокировка доступа к данному
      unlockCache( NEXT_DATA_INDEX_GWID );
    }
  }

  /**
   * Выполняет попытку блокировки доступа к данному
   *
   * @param aGwid {@link Gwid} идентификатор блокируемого данного
   * @throws TsNullArgumentRtException аргумент = null
   */
  private void tryLockGwid( Gwid aGwid ) {
    TsNullArgumentRtException.checkNull( aGwid );
    synchronized (currdataIndexesCache) {
      // TODO: требуется блокировка доступа к кэшу текущих данных на уровне кластера
    }
    // Метка времени (мсек с начала эпохи) начала запроса блокирования данных
    long tryLockStartTime = System.currentTimeMillis();
    // Метка времени (мсек с начала эпохи) последней проверки активной транзакции
    long lastCheckTime = tryLockStartTime;
    synchronized (localLockedGwids) {
      // Проверка доступа к данным. true: с удаленным доступом
      while( !getGwidsAccess( this, aGwid ) ) {
        try {
          long currTime = System.currentTimeMillis();
          long timeFromCheck = currTime - lastCheckTime;
          if( timeFromCheck > TX_LOCK_CHECK_ACTIVE_TX_TIMEOUT ) {
            lastCheckTime = currTime;
            Long t = Long.valueOf( currTime - tryLockStartTime );
            logger().warning( MSG_WAIT_LOCK_INFOES, aGwid, t );
            if( currTime - tryLockStartTime > TX_LOCKED_GWIDS_TIMEOUT ) {
              // Превышение времени максимального ожидания блокировки данного
              throw new TsIllegalStateRtException( ERR_LOCK_INFOES_TIMEOUT, aGwid );
            }
          }
          localLockedGwids.wait( TX_LOCK_CHECK_ACTIVE_TX_TIMEOUT );
        }
        catch( InterruptedException e ) {
          logger().error( e );
          throw new TsIllegalStateRtException( e );
        }
      }
      localLockedGwids.add( aGwid );
      remoteLockedGwids.add( aGwid );
    }
  }

  /**
   * Выполняет разблокировку доступа к данному
   *
   * @param aGwid {@link Gwid} идентификатор блокируемого данного
   */
  private void unlockCache( Gwid aGwid ) {
    TsNullArgumentRtException.checkNull( aGwid );
    synchronized (localLockedGwids) {
      // Разблокирование доступа к данным
      localLockedGwids.remove( aGwid );
      // Сигнал оповещения ожидающих потоков об освобождении ресурса
      localLockedGwids.notifyAll();
    }
  }

  /**
   * Проверяет существование конфликтных записей по одним и тем же ресурсам и пытается их разрешить
   *
   * @param aCurrdataBackend {@link S5BackendCurrDataSingleton} бекенд текущих данных
   * @param aGwid {@link Gwid} идентификатор данного к которыму необходим доступ на запись
   * @return boolean <b>false</b> данное занято и не может быть освобождено;<b>true</b> получен доступ к данному
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static boolean getGwidsAccess( S5BackendCurrDataSingleton aCurrdataBackend, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aCurrdataBackend, aGwid );
    // Проверка локального доступа
    if( aCurrdataBackend.localLockedGwids.contains( aGwid ) ) {
      return false;
    }
    if( aCurrdataBackend.remoteLockedGwids.contains( aGwid ) ) {
      // Удаленный доступ уже есть
      return true;
    }
    // Получение удаленного доступа
    try {
      // Формирование команды на получение удаленного доступа
      IS5ClusterCommand cmd = S5ClusterCommandCurrdataUnlockGwids.createCommand( new GwidList( aGwid ) );
      // Отправляем запрос на получение доступа: boolean remoteOnly = true; boolean primaryOnly = false
      IStringMap<ITjValue> result = aCurrdataBackend.clusterManager().sendSyncCommand( cmd, true, false );
      // Обработка результата
      for( String nodeId : result.keys() ) {
        ITjValue value = result.getByKey( nodeId );
        if( !value.equals( TjUtils.TRUE ) ) {
          // Ошибка получения удаленного доступа к данным на узле
          aCurrdataBackend.logger().warning( ERR_REMOTE_ACCESS, nodeId, aGwid );
          // Ошибка получения доступа к данным
          return false;
        }
      }
    }
    catch( Throwable e ) {
      aCurrdataBackend.logger().error( e );
      // Ошибка получения доступа к данным
      return false;
    }
    return true;
  }
}
