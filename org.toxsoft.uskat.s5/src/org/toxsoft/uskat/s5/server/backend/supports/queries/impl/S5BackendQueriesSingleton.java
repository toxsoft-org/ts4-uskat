package org.toxsoft.uskat.s5.server.backend.supports.queries.impl;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.queries.impl.IS5Resources.*;

import java.util.concurrent.TimeUnit;

import javax.ejb.*;
import javax.enterprise.concurrent.ManagedExecutorService;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.strid.idgen.IStridGenerator;
import org.toxsoft.core.tslib.bricks.strid.idgen.UuidStridGenerator;
import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.bricks.time.ITemporal;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.hqserv.IDtoQueryParam;
import org.toxsoft.uskat.core.backend.api.IBaQueries;
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
    // Текущее время
    long currTime = System.currentTimeMillis();
    // Обработка данных фронтендов
    for( IS5FrontendRear frontend : backend().attachedFrontends() ) {
      // Данные расширения для сессии
      S5BaQueriesData baData =
          frontend.frontendData().findBackendAddonData( IBaQueries.ADDON_ID, S5BaQueriesData.class );
      if( baData == null ) {
        // фронтенд не поддерживает обработку текущих данных
        continue;
      }
      GtMessage message = null;
      IStringMap<S5BaQueriesConvoy> openQueries;
      synchronized (baData) {
        openQueries = new StringMap<>( baData.openQueries );
      }
      for( S5BaQueriesConvoy openQuery : openQueries ) {
        if( message != null ) {
          // Передача текущих значений фронтенду
          frontend.onBackendMessage( message );
        }
      }
    }
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
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  @Asynchronous
  public void execQuery( IS5FrontendRear aFrontend, String aQueryId, IQueryInterval aTimeInterval ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aQueryId, aTimeInterval );
    S5BaQueriesData baData =
        aFrontend.frontendData().findBackendAddonData( IBaQueries.ADDON_ID, S5BaQueriesData.class );
    // Конвой-объект
    S5BaQueriesConvoy convoy = null;
    synchronized (baData) {
      convoy = baData.openQueries.getByKey( aQueryId );
      convoy.exec( aTimeInterval );
    }
    // Интервал запроса
    IQueryInterval interval = convoy.interval();
    // Функции запроса
    IMap<IS5SequenceReader<IS5Sequence<?>, ?>, IList<IS5BackendQueriesFunction>> functions =
        getQueryFunctions( convoy.args().values(), interval );
    // Результат для передачи
    IListEdit<IList<ITemporal<?>>> retValue = new ElemArrayList<>();
    for( IS5SequenceReader<IS5Sequence<?>, ?> reader : functions.keys() ) {
      // Чтение значений функциями
      retValue.addAll( readByFunctions( reader, functions.getByKey( reader ), interval ) );
    }
    // TODO:
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void cancel( IS5FrontendRear aFrontend, String aQueryId ) {
    TsNullArgumentRtException.checkNull( aFrontend, aQueryId );
    S5BaQueriesData baData =
        aFrontend.frontendData().findBackendAddonData( IBaQueries.ADDON_ID, S5BaQueriesData.class );
    synchronized (baData) {
      S5BaQueriesConvoy convoy = baData.openQueries.getByKey( aQueryId );
      convoy.cancel();
    }
  }

  @Override
  public void close( IS5FrontendRear aFrontend, String aQueryId ) {
    TsNullArgumentRtException.checkNull( aFrontend, aQueryId );
    S5BaQueriesData baData =
        aFrontend.frontendData().findBackendAddonData( IBaQueries.ADDON_ID, S5BaQueriesData.class );
    synchronized (baData) {
      S5BaQueriesConvoy convoy = baData.openQueries.removeByKey( aQueryId );
      if( convoy != null ) {
        convoy.close();
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренняя реализация
  //
  private IMap<IS5SequenceReader<IS5Sequence<?>, ?>, IList<IS5BackendQueriesFunction>> getQueryFunctions(
      IList<IDtoQueryParam> aQueryParams, IQueryInterval aInterval ) {
    TsNullArgumentRtException.checkNulls( aQueryParams, aInterval );
    IMapEdit<IS5SequenceReader<IS5Sequence<?>, ?>, IList<IS5BackendQueriesFunction>> retValue = new ElemMap<>();
    return retValue;
  }

  /**
   * Читает значения последовательностей данных в указанном диапазоне времени и применяет к ним функции обработки.
   *
   * @param aSequenceReader {@link IS5SequenceReader} читатель последовательности значений
   * @param aFunctions {@link IList}&lt;{@link IS5BackendQueriesFunction}&gt; список функций обработки.
   * @param aInterval {@link IQueryInterval} интервал запрашиваемых данных
   * @throws TsIllegalArgumentRtException aStartTime > aEndTime
   * @return {@link IList}&lt;{@link IList}&lt;{@link ITemporal}&gt;&gt; список обработанных значений данных. Порядок
   *         элементов списка соответствует порядку элементам списка функций обработки.
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException количество запрашиваемых данных не соответствует количеству агрегаторов
   * @throws TsIllegalArgumentRtException неверный интервал запроса
   */
  private IList<IList<ITemporal<?>>> readByFunctions( IS5SequenceReader<IS5Sequence<?>, ?> aSequenceReader,
      IList<IS5BackendQueriesFunction> aFunctions, IQueryInterval aInterval ) {
    TsNullArgumentRtException.checkNulls( aFunctions, aInterval );
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
      Gwid gwid = function.arg().dataGwid();
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
    IList<?> sequences = aSequenceReader.readSequences( gwids, aInterval, ACCESS_TIMEOUT_DEFAULT );
    // Исполнитель s5-потоков чтения данных
    S5ReadThreadExecutor<IList<IList<ITemporal<?>>>> executor = new S5ReadThreadExecutor<>( readExecutor, logger() );
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
    IList<IList<IList<ITemporal<?>>>> executorsResults = executor.results();

    // Формирование результата
    IListEdit<IList<ITemporal<?>>> retValue = new ElemArrayList<>( count );
    for( int index = 0; index < count; index++ ) {
      // Функция обработки данного
      IS5BackendQueriesFunction function = aFunctions.get( index );
      // Идентификатор данного
      Gwid gwid = function.arg().dataGwid();
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

}
