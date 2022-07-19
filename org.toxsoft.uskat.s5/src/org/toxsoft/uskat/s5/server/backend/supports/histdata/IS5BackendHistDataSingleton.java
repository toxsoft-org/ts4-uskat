package org.toxsoft.uskat.s5.server.backend.supports.histdata;

import javax.ejb.Local;

import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.core.api.rtdserv.ISkWriteHistDataChannel;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoRtdataInfo;
import org.toxsoft.uskat.s5.server.backend.IS5BackendSupportSingleton;

/**
 * Локальный интерфейс синглетона запросов к хранимым данным предоставляемый s5-сервером.
 *
 * @author mvk
 */
@Local
public interface IS5BackendHistDataSingleton
    extends IS5BackendSupportSingleton {

  /**
   * Writes RTdata data history.
   * <p>
   * Arguments are as specified in {@link ISkWriteHistDataChannel#writeValues(ITimeInterval, ITimedList)}.
   *
   * @param aGwid {@link Gwid} - concrete GWID of RTdata with {@link IDtoRtdataInfo#isHist()} = <code>true</code>
   * @param aInterval {@link ITimeInterval} - time interval covered by values
   * @param aValues {@link Gwid},{@link ITimedList}&lt;{@link ITemporalAtomicValue}&gt; - the values
   */
  void writeHistData( Gwid aGwid, ITimeInterval aInterval, ITimedList<ITemporalAtomicValue> aValues );

  /**
   * Returns the single RTdata history for specified time interval.
   *
   * @param aInterval {@link IQueryInterval} - query time interval
   * @param aGwid {@link Gwid} - valid concrete single RTdata GWID of one object
   * @return {@link ITimedList}&lt;{@link SkEvent}&gt; - list of the queried entities
   */
  ITimedList<ITemporalAtomicValue> queryObjRtdata( IQueryInterval aInterval, Gwid aGwid );
}
