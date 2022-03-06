package org.toxsoft.uskat.s5.server.backend.supports.histdata;

import static java.lang.String.*;
import static org.toxsoft.core.tslib.bricks.time.EQueryIntervalType.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.addons.realtime.S5RealtimeUtils.*;
import static org.toxsoft.uskat.s5.server.backend.supports.histdata.IS5HistDataHardConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.histdata.IS5HistDataInterceptor.*;
import static org.toxsoft.uskat.s5.server.backend.supports.histdata.IS5Resources.*;
import static ru.uskat.core.api.rtdata.ISkRtdataHardConstants.*;

import java.util.concurrent.TimeUnit;

import javax.ejb.*;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.TsInternalErrorRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ELogSeverity;
import org.toxsoft.uskat.s5.server.backend.supports.currdata.IS5BackendCurrDataSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.sequences.S5HistDataAggregatorsThread;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.sequences.S5HistDataSequenceFactory;
import org.toxsoft.uskat.s5.server.backend.supports.objects.IS5BackendObjectsSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.IS5BackendSysDescrSingleton;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.interceptors.S5InterceptorSupport;
import org.toxsoft.uskat.s5.server.sequences.ISequenceBlockEdit;
import org.toxsoft.uskat.s5.server.sequences.ISequenceFactory;
import org.toxsoft.uskat.s5.server.sequences.impl.S5BackendSequenceSupportSingleton;
import org.toxsoft.uskat.s5.server.sequences.reader.IS5SequenceReadQuery;
import org.toxsoft.uskat.s5.utils.threads.impl.S5ReadThreadExecutor;

