package org.toxsoft.uskat.s5.server.backend.supports.queries.impl;

import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.queries.impl.IS5Resources.*;

import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.bricks.time.ITemporal;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ELogSeverity;
import org.toxsoft.uskat.s5.server.backend.addons.queries.ES5QueriesConvoyState;
import org.toxsoft.uskat.s5.server.backend.addons.queries.S5BaQueriesConvoy;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.IS5HistDataSequence;
import org.toxsoft.uskat.s5.server.backend.supports.queries.IS5BackendQueriesFunction;
import org.toxsoft.uskat.s5.server.sequences.IS5Sequence;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceCursor;
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
    extends S5AbstractReadThread<IList<IList<ITemporal<?>>>> {

  private final S5BaQueriesConvoy                query;
  private final IList<IS5Sequence<?>>            sequences;
  private final IList<IS5BackendQueriesFunction> functions;
  private final IListEdit<IList<ITemporal<?>>>   result;

  /**
   * Создание асинхронной задачи выполнения функций обработки значений данного
   *
   * @param aQuery {@link S5BaQueriesConvoy} конвой-объекта запроса
   * @param aSequences {@link IList}&lt;{@link IS5HistDataSequence} спислк исходных последовательность значений данных.
   *          Если данные запрашивались по конкретному {@link Gwid} то в списке только одна последовательность.
   * @param aFunctions {@link IList}&lt;{@link IS5BackendQueriesFunction}&gt; список функций обработки значений
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException недопустимый интервал времени
   */
  public S5BackendQueriesFunctionThread( S5BaQueriesConvoy aQuery, IList<IS5Sequence<?>> aSequences,
      IList<IS5BackendQueriesFunction> aFunctions ) {
    TsNullArgumentRtException.checkNulls( aQuery, aSequences, aFunctions );
    query = aQuery;
    sequences = aSequences;
    functions = new ElemArrayList<>( aFunctions );
    int count = aFunctions.size();
    result = new ElemArrayList<>( count );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5AbstractServerThread
  //
  @SuppressWarnings( "unchecked" )
  @Override
  protected void doRun() {
    IQueryInterval interval = query.interval();
    // Идентификаторы данных последовательностей
    String sequencesGwidStr = sequencesGwidStr( sequences );
    try {
      int count = functions.size();
      long traceStartTime = System.currentTimeMillis();
      long traceCreateStartTime = 0, traceCreateTimeout = 0;
      // Создание последовательности и размещение в результате
      traceCreateStartTime = System.currentTimeMillis();
      // Общее количество обработанных блоков
      int resultBlockCount = 0;
      // Общее количество обработанных значений
      int resultValueCount = 0;
      // Создание списка курсоров чтения значений
      IListEdit<IS5SequenceCursor<?>> cursors = new ElemLinkedList<>();
      for( IS5Sequence<?> sequence : sequences ) {
        resultBlockCount += sequence.blocks().size();
        resultValueCount += sequence.valuesQtty();
        // Курсор значений
        cursors.add( sequence.createCursor() );
      }
      // Проход по всем функциям поставляя значения блока для обработки
      for( int index = 0; index < count; index++ ) {
        if( query.state() != ES5QueriesConvoyState.EXECUTING ) {
          // Запрос был отменен
          break;
        }
        // Функция обработки
        IS5BackendQueriesFunction function = functions.get( index );
        // Значения-результат
        IList<ITemporal<?>> values = function.evaluate( cursors );
        // Обработка значений последовательности
        result.addAll( values );
        // Количество обработанных значений
        resultValueCount += values.size();
      }
      traceCreateTimeout = System.currentTimeMillis() - traceCreateStartTime;

      if( logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
        Integer bc = Integer.valueOf( resultBlockCount );
        Integer vc = Integer.valueOf( resultValueCount );
        Integer ac = Integer.valueOf( functions.size() );
        Integer avc = Integer.valueOf( resultValueCount );
        Integer index = Integer.valueOf( threadIndex() );
        Long ta = Long.valueOf( System.currentTimeMillis() - traceStartTime );
        Long tc = Long.valueOf( traceCreateTimeout );
        String s = valuesToString( result );
        logger().debug( MSG_FUNC_THREAD_FINISH, index, sequencesGwidStr, interval, bc, vc, ac, avc, ta, tc, s );
      }
    }
    catch( Throwable e ) {
      throw new TsInternalErrorRtException( e, ERR_FUNC_UNEXPECTED, sequencesGwidStr, interval, cause( e ) );
    }
  }

  @Override
  protected void doCancel() {
    // nop
  }

  @Override
  public IList<IList<ITemporal<?>>> result() {
    return result;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Вывод в строку текстового представления значений
   *
   * @param aSequences {@link IList}&lt;S&gt; список последовательностей
   * @return String текстовое представление
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static String sequencesGwidStr( IList<IS5Sequence<?>> aSequences ) {
    TsNullArgumentRtException.checkNull( aSequences );
    StringBuilder sb = new StringBuilder();
    for( int index = 0, n = aSequences.size(); index < n; index++ ) {
      IS5Sequence<?> sequence = aSequences.get( index );
      sb.append( sequence.gwid() );
      if( index + 1 < n ) {
        sb.append( ',' );
      }
    }
    return sb.toString();
  }

  /**
   * Вывод в строку текстового представления значений
   *
   * @param aValues {@link IList}&lt;S&gt; список последовательностей
   * @return String текстовое представление
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static String valuesToString( IList<IList<ITemporal<?>>> aValues ) {
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
