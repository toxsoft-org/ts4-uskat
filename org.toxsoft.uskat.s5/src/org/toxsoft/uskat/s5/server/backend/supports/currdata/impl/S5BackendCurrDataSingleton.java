package org.toxsoft.uskat.s5.server.backend.supports.currdata.impl;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.currdata.impl.IS5CurrDataInterceptor.*;
import static org.toxsoft.uskat.s5.server.backend.supports.currdata.impl.IS5Resources.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.ejb.*;

import org.infinispan.Cache;
import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ELogSeverity;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.core.api.objserv.IDtoObject;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassInfo;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoRtdataInfo;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.s5.common.sysdescr.ISkSysdescrReader;
import org.toxsoft.uskat.s5.server.backend.addons.rtdata.S5BaRtdataData;
import org.toxsoft.uskat.s5.server.backend.impl.S5BackendSupportSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.currdata.IS5BackendCurrDataSingleton;
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
    long currTime = System.currentTimeMillis();
    // Обработка данных фронтендов
    for( IS5FrontendRear frontend : backend().attachedFrontends() ) {
      // Данные расширения для сессии
      S5BaRtdataData baData =
          frontend.frontendData().findBackendAddonData( IBaCommands.ADDON_ID, S5BaRtdataData.class );
      if( baData == null ) {
        // фронтенд не поддерживает обработку текущих данных
        continue;
      }
      synchronized (baData) {
        if( baData.currDataToSend.size() > 0 && //
            (currTime - baData.lastCurrDataToSendTime >= baData.currDataToSendTimeout) ) {
          // Передача текущих значений фронтенду
          frontend.onBackendMessage( BaMsgRtdataCurrData.INSTANCE.makeMessage( baData.currDataToSend ) );
          baData.currDataToSend.clear();
          baData.lastCurrDataToSendTime = currTime;
        }
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
  public void configureCurrDataReader( IS5FrontendRear aFrontend, IGwidList aRtdGwids ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aRtdGwids );

    // Данные фронтенда
    S5BaRtdataData baData = aFrontend.frontendData().findBackendAddonData( IBaRtdata.ADDON_ID, S5BaRtdataData.class );
    // Удаляемые данные из подписки
    GwidList removeRtdGwids;
    synchronized (baData) {
      removeRtdGwids = new GwidList( baData.readCurrDataGwids );
    }
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
    synchronized (baData) {
      baData.readCurrDataGwids.setAll( aRtdGwids );
    }

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
    GwidList removeRtdGwids;
    synchronized (baData) {
      removeRtdGwids = new GwidList( baData.writeCurrDataGwids );
    }
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
    synchronized (baData) {
      baData.writeCurrDataGwids.setAll( aRtdGwids );
    }
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
        for( Gwid gwid : baData.readCurrDataGwids ) {
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
        for( Gwid gwid : baData.writeCurrDataGwids ) {
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
      retValue.put( gwid, valuesCache.get( gwid ) );
    }
    return retValue;
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void writeValues( IMap<Gwid, IAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNulls( aValues );

    IMapEdit<Gwid, IAtomicValue> changedValues = new ElemMap<>();
    Map<Gwid, IAtomicValue> newCachedValues = new HashMap<>();

    for( Gwid gwid : aValues.keys() ) {
      IAtomicValue newValue = aValues.getByKey( gwid );
      IAtomicValue prevValue = valuesCache.get( gwid );
      if( prevValue == null ) {
        // В кэше не найдено данное
        logger().error( ERR_CACHE_VALUE_NOT_FOUND, gwid, newValue );
        continue;
      }
      EAtomicType type = valuesTypes.get( gwid );
      EAtomicType valueType = newValue.atomicType();
      if( type == null ) {
        ISkClassInfo classInfo = sysdescrReader.getClassInfo( gwid.classId() );
        IDtoRtdataInfo dataInfo = classInfo.rtdata().list().getByKey( gwid.propId() );
        type = dataInfo.dataType().atomicType();
        valuesTypes.put( gwid, type );
      }
      if( newValue.isAssigned() && newValue.atomicType() != type ) {
        // Недопустимый тип значения
        throw new TsIllegalArgumentRtException( ERR_WRONG_VALUE_TYPE, gwid, type, valueType, newValue );
      }

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

    // Оповещение об изменении значений
    if( logger().isSeverityOn( ELogSeverity.INFO ) ) {
      // Вывод в лог сохраняемых данных
      logger().info( toStr( MSG_WRITE_CURRDATA_VALUES, newCachedValues ) );
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
          frontend.frontendData().findBackendAddonData( IBaCommands.ADDON_ID, S5BaRtdataData.class );
      if( baData == null ) {
        // фронтенд не поддерживает обработку текущих данных
        continue;
      }
      synchronized (baData) {
        for( Gwid gwid : changedValues.keys() ) {
          if( baData.readCurrDataGwids.hasElem( gwid ) ) {
            baData.currDataToSend.put( gwid, changedValues.getByKey( gwid ) );
            if( baData.currDataToSend.size() == 1 ) {
              // При добавлении первого данного обнуляем отсчет времени
              baData.lastCurrDataToSendTime = currTime;
            }
          }
        }
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
      // TODO: 2019-12-28 mvk надо поднимать ошибку если defaultValue = IAtomicValue.NULL
      // Но сейчас это мешает запуску uskat-tm. Заменяем на вывод ошибки в лог
      // if( defaultValue == IAtomicValue.NULL && dataDef.params().getBool( TSID_IS_NULL_ALLOWED, true ) == false ) {
      // // Текущее данное имеет тип для которого требуется, но неопределено значение по умолчанию
      // throw new TsInternalErrorRtException( ERR_NO_DEFAULT_VALUE, classId, dataId, info.typeId() );
      // }
      aLogger.warning( ERR_NO_DEFAULT_VALUE, classId, dataId, info.dataType() );
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
   * Возвращает строку представляющую значения текущих данных
   *
   * @param aMessage String начальная строка
   * @param aValues {@link Map} карта значений.
   *          <p>
   *          Ключ: {@link Gwid} идентификатор текущего данного;<br>
   *          Значение: {@link IAtomicValue} значение текущего данного
   * @return String строка представления значений текущих данных
   */
  public static String toStr( String aMessage, Map<Gwid, IAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNulls( aValues );
    StringBuilder sb = new StringBuilder();
    sb.append( String.format( aMessage, Integer.valueOf( aValues.size() ) ) );
    for( Gwid gwid : aValues.keySet() ) {
      IAtomicValue value = aValues.get( gwid );
      sb.append( String.format( MSG_CURRDATA_VALUE, gwid, value ) );
    }
    return sb.toString();
  }
}
