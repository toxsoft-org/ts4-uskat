package org.toxsoft.uskat.s5.server.backend.supports.currdata.impl;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.currdata.impl.IS5CurrDataInterceptor.*;
import static org.toxsoft.uskat.s5.server.backend.supports.currdata.impl.IS5Resources.*;

import java.util.*;
import java.util.concurrent.*;

import javax.annotation.*;
import javax.ejb.*;

import org.infinispan.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.errors.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.s5.common.sysdescr.*;
import org.toxsoft.uskat.s5.server.backend.addons.rtdata.*;
import org.toxsoft.uskat.s5.server.backend.impl.*;
import org.toxsoft.uskat.s5.server.backend.supports.currdata.*;
import org.toxsoft.uskat.s5.server.backend.supports.objects.*;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.*;
import org.toxsoft.uskat.s5.server.frontend.*;
import org.toxsoft.uskat.s5.server.interceptors.*;
import org.toxsoft.uskat.s5.utils.jobs.*;

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
   * Интервал выполнения doJob (мсек)
   */
  private static final long DOJOB_INTERVAL = 10;

  /**
   * Карта кэша значений текущих данных.
   * <p>
   * Ключ: {@link Gwid} идентификатор текущего данного;<br>
   * Значение: {@link IAtomicValue} значение текущего данного
   */
  @Resource( lookup = INFINISPAN_CACHE_CURRDATA_VALUES )
  private Cache<Gwid, IAtomicValue> valuesCache;

  /**
   * Атомарные типы значений данных.
   * <p>
   * Ключ: {@link Gwid} идентификатор текущего данного;<br>
   * Значение: {@link EAtomicType}-тип данного
   */
  private final Map<Gwid, EAtomicType> valuesTypes = new ConcurrentHashMap<>();

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
  // Реализация шаблонных методов S5BackendSupportSingleton
  //
  @Override
  protected void doInitSupport() {
    // Инициализация базового класса
    super.doInitSupport();
    // Читатель системного описания
    sysdescrReader = sysdescrBackend.getReader();
    IS5BackendCurrDataSingleton currdataSingleton =
        sessionContext().getBusinessObject( IS5BackendCurrDataSingleton.class );
    sysdescrInterceptor = new S5SysdescrInterceptor( transactionManager(), objectsBackend, currdataSingleton );
    sysdescrBackend.addClassInterceptor( sysdescrInterceptor, 2 );
    objectsInterceptor = new S5ObjectsInterceptor( currdataSingleton );
    objectsBackend.addObjectsInterceptor( objectsInterceptor, 2 );
    // Инициализация кэша значений
    initCache( sysdescrReader, objectsBackend, valuesCache, logger() );
    // Запуск doJob
    addOwnDoJob( DOJOB_INTERVAL );
  }

  @Override
  public void doJob() {
    super.doJob();
    // Текущее время
    long currTime = System.currentTimeMillis();
    // Обработка данных фронтендов
    for( IS5FrontendRear frontend : backend().attachedFrontends() ) {
      // Данные расширения для сессии
      S5BaRtdataData baData = frontend.frontendData().findBackendAddonData( IBaRtdata.ADDON_ID, S5BaRtdataData.class );
      if( baData == null ) {
        // фронтенд не поддерживает обработку текущих данных
        continue;
      }
      GtMessage message = null;
      synchronized (baData) {
        if( baData.currdataToFrontend.size() > 0 && //
            (currTime - baData.lastCurrdataToFrontendTime >= baData.currdataTimeout) ) {
          message = BaMsgRtdataCurrData.INSTANCE.makeMessage( baData.currdataToFrontend );
          baData.currdataToFrontend.clear();
          baData.lastCurrdataToFrontendTime = currTime;
        }
      }
      if( message != null ) {
        // Передача текущих значений фронтенду
        frontend.onBackendMessage( message );
      }
    }
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
        valuesCache.remove( gwid );
      }

      // Размещение данных в кэше
      for( Gwid gwid : aAddRtdGwids.keys() ) {
        valuesCache.put( gwid, aAddRtdGwids.getByKey( gwid ) );
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
  public IMap<Gwid, IAtomicValue> configureCurrDataReader( IS5FrontendRear aFrontend, IGwidList aToRemove,
      IGwidList aToAdd ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aToAdd );

    // Данные фронтенда
    S5BaRtdataData baData = aFrontend.frontendData().findBackendAddonData( IBaRtdata.ADDON_ID, S5BaRtdataData.class );
    // Пред-вызов интерсепторов
    if( !callBeforeConfigureCurrDataReader( interceptors, aFrontend, aToRemove, aToAdd ) ) {
      // Интерсепторы отклонили регистрацию читателя данных
      logger().info( MSG_REJECT_READER_BY_INTERCEPTORS );
      return IMap.EMPTY;
    }
    // Фактическое выполнение подписки на данные
    synchronized (baData) {
      if( aToRemove == null ) {
        baData.currdataGwidsToFrontend.clear();
      }
      if( aToRemove != null ) {
        for( Gwid g : aToRemove ) {
          baData.currdataGwidsToFrontend.remove( g );
        }
      }
      baData.currdataGwidsToFrontend.addAll( aToAdd );
    }
    // Пост-вызов интерсепторов
    callAfterConfigureCurrDataReader( interceptors, aFrontend, aToRemove, aToAdd, logger() );

    if( aToAdd.size() == 0 ) {
      // Нет новых данных
      return IMap.EMPTY;
    }
    IMap<Gwid, IAtomicValue> retValue = readValues( aToAdd );
    return retValue;
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void configureCurrDataWriter( IS5FrontendRear aFrontend, IGwidList aToRemove, IGwidList aToAdd ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aToAdd );

    // Данные фронтенда
    S5BaRtdataData baData = aFrontend.frontendData().findBackendAddonData( IBaRtdata.ADDON_ID, S5BaRtdataData.class );
    // Пред-вызов интерсепторов
    if( !callBeforeConfigureCurrDataWriter( interceptors, aFrontend, aToRemove, aToAdd ) ) {
      // Интерсепторы отклонили регистрацию читателя данных
      logger().info( MSG_REJECT_READER_BY_INTERCEPTORS );
      return;
    }
    // Фактическое выполнение подписки на данные
    synchronized (baData) {
      if( aToRemove == null ) {
        baData.currdataGwidsToBackend.clear();
      }
      if( aToRemove != null ) {
        for( Gwid g : aToRemove ) {
          baData.currdataGwidsToBackend.remove( g );
        }
      }
      baData.currdataGwidsToBackend.addAll( aToAdd );
    }
    // Пост-вызов интерсепторов
    callAfterConfigureCurrDataWriter( interceptors, aFrontend, aToRemove, aToAdd, logger() );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public IGwidList readRtdGwids() {
    Set<Gwid> gwids = new HashSet<>();
    for( IS5FrontendRear frontend : backend().attachedFrontends() ) {
      S5BaRtdataData baData = frontend.frontendData().findBackendAddonData( IBaRtdata.ADDON_ID, S5BaRtdataData.class );
      if( baData == null ) {
        // Фронтенд не работает с данными реального времени
        continue;
      }
      synchronized (baData) {
        for( Gwid gwid : baData.currdataGwidsToFrontend ) {
          gwids.add( gwid );
        }
      }
    }
    return new GwidList( gwids );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public IGwidList writeRtdGwids() {
    Set<Gwid> gwids = new HashSet<>();

    for( IS5FrontendRear frontend : backend().attachedFrontends() ) {
      S5BaRtdataData baData = frontend.frontendData().findBackendAddonData( IBaRtdata.ADDON_ID, S5BaRtdataData.class );
      if( baData == null ) {
        // Фронтенд не работает с данными реального времени
        continue;
      }
      synchronized (baData) {
        for( Gwid gwid : baData.currdataGwidsToBackend ) {
          gwids.add( gwid );
        }
      }
    }
    return new GwidList( gwids );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public IMap<Gwid, IAtomicValue> readValues( IGwidList aRtdGwids ) {
    TsNullArgumentRtException.checkNull( aRtdGwids );
    IMapEdit<Gwid, IAtomicValue> retValue = new ElemMap<>();
    // Начало пакетного изменения значений в кэше
    for( Gwid gwid : aRtdGwids ) {
      IAtomicValue value = valuesCache.get( gwid );
      if( value == null ) {
        // Текущее данное не зарегистрировано
        logger().error( ERR_READ_CACHE_VALUE_NOT_FOUND, gwid );
        continue;
      }
      retValue.put( gwid, value );
    }
    return retValue;
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void writeValues( IS5FrontendRear aFrontend, IMap<Gwid, IAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aValues );
    Thread t = Thread.currentThread();
    synchronized (this) {
      // Текущее время трассировки
      long traceTime0 = System.currentTimeMillis();

      IMapEdit<Gwid, IAtomicValue> changedValues = new ElemMap<>();
      Map<Gwid, IAtomicValue> newCachedValues = new HashMap<>();

      for( Gwid gwid : aValues.keys() ) {
        IAtomicValue newValue = aValues.getByKey( gwid );
        IAtomicValue prevValue = valuesCache.get( gwid );
        if( prevValue == null ) {
          // В кэше не найдено данное
          logger().error( ERR_WRITE_CACHE_VALUE_NOT_FOUND, gwid, newValue );
          continue;
        }
        EAtomicType type = valuesTypes.get( gwid );
        if( type == null ) {
          ISkClassInfo classInfo = sysdescrReader.getClassInfo( gwid.classId() );
          IDtoRtdataInfo dataInfo = classInfo.rtdata().list().getByKey( gwid.propId() );
          type = dataInfo.dataType().atomicType();
          valuesTypes.put( gwid, type );
        }

        // Проверка типа значения
        AvTypeCastRtException.checkCanAssign( type, newValue.atomicType() );

        if( !prevValue.equals( newValue ) ) {
          // Изменилось значение текущего данного
          newCachedValues.put( gwid, newValue );
          changedValues.put( gwid, newValue );
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

      // Запись значений в кэш
      valuesCache.putAll( newCachedValues );

      // Пост-вызов интерсепторов
      callAfterWriteCurrData( interceptors, changedValues, logger() );

      // Текущее время
      long currTime = System.currentTimeMillis();
      // Проход по всем фронтендам и формирование их буферов передачи данных
      for( IS5FrontendRear frontend : backend().attachedFrontends() ) {
        // Данные расширения для сессии
        S5BaRtdataData baData =
            frontend.frontendData().findBackendAddonData( IBaRtdata.ADDON_ID, S5BaRtdataData.class );
        if( baData == null ) {
          // фронтенд не поддерживает обработку текущих данных
          continue;
        }
        GtMessage message = null;
        synchronized (baData) {
          for( Gwid gwid : changedValues.keys() ) {
            if(
            // фронтенд подписан на чтение значений текущих данного
            baData.currdataGwidsToFrontend.hasElem( gwid ) ||
            // фронтенд формирует значения текущего данного, но не он их изменил
                (frontend != aFrontend && baData.currdataGwidsToBackend.hasElem( gwid )) ) {
              if( baData.currdataToFrontend.size() == 0 ) {
                // При добавлении первого данного обнуляем отсчет времени
                baData.lastCurrdataToFrontendTime = currTime;
              }
              baData.currdataToFrontend.put( gwid, changedValues.getByKey( gwid ) );
            }
          }
          if( baData.currdataToFrontend.size() > 0 && baData.currdataTimeout <= 0 ) {
            // Немедленная передача текущих значений фронтенду
            message = BaMsgRtdataCurrData.INSTANCE.makeMessage( baData.currdataToFrontend );
            baData.currdataToFrontend.clear();
            baData.lastCurrdataToFrontendTime = currTime;
          }
        }
        if( message != null ) {
          // Немедленная передача текущих значений фронтенду
          frontend.onBackendMessage( message );
        }
      }
      // Текущее время
      currTime = System.currentTimeMillis();
      // Вывод в журнал сообщения об изменении значений
      writeValuesToLog( logger(), newCachedValues, currTime - traceTime0 );

      if( t != Thread.currentThread() ) {
        logger().info( MSG_WRITE_THREAD_DIFF );
      }
    }
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void addCurrDataInterceptor( IS5CurrDataInterceptor aInterceptor, int aPriority ) {
    TsNullArgumentRtException.checkNull( aInterceptor );
    interceptors.add( aInterceptor, aPriority );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void removeCurrDataInterceptor( IS5CurrDataInterceptor aInterceptor ) {
    TsNullArgumentRtException.checkNull( aInterceptor );
    interceptors.remove( aInterceptor );
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Инициализация работы с кэшем текущих данных
   *
   * @param aSysdescrReader {@link ISkSysdescrReader} читатель системного описания
   * @param aObjectsBackend {@link IS5BackendObjectsSingleton} поддержка доступа к объектам системы
   * @param aValuesCache {@link Cache}&lt;{@link Gwid},{@link Integer}&gt; карта кэша значений данных. <br>
   *          Ключ: {@link Integer} целочисленный индекс текущего данного;<br>
   *          Значение: {@link IAtomicValue} значение текущего данного
   * @param aLogger {@link ILogger} журнал работы
   * @return int количество данных в кэше
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static int initCache( ISkSysdescrReader aSysdescrReader, IS5BackendObjectsSingleton aObjectsBackend,
      Cache<Gwid, IAtomicValue> aValuesCache, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aSysdescrReader, aObjectsBackend, aValuesCache, aLogger );
    long traceStartTime = System.currentTimeMillis();
    IMap<Gwid, IAtomicValue> allValues = getDefaultValues( aSysdescrReader, aObjectsBackend, aLogger );
    int size = aValuesCache.size();
    Long traceLoadTime = Long.valueOf( System.currentTimeMillis() - traceStartTime );
    if( size > 0 ) {
      // Кэш уже сформирован кластером
      aLogger.info( MSG_CACHE_ALREADY_INITED, Integer.valueOf( size ), traceLoadTime );
      if( allValues.size() != size ) {
        // Размер кэша текущих данных не соотвествует количеству текущих данных в системе
        aLogger.error( ERR_WRONG_CACHE_SIZE, Integer.valueOf( size ), Integer.valueOf( allValues.size() ) );
      }
      return size;
    }
    // TODO: требуется блокировка доступа к кэшу текущих данных на уровне кластера
    for( Gwid gwid : allValues.keys() ) {
      aValuesCache.put( gwid, allValues.getByKey( gwid ) );
    }
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
    IStridablesList<IDtoRtdataInfo> infos = classCurrDataInfos( aClassInfo );
    if( infos.size() == 0 ) {
      // В классе нет текущих данных
      return IMap.EMPTY;
    }
    // Идентификатор класса
    String classId = aClassInfo.id();
    // Запрос объектов класса. false: без объектов классов-наследников
    IList<IDtoObject> objs = aObjectsBackend.readObjects( new StringArrayList( classId ) );
    if( objs.size() == 0 ) {
      // Нет объектов класса
      return IMap.EMPTY;
    }
    IMapEdit<Gwid, IAtomicValue> retValue = new ElemMap<>();
    for( IDtoRtdataInfo info : infos ) {
      if( !info.isCurr() ) {
        // Загружаются только текущие данные
        continue;
      }
      String dataId = info.id();
      IAtomicValue defaultValue = info.dataType().defaultValue();
      IAtomicValue isNullAllowed = info.dataType().params().findValue( IAvMetaConstants.TSID_IS_NULL_ALLOWED );
      if( defaultValue == IAtomicValue.NULL ) {
        if( isNullAllowed == null || !isNullAllowed.isAssigned() || !isNullAllowed.asBool() ) {
          aLogger.warning( ERR_NO_DEFAULT_VALUE, classId, dataId, info.dataType() );
        }
      }
      for( IDtoObject obj : objs ) {
        retValue.put( Gwid.createRtdata( classId, obj.strid(), dataId ), defaultValue );
      }
    }
    return retValue;
  }

  /**
   * Возвращает список описаний текущих данных класса
   *
   * @param aClassInfo {@link ISkClassInfo} - описание класса
   * @return {@link IList}&lt;{@link IDtoRtdataInfo}&gt; список описаний текущих данных класса
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static IStridablesList<IDtoRtdataInfo> classCurrDataInfos( ISkClassInfo aClassInfo ) {
    TsNullArgumentRtException.checkNull( aClassInfo );
    // Список описаний всех(текущих, исторических) данных с учетом данных родительских классов
    IStridablesList<IDtoRtdataInfo> rtdataInfos = aClassInfo.rtdata().list();
    // Список описаний текущих данных
    IStridablesListEdit<IDtoRtdataInfo> retValue = new StridablesList<>();
    for( IDtoRtdataInfo rtdataInfo : rtdataInfos ) {
      if( rtdataInfo.isCurr() ) {
        retValue.add( rtdataInfo );
      }
    }
    return retValue;
  }

  /**
   * Вывод записанных значений в журнал
   *
   * @param aLogger {@link ILogger} журнал
   * @param aValues {@link Map} карта значений
   * @param aTime long время (мсек) записи
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static void writeValuesToLog( ILogger aLogger, Map<Gwid, IAtomicValue> aValues, long aTime ) {
    TsNullArgumentRtException.checkNulls( aLogger, aValues );
    if( (aLogger.isSeverityOn( ELogSeverity.DEBUG ) || aLogger.isSeverityOn( ELogSeverity.INFO ))
        && aValues.size() == 1 ) {
      Gwid gwid = aValues.keySet().iterator().next();
      IAtomicValue value = aValues.get( gwid );
      aLogger.info( MSG_WRITE_CURRDATA_VALUE, gwid, value );
      return;
    }
    if( aLogger.isSeverityOn( ELogSeverity.DEBUG ) ) {
      aLogger.debug( toStr( MSG_WRITE_CURRDATA_VALUES_DEBUG, aValues, aTime ) );
      return;
    }
    if( aLogger.isSeverityOn( ELogSeverity.INFO ) ) {
      aLogger.info( MSG_WRITE_CURRDATA_VALUES_INFO, Integer.valueOf( aValues.size() ), Long.valueOf( aTime ) );
      return;
    }
  }

  /**
   * Возвращает строку представляющую значения текущих данных
   *
   * @param aMessage String начальная строка
   * @param aValues {@link Map} карта значений.
   *          <p>
   *          Ключ: {@link Gwid} идентификатор текущего данного;<br>
   *          Значение: {@link IAtomicValue} значение текущего данного
   * @param aTime long время (мсек) записи
   * @return String строка представления значений текущих данных
   */
  public static String toStr( String aMessage, Map<Gwid, IAtomicValue> aValues, long aTime ) {
    TsNullArgumentRtException.checkNulls( aValues );
    StringBuilder sb = new StringBuilder();
    sb.append( String.format( aMessage, Integer.valueOf( aValues.size() ), Long.valueOf( aTime ) ) );
    for( Gwid gwid : aValues.keySet() ) {
      IAtomicValue value = aValues.get( gwid );
      sb.append( String.format( MSG_CURRDATA_VALUE, gwid, value ) );
    }
    return sb.toString();
  }
}
