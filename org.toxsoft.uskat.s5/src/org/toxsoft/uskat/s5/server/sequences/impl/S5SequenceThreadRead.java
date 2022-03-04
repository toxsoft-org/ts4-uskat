package org.toxsoft.uskat.s5.server.sequences.impl;

import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceSQL.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceUtils.*;

import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ELogSeverity;
import org.toxsoft.uskat.s5.server.sequences.*;
import org.toxsoft.uskat.s5.server.sequences.reader.IS5SequenceReadQuery;
import org.toxsoft.uskat.s5.utils.threads.impl.S5AbstractReadThread;

/**
 * Поток чтения последовательностей данных в указанном интервале
 *
 * @param <S> тип последовательности значений данного
 * @param <V> тип значения последовательности
 * @author mvk
 */
public class S5SequenceThreadRead<S extends IS5Sequence<V>, V extends ITemporal<?>>
    extends S5AbstractReadThread<IMap<Gwid, S>> {

  private final IS5SequenceReadQuery query;
  private final IGwidList            gwids;
  private IMapEdit<Gwid, S>          result = new ElemMap<>();

  /**
   * Конструктор
   *
   * @param aQuery {@link IS5SequenceReadQuery} запрос чтения хранимых данных
   * @param aGwids {@link IGwidList} список идентификаторов читаемых данных
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException пустой список описаний данных
   * @throws TsIllegalArgumentRtException в описании типа представлены данные с разным типом
   * @throws TsIllegalArgumentRtException интервал запроса должен быть подмножеством интервала среза полученных данных
   */
  public S5SequenceThreadRead( IS5SequenceReadQuery aQuery, IGwidList aGwids ) {
    TsNullArgumentRtException.checkNulls( aQuery, aGwids );
    TsIllegalArgumentRtException.checkTrue( aGwids.size() == 0 );
    query = aQuery;
    gwids = aGwids;
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5AbstractServerThread
  //
  @SuppressWarnings( "unchecked" )
  @Override
  protected void doRun() {
    String table = (gwids.size() > 0 ? gwidsToString( gwids, 3 ) : TsLibUtils.EMPTY_STRING);
    String infoStr = String.format( MSG_INFO, table, Integer.valueOf( gwids.size() ) );
    // Интервал запроса
    IQueryInterval interval = query.interval();
    // Фабрика значений
    ISequenceFactory<V> factory = (ISequenceFactory<V>)query.factory();
    long traceTimestamp = System.currentTimeMillis();
    long traceReadTimeout = 0;
    long traceCreateTimeout = 0;
    try {
      // Чтение блоков
      IMap<Gwid, IList<ISequenceBlock<V>>> readBlocks = readBlocks( query, gwids );
      traceReadTimeout = System.currentTimeMillis() - traceTimestamp;
      traceTimestamp = System.currentTimeMillis();
      // Создание последовательностей
      for( int index = 0, n = readBlocks.keys().size(); index < n; index++ ) {
        Gwid gwid = readBlocks.keys().get( index );
        IList<ISequenceBlockEdit<V>> blocks = (IList<ISequenceBlockEdit<V>>)(Object)readBlocks.values().get( index );
        // Фактический интервал значений (может быть больше запрашиваемого так как могут быть значения ДО и ПОСЛЕ)
        long factStartTime = interval.startTime();
        long factEndTime = interval.endTime();
        if( blocks.size() > 0 ) {
          long startTime = blocks.get( 0 ).startTime();
          long endTime = blocks.get( blocks.size() - 1 ).endTime();
          if( startTime < factStartTime ) {
            factStartTime = startTime;
          }
          if( factEndTime < endTime ) {
            factEndTime = endTime;
          }
        }
        IQueryInterval factInterval = new QueryInterval( EQueryIntervalType.CSCE, factStartTime, factEndTime );
        IS5SequenceEdit<V> sequence = factory.createSequence( gwid, factInterval, blocks );
        // 2020-12-07 mvk ???
        // if( interval.equals( factInterval ) == false ) {
        // sequence.setInterval( interval );
        // }
        result.put( gwid, (S)sequence );
      }
      traceCreateTimeout = System.currentTimeMillis() - traceTimestamp;
      // Журнал
      if( logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
        Integer td = Integer.valueOf( threadIndex() );
        Long ta = Long.valueOf( System.currentTimeMillis() - traceTimestamp );
        Long tr = Long.valueOf( traceReadTimeout );
        Long tc = Long.valueOf( traceCreateTimeout );
        String s = sequencesToString( result.values() );
        logger().debug( MSG_READ_THREAD_FINISH, td, infoStr, interval, ta, tr, tc, s );
      }
    }
    catch( RuntimeException e ) {
      throw new TsInternalErrorRtException( e, ERR_SEQUENCE_READ_UNEXPECTED, infoStr, interval, cause( e ) );
    }
  }

  @Override
  protected void doCancel() {
    // nop
  }

  @Override
  public IMap<Gwid, S> result() {
    return result;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
}
