package org.toxsoft.uskat.s5.server.backend.supports.queries.impl;

import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.queries.impl.IS5Resources.*;

import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.bricks.time.ITemporal;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ELogSeverity;
import org.toxsoft.uskat.s5.server.backend.addons.queries.ES5QueriesConvoyState;
import org.toxsoft.uskat.s5.server.backend.addons.queries.S5BaQueriesConvoy;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.IS5HistDataSequence;
import org.toxsoft.uskat.s5.server.backend.supports.queries.IS5BackendQueriesFunction;
import org.toxsoft.uskat.s5.server.sequences.*;
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
  private final IS5Sequence<?>                   sequence;
  private final IList<IS5BackendQueriesFunction> functions;
  private final IListEdit<IList<ITemporal<?>>>   result;

  /**
   * Создание асинхронной задачи выполнения функций обработки значений данного
   *
   * @param aQuery {@link S5BaQueriesConvoy} конвой-объекта запроса
   * @param aSequence {@link IS5HistDataSequence} исходная последовательность данных
   * @param aFunctions {@link IList }&lt;{@link IS5BackendQueriesFunction}&gt; список функций обработки значений
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException недопустимый интервал времени
   */
  public S5BackendQueriesFunctionThread( S5BaQueriesConvoy aQuery, IS5Sequence<?> aSequence,
      IList<IS5BackendQueriesFunction> aFunctions ) {
    TsNullArgumentRtException.checkNulls( aQuery, aSequence, aFunctions );
    query = aQuery;
    sequence = aSequence;
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
    IQueryInterval interval = sequence.interval();
    try {
      int count = functions.size();
      // Идентификатор данного
      Gwid gwid = sequence.gwid();
      long traceStartTime = System.currentTimeMillis();
      long traceCreateStartTime = 0, traceCreateTimeout = 0;
      // Блоки последовательности
      IList<IS5SequenceBlock<?>> blocks = (IList<IS5SequenceBlock<?>>)(Object)sequence.blocks();
      // Количество прочитанных значений
      int valueCount = 0;
      // Количество считанных блоков
      for( int blockIndex = 0, n = blocks.size(); blockIndex < n; blockIndex++ ) {
        // Подсчет считанных значений
        valueCount += blocks.get( blockIndex ).size();
      }
      // Создание последовательности и размещение в результате
      traceCreateStartTime = System.currentTimeMillis();
      // Курсор значений
      IS5SequenceCursor<?> cursor = sequence.createCursor();
      // Общее количество обработанных значений
      int resultValueCount = 0;
      // Проход по всем функциям поставляя значения блока для обработки
      for( int index = 0; index < count; index++ ) {
        if( query.state() != ES5QueriesConvoyState.EXECUTING ) {
          // Запрос был отменен
          break;
        }
        // Функция обработки
        IS5BackendQueriesFunction function = functions.get( index );
        // Значения-результат
        IList<ITemporal<?>> values = function.evaluate( cursor );
        // Обработка значений последовательности
        result.addAll( values );
        // Количество обработанных значений
        resultValueCount += values.size();
      }
      traceCreateTimeout = System.currentTimeMillis() - traceCreateStartTime;

      if( logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
        Integer bc = Integer.valueOf( blocks.size() );
        Integer vc = Integer.valueOf( valueCount );
        Integer ac = Integer.valueOf( functions.size() );
        Integer avc = Integer.valueOf( resultValueCount );
        Integer index = Integer.valueOf( threadIndex() );
        Long ta = Long.valueOf( System.currentTimeMillis() - traceStartTime );
        Long tc = Long.valueOf( traceCreateTimeout );
        String s = valuesToString( result );
        logger().debug( MSG_FUNC_THREAD_FINISH, index, gwid, interval, bc, vc, ac, avc, ta, tc, s );
      }
    }
    catch( Throwable e ) {
      throw new TsInternalErrorRtException( e, ERR_FUNC_UNEXPECTED, sequence.gwid(), interval, cause( e ) );
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
