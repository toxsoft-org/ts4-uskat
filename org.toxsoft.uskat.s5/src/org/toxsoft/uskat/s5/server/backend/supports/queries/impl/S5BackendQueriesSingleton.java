package org.toxsoft.uskat.s5.server.backend.supports.queries.impl;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.queries.impl.IS5Resources.*;

import java.util.concurrent.TimeUnit;

import javax.ejb.*;
import javax.enterprise.concurrent.ManagedExecutorService;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.strid.idgen.IStridGenerator;
import org.toxsoft.core.tslib.bricks.strid.idgen.UuidStridGenerator;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.cmdserv.IDtoCompletedCommand;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.core.api.hqserv.ESkQueryState;
import org.toxsoft.uskat.core.api.hqserv.IDtoQueryParam;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassInfo;
import org.toxsoft.uskat.core.backend.api.BaMsgQueryNextData;
import org.toxsoft.uskat.core.backend.api.IBaQueries;
import org.toxsoft.uskat.s5.common.sysdescr.ISkSysdescrReader;
import org.toxsoft.uskat.s5.server.backend.addons.queries.S5BaQueriesConvoy;
import org.toxsoft.uskat.s5.server.backend.addons.queries.S5BaQueriesData;
import org.toxsoft.uskat.s5.server.backend.impl.S5BackendSupportSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.commands.IS5BackendCommandSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.events.IS5BackendEventSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.IS5BackendHistDataSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.queries.IS5BackendQueriesFunction;
import org.toxsoft.uskat.s5.server.backend.supports.queries.IS5BackendQueriesSingleton;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.sequences.IS5Sequence;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceHardConstants;
import org.toxsoft.uskat.s5.server.sequences.reader.IS5SequenceReadQuery;
import org.toxsoft.uskat.s5.server.sequences.reader.IS5SequenceReader;
import org.toxsoft.uskat.s5.server.singletons.S5ServiceSingletonUtils;
import org.toxsoft.uskat.s5.utils.jobs.IS5ServerJob;
import org.toxsoft.uskat.s5.utils.threads.impl.S5ReadThreadExecutor;

/**
 * Реализация синглетона {@link IS5BackendQueriesSingleton}
 *
 * @author mvk
 */
