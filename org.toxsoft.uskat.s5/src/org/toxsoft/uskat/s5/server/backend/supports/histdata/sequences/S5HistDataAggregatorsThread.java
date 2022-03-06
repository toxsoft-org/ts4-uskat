package org.toxsoft.uskat.s5.server.backend.supports.histdata.sequences;

import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.histdata.sequences.IS5Resources.*;

import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.bricks.time.impl.TimeInterval;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.coll.primtypes.ILongListEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.LongArrayList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ELogSeverity;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.IS5HistDataAggregator;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.IS5HistDataSequence;
import org.toxsoft.uskat.s5.server.sequences.ISequenceBlock;
import org.toxsoft.uskat.s5.utils.threads.impl.S5AbstractReadThread;

/**
 * Поток чтения последовательности агрегированных значений одного данного
 * <p>
 * Конструктор потока требует указать список агрегаторов для данного. Возвращаемый результ (список последовательностей)
 * соответствует каждому из агрегаторов.
 *
 * @author mvk
 */
public class S5HistDataAggregatorsThread
    extends S5AbstractReadThread<IList<IList<ITemporalAtomicValue>>> {

  private final IS5HistDataSequence          source;
  private final IList<IS5HistDataAggregator> aggregators;

  private final ITimeInterval                          interval;
  private final ILongListEdit                          aggregatorStartTimes;
  private final ILongListEdit                          aggregatorEndTimes;
  private final IListEdit<IList<ITemporalAtomicValue>> result;

  /**
   * Создание асинхронной задачи объединения блоков последовательности данных
   *
   * @param aSource {@link IS5HistDataSequence} исходная последовательность данных
   * @param aAggregators {@link IList }&lt;{@link IS5HistDataAggregator}&gt; список аргегаторов
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException недопустимый интервал времени
   */
  public S5HistDataAggregatorsThread( IS5HistDataSequence aSource, IList<IS5HistDataAggregator> aAggregators ) {
    TsNullArgumentRtException.checkNulls( aSource, aAggregators );
    source = aSource;
    aggregators = new ElemArrayList<>( aAggregators );
    int count = aAggregators.size();

    // Время последовательности
    ITimeInterval sequenceInterval = aSource.interval();
    long sequenceStartTime = sequenceInterval.startTime();
    long sequenceEndTime = sequenceInterval.endTime();
    // Определяем интервал запрашиваемх данных: необходимо начинать и завершать по времени которое определяется данным с
    // самим большим временем агрегации...
    long minStartTime = sequenceStartTime;
    long maxEndTime = sequenceEndTime;
    // ... и формируем интервалы в которых формируются агрегированные значения
    aggregatorStartTimes = new LongArrayList( count );
    aggregatorEndTimes = new LongArrayList( count );
    for( int index = 0; index < count; index++ ) {
      IS5HistDataAggregator aggregator = aAggregators.get( index );
      long aggregationStep = aggregator.aggregationStep();
      if( aggregationStep == 0 ) {
        // Интервал агрегации все данные запроса
        aggregatorStartTimes.add( sequenceStartTime );
        aggregatorEndTimes.add( sequenceEndTime );
        continue;
      }
      long aggregationStartTime = ((sequenceStartTime / aggregationStep) * aggregationStep);
      long aggregationEndTime = ((((sequenceEndTime / aggregationStep) * aggregationStep) + aggregationStep) - 1);
      if( minStartTime > aggregationStartTime ) {
        minStartTime = aggregationStartTime;
      }
      if( maxEndTime < aggregationEndTime ) {
        maxEndTime = aggregationEndTime;
      }
      aggregatorStartTimes.add( aggregationStartTime );
      aggregatorEndTimes.add( aggregationEndTime );
    }
    interval = new TimeInterval( minStartTime, maxEndTime );
    result = new ElemArrayList<>( count );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5AbstractServerThread
  //
  @Override
  protected void doRun() {
    try {
      int count = aggregators.size();
      // Идентификатор данного
      Gwid gwid = source.gwid();
      long traceStartTime = System.currentTimeMillis();
      long traceCreateStartTime = 0, traceCreateTimeout = 0;
      IListEdit<IListEdit<ITemporalAtomicValue>> resultValues = new ElemArrayList<>( count );
      for( int index = 0; index < count; index++ ) {
        resultValues.add( new ElemLinkedList<>() );
      }
      // Блоки последовательности
      IList<ISequenceBlock<ITemporalAtomicValue>> blocks = source.blocks();
      // Количество прочитанных значений
      int valueCount = 0;
      // Количество считанных блоков
      for( int blockIndex = 0, n = blocks.size(); blockIndex < n; blockIndex++ ) {
        // Подсчет считанных значений
        valueCount += blocks.get( blockIndex ).size();
      }
      // Проход по всем агрегатором поставляя значения блока для агрегации
      for( int aggregatorIndex = 0; aggregatorIndex < count; aggregatorIndex++ ) {
        // Агрегатор
        IS5HistDataAggregator aggregator = aggregators.get( aggregatorIndex );
        // Агрегированные значения (результат)
        IListEdit<ITemporalAtomicValue> aggregatorValues = resultValues.get( aggregatorIndex );
        // Время агрегации
        long aggregationStartTime = aggregatorStartTimes.getValue( aggregatorIndex );
        // Время агрегации
        long aggregationEndTime = aggregatorEndTimes.getValue( aggregatorIndex );
        // Обработка считанных блоков
        for( int blockIndex = 0, n = blocks.size(); blockIndex < n; blockIndex++ ) {
          IHistDataBlock histDataBlock = (IHistDataBlock)blocks.get( blockIndex );
          // Время с которого должны считываться данные (включительно)
          long startImportTime = Math.max( aggregationStartTime, histDataBlock.startTime() );
          // Время по которое должны считываться данные (включительно)
          long endImportTime = Math.min( aggregationEndTime, histDataBlock.endTime() );
          if( startImportTime > histDataBlock.endTime() ) {
            // Текущий блок не может предоставить данные для агрегации. Переход на следующий блок
            continue;
          }
          // Инициализация импорта
          histDataBlock.setImportTime( startImportTime );
          // Импорт и агрегация
          while( histDataBlock.hasImport() ) {
            ITemporalValueImporter importer = histDataBlock.nextImport();
            if( importer.timestamp() > endImportTime ) {
              // Данные для импорта закончились
              break;
            }
            IList<ITemporalAtomicValue> values = aggregator.aggregate( importer );
            if( values.size() > 0 ) {
              for( ITemporalAtomicValue value : values ) {
                aggregatorValues.add( value );
              }
            }
          }
        }
      }
      // Значение признак: больше нет значений в последовательности
      for( int index = 0; index < count; index++ ) {
        IS5HistDataAggregator aggregator = aggregators.get( index );
        IList<ITemporalAtomicValue> values = aggregator.aggregate( ITemporalValueImporter.NULL );
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
        // IS5HistDataAggregator aggregator = aggregators.get( index );
        // // Интервал агрегации
        // long step = aggregator.aggregationStep();
        // Значения для последовательности
        IList<ITemporalAtomicValue> aggregatorValues = resultValues.get( index );
        // Добавление последовательности в результат
        result.add( aggregatorValues );
        // Количество агрегированных значений
        aggregateValueCount += aggregatorValues.size();
      }
      traceCreateTimeout = System.currentTimeMillis() - traceCreateStartTime;

      if( logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
        Integer bc = Integer.valueOf( blocks.size() );
        Integer vc = Integer.valueOf( valueCount );
        Integer ac = Integer.valueOf( aggregators.size() );
        Integer avc = Integer.valueOf( aggregateValueCount );
        Integer index = Integer.valueOf( threadIndex() );
        Long ta = Long.valueOf( System.currentTimeMillis() - traceStartTime );
        Long tc = Long.valueOf( traceCreateTimeout );
        String s = valuesToString( result );
        logger().debug( MSG_AGGR_THREAD_FINISH, index, gwid, interval, bc, vc, ac, avc, ta, tc, s );
      }
    }
    catch( Throwable e ) {
      throw new TsInternalErrorRtException( e, ERR_AGGR_UNEXPECTED, source.gwid(), interval, cause( e ) );
    }
  }

  @Override
  protected void doCancel() {
    // nop
  }

  @Override
  public IList<IList<ITemporalAtomicValue>> result() {
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
  private static String valuesToString( IList<IList<ITemporalAtomicValue>> aValues ) {
    TsNullArgumentRtException.checkNull( aValues );
    StringBuilder sb = new StringBuilder();
    for( int index = 0, n = aValues.size(); index < n; index++ ) {
      IList<ITemporalAtomicValue> values = aValues.get( index );
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
