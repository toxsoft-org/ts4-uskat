package org.toxsoft.uskat.s5.server.backend.supports.queries.impl;

import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.queries.impl.IS5Resources.*;

import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.TimeInterval;
import org.toxsoft.core.tslib.bricks.time.impl.TimedList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.primtypes.ILongListEdit;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ELogSeverity;
import org.toxsoft.uskat.core.api.hqserv.ISkHistoryQueryServiceConstants;
import org.toxsoft.uskat.s5.legacy.S5LongArrayList;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.IS5HistDataSequence;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.ITemporalValueImporter;
import org.toxsoft.uskat.s5.server.backend.supports.queries.IS5BackendQueriesFunction;
import org.toxsoft.uskat.s5.server.sequences.IS5Sequence;
import org.toxsoft.uskat.s5.server.sequences.ISequenceBlock;
import org.toxsoft.uskat.s5.utils.threads.impl.S5AbstractReadThread;

/**
 * Поток выполнения функций запросов значений одного данного
 * <p>
 * Конструктор потока требует указать список функций для обработки значений данного. Возвращаемый результ (список
 * последовательностей) соответствует каждой из функций обработки.
 *
 * @author mvk
 */
public class S5BackendQueriesFunctionThread
    extends S5AbstractReadThread<IList<ITimedList<ITemporal<?>>>> {

  private final IS5Sequence<?>                   source;
  private final IList<IS5BackendQueriesFunction> functions;

  private final ITimeInterval                       interval;
  private final ILongListEdit                       startTimes;
  private final ILongListEdit                       endTimes;
  private final IListEdit<ITimedList<ITemporal<?>>> result;

  /**
   * Создание асинхронной задачи выполнения функций обработки значений данного
   *
   * @param aSource {@link IS5HistDataSequence} исходная последовательность данных
   * @param aFunctions {@link IList }&lt;{@link IS5BackendQueriesFunction}&gt; список функций обработки значений
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException недопустимый интервал времени
   */
  public S5BackendQueriesFunctionThread( IS5Sequence<?> aSource, IList<IS5BackendQueriesFunction> aFunctions ) {
    TsNullArgumentRtException.checkNulls( aSource, aFunctions );
    source = aSource;
    functions = new ElemArrayList<>( aFunctions );
    int count = aFunctions.size();

    // Время последовательности
    ITimeInterval sequenceInterval = aSource.interval();
    long sequenceStartTime = sequenceInterval.startTime();
    long sequenceEndTime = sequenceInterval.endTime();
    // Определяем интервал запрашиваемх данных: необходимо начинать и завершать по времени которое определяется данным с
    // самим большим временем обработки...
    long minStartTime = sequenceStartTime;
    long maxEndTime = sequenceEndTime;
    // ... и формируем интервалы в которых формируются обработанные значения
    startTimes = new S5LongArrayList( count );
    endTimes = new S5LongArrayList( count );
    for( int index = 0; index < count; index++ ) {
      IS5BackendQueriesFunction function = aFunctions.get( index );
      long aggregationStep = ISkHistoryQueryServiceConstants.HQFUNC_ARG_AGGREGAION_INTERVAL
          .getValue( function.arg().right().funcArgs() ).asLong();
      if( aggregationStep == 0 ) {
        // Интервал обработки: все данные запроса
        startTimes.add( sequenceStartTime );
        endTimes.add( sequenceEndTime );
        continue;
      }
      long funcStartTime = ((sequenceStartTime / aggregationStep) * aggregationStep);
      long funcEndTime = ((((sequenceEndTime / aggregationStep) * aggregationStep) + aggregationStep) - 1);
      if( minStartTime > funcStartTime ) {
        minStartTime = funcStartTime;
      }
      if( maxEndTime < funcEndTime ) {
        maxEndTime = funcEndTime;
      }
      startTimes.add( funcStartTime );
      endTimes.add( funcEndTime );
    }
    interval = new TimeInterval( minStartTime, maxEndTime );
    result = new ElemArrayList<>( count );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5AbstractServerThread
  //
  @SuppressWarnings( "unchecked" )
  @Override
  protected void doRun() {
    try {
      int count = functions.size();
      // Идентификатор данного
      Gwid gwid = source.gwid();
      long traceStartTime = System.currentTimeMillis();
      long traceCreateStartTime = 0, traceCreateTimeout = 0;
      IListEdit<ITimedListEdit<ITemporal<?>>> resultValues = new ElemArrayList<>( count );
      for( int index = 0; index < count; index++ ) {
        resultValues.add( new TimedList<>() );
      }
      // Блоки последовательности
      IList<ISequenceBlock<?>> blocks = (IList<ISequenceBlock<?>>)(Object)source.blocks();
      // Количество прочитанных значений
      int valueCount = 0;
      // Количество считанных блоков
      for( int blockIndex = 0, n = blocks.size(); blockIndex < n; blockIndex++ ) {
        // Подсчет считанных значений
        valueCount += blocks.get( blockIndex ).size();
      }
      // Проход по всем функциям поставляя значения блока для обработки
      for( int index = 0; index < count; index++ ) {
        // Функция обработки
        IS5BackendQueriesFunction function = functions.get( index );
        // Значения-результат
        ITimedListEdit<ITemporal<?>> functionValues = resultValues.get( index );
        // Начальное время обработки
        long funcStartTime = startTimes.getValue( index );
        // Конечное время агрегации
        long funcEndTime = endTimes.getValue( index );
        // Обработка считанных блоков
        for( int blockIndex = 0, n = blocks.size(); blockIndex < n; blockIndex++ ) {
          ISequenceBlock<?> block = blocks.get( blockIndex );
          // Время с которого должны считываться данные (включительно)
          long startImportTime = Math.max( funcStartTime, block.startTime() );
          // Время по которое должны считываться данные (включительно)
          long endImportTime = Math.min( funcEndTime, block.endTime() );
          if( startImportTime > block.endTime() ) {
            // Текущий блок не может предоставить данные для обработки. Переход на следующий блок
            continue;
          }
          // Инициализация импорта
          block.setImportTime( startImportTime );
          // Импорт и агрегация
          while( block.hasImport() ) {
            ITemporalValueImporter importer = block.nextImport();
            if( importer.timestamp() > endImportTime ) {
              // Данные для импорта закончились
              break;
            }
            ITimedList<ITemporalAtomicValue> values = function.nextValue( importer );
            if( values.size() > 0 ) {
              for( ITemporalAtomicValue value : values ) {
                functionValues.add( value );
              }
            }
          }
        }
      }
      // Значение признак: больше нет значений в последовательности
      for( int index = 0; index < count; index++ ) {
        IS5BackendQueriesFunction aggregator = functions.get( index );
        IList<ITemporalAtomicValue> values = aggregator.nextValue( ITemporalValueImporter.NULL );
        if( values.size() > 0 ) {
          for( ITemporalAtomicValue value : values ) {
            // При завершении текущего интервала было сформировано последнее значение
            resultValues.get( index ).add( value );
          }
        }
      }
      // Создание последовательности и размещение в результате
      traceCreateStartTime = System.currentTimeMillis();
      // Общее количество агрегированных значений
      int aggregateValueCount = 0;
      for( int index = 0; index < count; index++ ) {
        // 2020-11-30 mvk
        // IS5BackendQueriesFunction aggregator = functions.get( index );
        // // Интервал агрегации
        // long step = aggregator.aggregationStep();
        // Значения для последовательности
        ITimedList<ITemporal<?>> aggregatorValues = resultValues.get( index );
        // Добавление последовательности в результат
        result.add( aggregatorValues );
        // Количество агрегированных значений
        aggregateValueCount += aggregatorValues.size();
      }
      traceCreateTimeout = System.currentTimeMillis() - traceCreateStartTime;

      if( logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
        Integer bc = Integer.valueOf( blocks.size() );
        Integer vc = Integer.valueOf( valueCount );
        Integer ac = Integer.valueOf( functions.size() );
        Integer avc = Integer.valueOf( aggregateValueCount );
        Integer index = Integer.valueOf( threadIndex() );
        Long ta = Long.valueOf( System.currentTimeMillis() - traceStartTime );
        Long tc = Long.valueOf( traceCreateTimeout );
        String s = valuesToString( result );
        logger().debug( MSG_FUNC_THREAD_FINISH, index, gwid, interval, bc, vc, ac, avc, ta, tc, s );
      }
    }
    catch( Throwable e ) {
      throw new TsInternalErrorRtException( e, ERR_FUNC_UNEXPECTED, source.gwid(), interval, cause( e ) );
    }
  }

  @Override
  protected void doCancel() {
    // nop
  }

  @Override
  public IList<ITimedList<ITemporal<?>>> result() {
    return result;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Вывод в строку текстового представления значений
   *
   * @param aValues {@link IList}&lt;S&gt; список последовательностей
   * @return String текстовое представление
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static String valuesToString( IList<ITimedList<ITemporal<?>>> aValues ) {
    TsNullArgumentRtException.checkNull( aValues );
    StringBuilder sb = new StringBuilder();
    for( int index = 0, n = aValues.size(); index < n; index++ ) {
      IList<ITemporal<?>> values = aValues.get( index );
      sb.append( "   [" ); //$NON-NLS-1$
      sb.append( index );
      sb.append( "]: " ); //$NON-NLS-1$
      sb.append( values );
      if( index + 1 < n ) {
        sb.append( "\n" ); //$NON-NLS-1$
      }
    }
    return sb.toString();
  }
}