import ru.uskat.backend.messages.SkMessageHistDataQueryFinished;
import ru.uskat.common.dpu.rt.events.*;

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
@ConcurrencyManagement( ConcurrencyManagementType.BEAN )
// @ConcurrencyManagement( ConcurrencyManagementType.CONTAINER )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@Lock( LockType.READ )
public class S5BackendHistDataSingleton
    extends S5BackendSequenceSupportSingleton<IS5HistDataSequence, ITemporalAtomicValue>
    implements IS5BackendHistDataSingleton {

  private static final long serialVersionUID = 157157L;

  /**
   * Имя синглетона в контейнере сервера для организации зависимостей (@DependsOn)
   */
  public static final String BACKEND_HISTDATA_ID = "S5BackendHistDataSingleton"; //$NON-NLS-1$

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
   * Поддержка интерсепторов операций проводимых над данными
   */
  private final S5InterceptorSupport<IS5HistDataInterceptor> interceptors = new S5InterceptorSupport<>();

  /**
   * Конструктор.
   */
  public S5BackendHistDataSingleton() {
    super( BACKEND_HISTDATA_ID, STR_D_BACKEND_HISTDATA );
  }

  // ------------------------------------------------------------------------------------
  // Определение шаблонных методов S5BackendSupportSingleton
  //
  @Override
  protected void doInitSupport() {
    // Инициализация базового класса
    super.doInitSupport();
  }

  @Override
  protected void doCloseSupport() {
    // nop
  }

  @Override
  protected IS5BackendHistDataSingleton getBusinessObject() {
    return sessionContext().getBusinessObject( IS5BackendHistDataSingleton.class );
  }

  @Override
  protected ISequenceFactory<ITemporalAtomicValue> doCreateFactory() {
    return new S5HistDataSequenceFactory( backend().initialConfig().impl(), sysdescrReader() );
  }

  // ------------------------------------------------------------------------------------
  // IS5BackendHistDataSingleton
  //
  @SuppressWarnings( "unchecked" )
  // 2020-09-15 mvk попытка вернуть блокировку READ после разделения backend на currdata и histdata
  // @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  // 2020-09-15 mvk
  // в usecase "запуск центрального сервера tm c ~30 клиентами" было обнаружено, что при блокировке
  // LockType.READ, могут возникать deadlock на уровне синглетонов поддержки бекенда. Вероятными причинами может быть:
  // 1. Возникающий дефицит потоков в сервере приложения
  // 2. Нереентерабельный код в интерсепторах
  // 3. Блокировка на уровен СУБД (опять же дефицит потоков/соединений/транзакциях jdbc).
  // @Lock( LockType.READ )
  // 2020-09-15 mvk попытка вернуть блокировку READ после разделения backend на currdata и histdata
  // @Lock( LockType.WRITE )
  @Lock( LockType.READ )
  @Override
  public void writeHistData( DpuWriteHistData aValues ) {
    TsNullArgumentRtException.checkNull( aValues );
    try {
      // Пред-вызов интерсепторов
      if( !callBeforeWriteHistData( interceptors, aValues ) ) {
        // Интерсепторы отклонили запись значений хранимых данных
        logger().debug( MSG_REJECT_HISTDATA_WRITE_BY_INTERCEPTORS );
        return;
      }
      if( logger().isSeverityOn( ELogSeverity.INFO ) ) {
        // Вывод в лог сохраняемых данных
        logger().info( toStr( MSG_WRITE_HISTDATA_VALUES, aValues ) );
      }
      // Карта последовательностей данных. Ключ: идентификатор данного; Значение: последовательность значений
      IListEdit<IS5HistDataSequence> sequences = new ElemLinkedList<>();
      for( Gwid gwid : aValues.keys() ) {
        DpuWriteHistDataValues dataValues = aValues.getByKey( gwid );
        // Интервал записи
        ITimeInterval ti = dataValues.interval();
        IQueryInterval interval = new QueryInterval( CSCE, ti.startTime(), ti.endTime() );
        ISequenceBlockEdit<ITemporalAtomicValue> block = factory().createBlock( gwid, dataValues.values() );
        sequences.add( (IS5HistDataSequence)factory().createSequence( gwid, interval, new ElemArrayList<>( block ) ) );
      }
      writeSequences( sequences );

      // Пост-вызов интерсепторов
      callAfterWriteHistData( interceptors, aValues, logger() );
    }
    catch( Throwable e ) {
      // Неожиданная ошибка записи хранимых данных
      logger().error( e, ERR_WRITE_UNEXPECTED, cause( e ) );
      throw new TsInternalErrorRtException( e, ERR_WRITE_UNEXPECTED, cause( e ) );
    }
  }

  @Override
  public S5HistDataSequenceFactory histdataSequenceFactory() {
    return factory();
  }

  @Override
  @Asynchronous
  public void execHistData( IS5FrontendRear aFrontend, String aQueryId, IQueryInterval aQueryInterval, IGwidList aGwids,
      IOptionSet aParams ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aQueryId, aQueryInterval, aGwids, aParams );
    long timeout = OP_SK_HDQUERY_CHECK_VALID_GWIDS.getValue( aParams ).asLong();
    try {
      IList<IS5HistDataSequence> sequences = readSequences( aFrontend, aQueryId, aGwids, aQueryInterval, timeout );
      // Формирование результата пользователю
      DpuHistData result = new DpuHistData();
      for( int index = 0, n = aGwids.size(); index < n; index++ ) {
        result.put( aGwids.get( index ), sequences.get( index ).get( aQueryInterval ) );
      }
      // Передача результата пользователю
      SkMessageHistDataQueryFinished.sendResult( aFrontend, aQueryId, result );
    }
    catch( Throwable e ) {
      // Вывод стека запроса завершения
      StringBuilder sb = new StringBuilder();
      sb.append( String.format( "execHistData(...): unexpected error: '%s'. trace:\n", cause( e ) ) ); //$NON-NLS-1$
      // Стек текущего потока
      StackTraceElement[] stack = e.getStackTrace();
      // Формирование стека
      for( int index = 0, n = stack.length; index < n; index++ ) {
        sb.append( format( MSG_STACK_ITEM, stack[index] ) );
      }
      String errTrace = sb.toString();
      // Вывод в журнал
      logger().error( errTrace );
      // Неожиданная ошибка выполнения
      SkMessageHistDataQueryFinished.sendError( aFrontend, aQueryId, HDQUERY_UNEXPECTED_ERROR, errTrace );
    }
  }

  @Override
  public void cancelHistDataResult( String aQueryId ) {
    TsNullArgumentRtException.checkNull( aQueryId );
    IS5SequenceReadQuery query = cancelReadQuery( aQueryId );
    if( query != null ) {
      // Отмена выполнения запроса
      SkMessageHistDataQueryFinished.sendError( query.frontend(), aQueryId, HDQUERY_CANCEL, ERR_HISTDATA_CANCEL_QUERY );
    }
  }

  @Override
  public IList<IList<ITemporalAtomicValue>> readAggregatedValues( IList<Pair<Gwid, IS5HistDataAggregator>> aGwids,
      IQueryInterval aInterval ) {
    TsNullArgumentRtException.checkNulls( aGwids, aInterval );
    // Количество запрашиваемых данных
    int count = aGwids.size();
    if( count == 0 ) {
      // Частный случай, ничего не запрашивается
      return IList.EMPTY;
    }
    // Формирование карты списков агрегаторов по читаемым данным
    IMapEdit<Gwid, IListEdit<IS5HistDataAggregator>> aggregatorsByGwids = new ElemMap<>();
    for( int index = 0, n = count; index < n; index++ ) {
      Pair<Gwid, IS5HistDataAggregator> ga = aGwids.get( index );
      // Идентификатор данного
      Gwid gwid = ga.left();
      // Агрегатор данного
      IS5HistDataAggregator aggregator = ga.right();
      // Формирование общего списка агрегаторов для данного
      IListEdit<IS5HistDataAggregator> aggregators = aggregatorsByGwids.findByKey( gwid );
      if( aggregators == null ) {
        aggregators = new ElemArrayList<>();
        aggregatorsByGwids.put( gwid, aggregators );
      }
      aggregators.add( aggregator );
    }
    long traceStartTime = System.currentTimeMillis();
    // Список описаний запрашиваемых данных
    IGwidList gwids = new GwidList( aggregatorsByGwids.keys() );
    // Запрос данных внутри и на границах интервала
    IList<IS5HistDataSequence> sequences = readSequences( gwids, aInterval, ACCESS_TIMEOUT_DEFAULT );
    // Исполнитель s5-потоков чтения данных
    S5ReadThreadExecutor<IList<IList<ITemporalAtomicValue>>> executor =
        new S5ReadThreadExecutor<>( readExecutor(), logger() );
    for( int index = 0, n = gwids.size(); index < n; index++ ) {
      Gwid gwid = gwids.get( index );
      // Последовательность значений данного
      IS5HistDataSequence sequence = sequences.get( index );
      // Агрегаторы значений данного
      IListEdit<IS5HistDataAggregator> aggregators = aggregatorsByGwids.getByKey( gwid );
      // Регистрация потока
      executor.add( new S5HistDataAggregatorsThread( sequence, aggregators ) );
    }
    // Журнал
    Integer tc = Integer.valueOf( executor.threadCount() );
    Integer ac = Integer.valueOf( aGwids.size() );
    logger().info( MSG_AGGR_READ_SEQUENCE_START, tc, ac );
    // Запуск потоков (с ожиданием завершения, c поднятием исключений на ошибках потоков)
    executor.run( true, true );
    // Результаты чтения и агрегации
    IList<IList<IList<ITemporalAtomicValue>>> executorsResults = executor.results();

    // Формирование результата
    IListEdit<IList<ITemporalAtomicValue>> retValue = new ElemArrayList<>( count );
    for( int index = 0; index < count; index++ ) {
      Pair<Gwid, IS5HistDataAggregator> ga = aGwids.get( index );
      // Идентификатор данного
      Gwid gwid = ga.left();
      // Агрегатор данного
      IS5HistDataAggregator aggregator = ga.right();
      // Индекс описания в списке запроса данных
      int gwidIndex = aggregatorsByGwids.keys().indexOf( gwid );
      // Агрегаторы значений данного
      IListEdit<IS5HistDataAggregator> aggregators = aggregatorsByGwids.values().get( gwidIndex );
      // Индекс агрегатора в списке агрегаторов запроса
      int aggregatorIndex = aggregators.indexOf( aggregator );
      // Добавление в результат
      retValue.add( executorsResults.get( gwidIndex ).get( aggregatorIndex ) );
    }
    // Журналирование
    Long traceTimeout = Long.valueOf( System.currentTimeMillis() - traceStartTime );
    logger().debug( MSG_AGGR_READ_SEQUENCE_TIME, Integer.valueOf( count ), traceTimeout );
    // Возвращаение результата
    return retValue;
  }

  @Override
  public void addHistDataInterceptor( IS5HistDataInterceptor aInterceptor, int aPriority ) {
    TsNullArgumentRtException.checkNull( aInterceptor );
    interceptors.add( aInterceptor, aPriority );
  }

  @Override
  public void removeHistDataInterceptor( IS5HistDataInterceptor aInterceptor ) {
    TsNullArgumentRtException.checkNull( aInterceptor );
    interceptors.remove( aInterceptor );
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
}