@Startup
@Singleton
@LocalBean
@DependsOn( { //
    BACKEND_EVENTS_SINGLETON, //
    BACKEND_COMMANDS_SINGLETON, //
    BACKEND_HISTDATA_SINGLETON //
} )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
// @ConcurrencyManagement( ConcurrencyManagementType.CONTAINER )
@ConcurrencyManagement( ConcurrencyManagementType.BEAN )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@Lock( LockType.READ )
public class S5BackendQueriesSingleton
    extends S5BackendSupportSingleton
    implements IS5BackendQueriesSingleton, IS5ServerJob {

  private static final long serialVersionUID = 157157L;

  /**
   * Имя синглетона в контейнере сервера для организации зависимостей (@DependsOn)
   */
  public static final String BACKEND_QUERIES_ID = "S5BackendQueriesSingleton"; //$NON-NLS-1$

  /**
   * Интервал выполнения doJob (мсек)
   */
  private static final long DOJOB_INTERVAL = 1000;

  /**
   * Поддержка сервера системного описания
   */
  @EJB
  private IS5BackendEventSingleton sysdescrSupport;

  /**
   * Поддержка сервера запросов событий
   */
  @EJB
  private IS5BackendEventSingleton eventsSupport;

  /**
   * Поддержка сервера запросов команд
   */
  @EJB
  private IS5BackendCommandSingleton commandsSupport;

  /**
   * Поддержка сервера запросов хранимых данных
   */
  @EJB
  private IS5BackendHistDataSingleton histDataSupport;

  /**
   * Читатель системного описания
   */
  private ISkSysdescrReader sysdescrReader;

  /**
   * Исполнитель потоков чтения блоков
   */
  private ManagedExecutorService readExecutor;

  /**
   * Генератор идентификаторов запроса чтения данных
   */
  private final IStridGenerator uuidGenerator;

  /**
   * Конструктор.
   */
  public S5BackendQueriesSingleton() {
    super( BACKEND_QUERIES_ID, STR_D_BACKEND_QUERIES );
    uuidGenerator = new UuidStridGenerator( UuidStridGenerator.createState( BACKEND_QUERIES_ID ) );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendSupportSingleton
  //
  @Override
  protected void doInitSupport() {
    // Инициализация базового класса
    super.doInitSupport();
    // Поиск исполнителя потоков чтения блоков
    readExecutor = S5ServiceSingletonUtils.lookupExecutor( IS5SequenceHardConstants.READ_EXECUTOR_JNDI );
    // Запуск doJob
    addOwnDoJob( DOJOB_INTERVAL );
  }

  @Override
  public void doJob() {
    super.doJob();
  }

  @Override
  protected void doCloseSupport() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5BackendQueriesSingleton
  //
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public String createQuery( IS5FrontendRear aFrontend, IOptionSet aParams ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aParams );
    // Идентификатор запроса
    String queryId = uuidGenerator.nextId();
    // Конвой-объект запроса
    S5BaQueriesConvoy convoy = new S5BaQueriesConvoy( queryId, aParams );
    // Данные фронтенда
    S5BaQueriesData baData =
        aFrontend.frontendData().findBackendAddonData( IBaQueries.ADDON_ID, S5BaQueriesData.class );
    synchronized (baData) {
      // Размещение конвой-объекта в сессии
      baData.openQueries.put( queryId, convoy );
    }
    logger().info( MSG_CREATE_QUERY, queryId );
    return queryId;
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void prepareQuery( IS5FrontendRear aFrontend, String aQueryId, IStringMap<IDtoQueryParam> aParams ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aQueryId, aParams );
    S5BaQueriesData baData =
        aFrontend.frontendData().findBackendAddonData( IBaQueries.ADDON_ID, S5BaQueriesData.class );
    synchronized (baData) {
      S5BaQueriesConvoy convoy = baData.openQueries.getByKey( aQueryId );
      convoy.prepareQuery( aParams );
    }
    logger().info( MSG_PREPARE_QUERY, aQueryId );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  @Asynchronous
  public void execQuery( IS5FrontendRear aFrontend, String aQueryId, IQueryInterval aTimeInterval ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aQueryId, aTimeInterval );
    S5BaQueriesData baData =
        aFrontend.frontendData().findBackendAddonData( IBaQueries.ADDON_ID, S5BaQueriesData.class );
    // Время начала выполнения запроса
    long traceTime0 = System.currentTimeMillis();
    // Конвой-объект
    S5BaQueriesConvoy convoy = null;
    synchronized (baData) {
      convoy = baData.openQueries.getByKey( aQueryId );
      convoy.exec( aTimeInterval );
    }
    // Счетчик значений
    S5BackendQuriesCounter counter = new S5BackendQuriesCounter();
    // Интервал запроса
    IQueryInterval interval = convoy.interval();
    // Функции запроса
    IMap<IS5SequenceReader<IS5Sequence<?>, ?>, IList<IS5BackendQueriesFunction>> queryFunctions =
        getQueryFunctions( convoy, counter );
    // Трасировка 1
    long traceTime1 = System.currentTimeMillis();
    // Тип значений
    EGwidKind gwidKind = null;
    // Прочитанные значения
    IStringMap<ITimedList<ITemporal<?>>> values = null;
    // Счетчик обработанных значений
    int valueCounter = 0;
    // Результат для передачи
    for( IS5SequenceReader<IS5Sequence<?>, ?> reader : queryFunctions.keys() ) {
      // Функции обработки данных читателя
      IList<IS5BackendQueriesFunction> functions = queryFunctions.getByKey( reader );
      // Чтение значений функциями
      IList<ITimedList<ITemporal<?>>> readerValues = readByFunctions( aQueryId, reader, functions, interval );
      if( convoy.state() != ESkQueryState.EXECUTING ) {
        // Отмена выполнения запроса
        logger().error( ERR_CANCEL_QUERY, "execQuery(...)", aQueryId, TsLibUtils.EMPTY_STRING ); //$NON-NLS-1$
        return;
      }
      // Подсчет обработанных значений
      for( ITimedList<ITemporal<?>> rv : readerValues ) {
        valueCounter += rv.size();
      }
      if( readerValues.size() > 0 && values != null && values.size() > 0 ) {
        // Передача предыдущих значений (промежуточных, aFinished = false)
        fireNextDataMessage( aFrontend, aQueryId, gwidKind, values, false );
        // Сохранение результатов нового чтения
        gwidKind = getGwidKind( reader );
        values = getValuesMap( functions, readerValues );
      }
    }
    // Трасировка 2
    long traceTime2 = System.currentTimeMillis();
    synchronized (baData) {
      convoy = baData.openQueries.getByKey( aQueryId );
      convoy.cancel();
    }
    // Передача последних данных (даже если они не получены)
    if( gwidKind == null ) {
      // Данных нет - отправляем пустой ответ
      gwidKind = EGwidKind.GW_RTDATA;
      values = IStringMap.EMPTY;
    }
    // Передача последних значений, aFinished = true
    fireNextDataMessage( aFrontend, aQueryId, gwidKind, values, true );
    // Трасировка 3
    long traceTime3 = System.currentTimeMillis();

    // Вывод в журнал информации об обработке запроса
    Integer pc = Integer.valueOf( convoy.args().size() );
    Integer vc = Integer.valueOf( valueCounter );
    Long at = Long.valueOf( traceTime3 - traceTime0 );
    Long t1 = Long.valueOf( traceTime1 - traceTime0 );
    Long t2 = Long.valueOf( traceTime2 - traceTime1 );
    Long t3 = Long.valueOf( traceTime3 - traceTime2 );
    logger().info( MSG_EXEC_QUERY_INFO, aQueryId, interval, pc, vc, at, t1, t2, t3 );
  }

  @SuppressWarnings( "nls" )
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void cancel( IS5FrontendRear aFrontend, String aQueryId ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aQueryId );
    IS5SequenceReadQuery histdataQuery = histDataSupport.cancelReadQuery( aQueryId );
    IS5SequenceReadQuery eventsQuery = eventsSupport.cancelReadQuery( aQueryId );
    IS5SequenceReadQuery commandsQuery = commandsSupport.cancelReadQuery( aQueryId );
    if( histdataQuery != null ) {
      logger().error( ERR_CANCEL_QUERY, "cancel(...)", aQueryId, "histdata" );
    }
    if( eventsQuery != null ) {
      logger().error( ERR_CANCEL_QUERY, "cancel(...)", aQueryId, "events" );
    }
    if( commandsQuery != null ) {
      logger().error( ERR_CANCEL_QUERY, "cancel(...)", aQueryId, "commands" );
    }
    S5BaQueriesData baData =
        aFrontend.frontendData().findBackendAddonData( IBaQueries.ADDON_ID, S5BaQueriesData.class );
    synchronized (baData) {
      S5BaQueriesConvoy convoy = baData.openQueries.getByKey( aQueryId );
      convoy.cancel();
    }
    logger().error( ERR_CANCEL_QUERY, "cancel(...)", aQueryId, TsLibUtils.EMPTY_STRING ); //$NON-NLS-1$
  }

  @Override
  public void close( IS5FrontendRear aFrontend, String aQueryId ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aQueryId );
    S5BaQueriesData baData =
        aFrontend.frontendData().findBackendAddonData( IBaQueries.ADDON_ID, S5BaQueriesData.class );
    synchronized (baData) {
      S5BaQueriesConvoy convoy = baData.openQueries.removeByKey( aQueryId );
      if( convoy != null ) {
        convoy.close();
      }
    }
    logger().info( MSG_CLOSE_QUERY, aQueryId );
  }

  // ------------------------------------------------------------------------------------
  // Внутренняя реализация
  //
  @SuppressWarnings( "unchecked" )
  private EGwidKind getGwidKind( IS5SequenceReader<IS5Sequence<?>, ?> aReader ) {
    TsNullArgumentRtException.checkNull( aReader );
    if( aReader == (IS5SequenceReader<IS5Sequence<?>, ?>)(Object)histDataSupport ) {
      return EGwidKind.GW_RTDATA;
    }
    if( aReader == (IS5SequenceReader<IS5Sequence<?>, ?>)(Object)eventsSupport ) {
      return EGwidKind.GW_EVENT;
    }
    if( aReader == (IS5SequenceReader<IS5Sequence<?>, ?>)(Object)commandsSupport ) {
      return EGwidKind.GW_CMD;
    }
    throw new TsNotAllEnumsUsedRtException();
  }

  @SuppressWarnings( "unchecked" )
  private IS5SequenceReader<IS5Sequence<?>, ?> findReaderByGwidKind( EGwidKind aGwidKind ) {
    TsNullArgumentRtException.checkNull( aGwidKind );
    if( aGwidKind == EGwidKind.GW_RTDATA ) {
      return (IS5SequenceReader<IS5Sequence<?>, ?>)(Object)histDataSupport;
    }
    if( aGwidKind == EGwidKind.GW_EVENT ) {
      return (IS5SequenceReader<IS5Sequence<?>, ?>)(Object)eventsSupport;
    }
    if( aGwidKind == EGwidKind.GW_CMD ) {
      return (IS5SequenceReader<IS5Sequence<?>, ?>)(Object)commandsSupport;
    }
    return null;
  }

  private static IStringMap<ITimedList<ITemporal<?>>> getValuesMap( IList<IS5BackendQueriesFunction> aFunctions,
      IList<ITimedList<ITemporal<?>>> aValues ) {
    IStringMapEdit<ITimedList<ITemporal<?>>> retValue = new StringMap<>();
    for( int index = 0, n = aFunctions.size(); index < n; index++ ) {
      String paramName = aFunctions.get( index ).arg().left();
      retValue.put( paramName, aValues.get( index ) );
    }
    return retValue;
  }

  private IMap<IS5SequenceReader<IS5Sequence<?>, ?>, IList<IS5BackendQueriesFunction>> getQueryFunctions(
      S5BaQueriesConvoy aQuery, S5BackendQuriesCounter aCounter ) {
    TsNullArgumentRtException.checkNulls( aQuery, aCounter );
    IOptionSet options = aQuery.params();
    IStringMap<IDtoQueryParam> params = aQuery.args();
    ITimeInterval interval = aQuery.interval();
    IMapEdit<IS5SequenceReader<IS5Sequence<?>, ?>, IList<IS5BackendQueriesFunction>> retValue = new ElemMap<>();
    for( String paramId : params.keys() ) {
      IDtoQueryParam param = params.getByKey( paramId );
      Gwid gwid = param.dataGwid();
      EGwidKind gwidKind = gwid.kind();
      IS5SequenceReader<IS5Sequence<?>, ?> reader = findReaderByGwidKind( gwidKind );
      if( reader == null ) {
        continue;
      }
      IListEdit<IS5BackendQueriesFunction> functions =
          (IListEdit<IS5BackendQueriesFunction>)retValue.findByKey( reader );
      if( functions == null ) {
        functions = new ElemArrayList<>();
        retValue.put( reader, functions );
      }
      switch( gwidKind ) {
        case GW_RTDATA:
          ISkClassInfo classInfo = sysdescrReader.getClassInfo( gwid.classId() );
          EAtomicType type = classInfo.rtdata().list().getByKey( gwid.propId() ).dataType().atomicType();
          functions.add(
              new S5BackendQueriesAtomicValueFunctions( paramId, param, type, interval, aCounter, options, logger() ) );
          break;
        case GW_EVENT:
          throw new TsUnderDevelopmentRtException();
        case GW_CMD:
          throw new TsUnderDevelopmentRtException();
        case GW_ATTR:
        case GW_CLASS:
        case GW_CLOB:
        case GW_CMD_ARG:
        case GW_EVENT_PARAM:
        case GW_LINK:
        case GW_RIVET:
          continue;
        default:
          throw new TsNotAllEnumsUsedRtException();
      }
    }
    return retValue;
  }

  /**
   * Читает значения последовательностей данных в указанном диапазоне времени и применяет к ним функции обработки.
   *
   * @param aQueryId String идентификатор запроса
   * @param aSequenceReader {@link IS5SequenceReader} читатель последовательности значений
   * @param aFunctions {@link IList}&lt;{@link IS5BackendQueriesFunction}&gt; список функций обработки.
   * @param aInterval {@link IQueryInterval} интервал запрашиваемых данных
   * @throws TsIllegalArgumentRtException aStartTime > aEndTime
   * @return {@link IList}&lt;{@link ITimedList}&lt;{@link ITemporal}&gt;&gt; список списков обработанных значений
   *         данных. Порядок и количество первого списка соответствует списку функций обработки.
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException количество запрашиваемых данных не соответствует количеству агрегаторов
   * @throws TsIllegalArgumentRtException неверный интервал запроса
   */
  private IList<ITimedList<ITemporal<?>>> readByFunctions( String aQueryId,
      IS5SequenceReader<IS5Sequence<?>, ?> aSequenceReader, IList<IS5BackendQueriesFunction> aFunctions,
      IQueryInterval aInterval ) {
    TsNullArgumentRtException.checkNulls( aQueryId, aSequenceReader, aFunctions, aInterval );
    // Количество запрашиваемых данных
    int count = aFunctions.size();
    if( count == 0 ) {
      // Частный случай, ничего не запрашивается
      return IList.EMPTY;
    }
    // Формирование карты списков функций по читаемым данным
    IMapEdit<Gwid, IListEdit<IS5BackendQueriesFunction>> functionsByGwids = new ElemMap<>();
    for( int index = 0, n = count; index < n; index++ ) {
      // Функция обработки данного
      IS5BackendQueriesFunction function = aFunctions.get( index );
      // Идентификатор данного
      Gwid gwid = function.arg().right().dataGwid();
      // Формирование общего списка функции для данного
      IListEdit<IS5BackendQueriesFunction> functions = functionsByGwids.findByKey( gwid );
      if( functions == null ) {
        functions = new ElemArrayList<>();
        functionsByGwids.put( gwid, functions );
      }
      functions.add( function );
    }
    long traceStartTime = System.currentTimeMillis();
    // Список описаний запрашиваемых данных
    IGwidList gwids = new GwidList( functionsByGwids.keys() );
    // Запрос данных внутри и на границах интервала
    IList<?> sequences =
        aSequenceReader.readSequences( IS5FrontendRear.NULL, aQueryId, gwids, aInterval, ACCESS_TIMEOUT_DEFAULT );
    // Исполнитель s5-потоков чтения данных
    S5ReadThreadExecutor<IList<ITimedList<ITemporal<?>>>> executor =
        new S5ReadThreadExecutor<>( readExecutor, logger() );
    for( int index = 0, n = gwids.size(); index < n; index++ ) {
      Gwid gwid = gwids.get( index );
      // Последовательность значений данного
      IS5Sequence<?> sequence = (IS5Sequence<?>)sequences.get( index );
      // Функции значений данного
      IListEdit<IS5BackendQueriesFunction> functions = functionsByGwids.getByKey( gwid );
      // Регистрация потока
      executor.add( new S5BackendQueriesFunctionThread( sequence, functions ) );
    }
    // Журнал
    Integer tc = Integer.valueOf( executor.threadCount() );
    Integer ac = Integer.valueOf( aFunctions.size() );
    logger().info( MSG_FUNC_READ_SEQUENCE_START, tc, ac );
    // Запуск потоков (с ожиданием завершения, c поднятием исключений на ошибках потоков)
    executor.run( true, true );
    // Результаты чтения и обработки
    IList<IList<ITimedList<ITemporal<?>>>> executorsResults = executor.results();

    // Формирование результата
    IListEdit<ITimedList<ITemporal<?>>> retValue = new ElemArrayList<>( count );
    for( int index = 0; index < count; index++ ) {
      // Функция обработки данного
      IS5BackendQueriesFunction function = aFunctions.get( index );
      // Идентификатор данного
      Gwid gwid = function.arg().right().dataGwid();
      // Индекс описания в списке запроса данных
      int gwidIndex = functionsByGwids.keys().indexOf( gwid );
      // Функции значений данного
      IListEdit<IS5BackendQueriesFunction> functions = functionsByGwids.values().get( gwidIndex );
      // Индекс функции в списке функций запроса
      int functionIndex = functions.indexOf( function );
      // Добавление в результат
      retValue.add( executorsResults.get( gwidIndex ).get( functionIndex ) );
    }
    // Журналирование
    Long traceTimeout = Long.valueOf( System.currentTimeMillis() - traceStartTime );
    logger().debug( MSG_FUNC_READ_SEQUENCE_TIME, Integer.valueOf( count ), traceTimeout );
    // Возвращаение результата
    return retValue;
  }

  @SuppressWarnings( "unchecked" )
  private static void fireNextDataMessage( IS5FrontendRear aFrontend, String aQueryId, EGwidKind aGwidKind,
      IStringMap<ITimedList<ITemporal<?>>> aValues, boolean aFinished ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aQueryId, aGwidKind, aValues );

    GtMessage message = null;
    switch( aGwidKind ) {
      case GW_RTDATA:
        message = BaMsgQueryNextData.INSTANCE.makeMessageAtomicValues( aQueryId,
            (IStringMap<ITimedList<ITemporalAtomicValue>>)(Object)aValues, aFinished );
        break;
      case GW_EVENT:
        message = BaMsgQueryNextData.INSTANCE.makeMessageEvents( aQueryId,
            (IStringMap<ITimedList<SkEvent>>)(Object)aValues, aFinished );
        break;
      case GW_CMD:
        message = BaMsgQueryNextData.INSTANCE.makeMessageCommands( aQueryId,
            (IStringMap<ITimedList<IDtoCompletedCommand>>)(Object)aValues, aFinished );
        break;
      case GW_ATTR:
      case GW_CLASS:
      case GW_CLOB:
      case GW_CMD_ARG:
      case GW_EVENT_PARAM:
      case GW_LINK:
      case GW_RIVET:
      default:
        break;
    }
    if( message != null ) {
      // Передача текущих значений фронтенду
      aFrontend.onBackendMessage( message );
    }

  }

}
