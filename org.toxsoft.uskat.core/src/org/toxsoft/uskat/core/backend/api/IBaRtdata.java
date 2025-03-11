package org.toxsoft.uskat.core.backend.api;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.temporal.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.evserv.*;
import org.toxsoft.uskat.core.api.rtdserv.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.backend.*;

/**
 * Backend addon for current and historic RTdata.
 * <p>
 * This is the mandatory addon.
 *
 * @author hazard157
 */
public interface IBaRtdata
    extends IBackendAddon {

  /**
   * ID of this backend addon.
   */
  String ADDON_ID = ISkBackendHardConstant.BAID_RTDATA;

  // ------------------------------------------------------------------------------------
  // Текущие данные

  /**
   * Prepares backend to supply current RTdata values in real-time.
   *
   * @param aToRemove {@link IGwidList} - list of current RTdata concrete GWIDs removed from read currdata. null -
   *          remove all GWIDs.
   * @param aToAdd {@link IGwidList} - list of current RTdata concrete GWIDs added to read currdata.
   * @return {@link IMap}&lt;{@link Gwid},{@link IAtomicValue}&gt; current RTdata values;<br>
   *         Key: {@link Gwid} concrete GWID;<br>
   *         Value: {@link IAtomicValue} сurrent value.
   * @throws TsNullArgumentRtException argument aToAdd = <code>null</code>
   */
  IMap<Gwid, IAtomicValue> configureCurrDataReader( IGwidList aToRemove, IGwidList aToAdd );

  /**
   * Prepares backend to receive current values for the specified RTdata.
   * <p>
   * Note: for unprepared GWIDs updating curtret valyes by {@link #writeCurrData(Gwid, IAtomicValue)} has no effect.
   *
   * @param aToRemove {@link IGwidList} - list of current RTdata concrete GWIDs removed from write currdata. null -
   *          remove all GWIDs.
   * @param aToAdd {@link IGwidList} - list of current RTdata concrete GWIDs added to write currdata.
   * @throws TsNullArgumentRtException argument aToAdd = <code>null</code>
   */
  void configureCurrDataWriter( IGwidList aToRemove, IGwidList aToAdd );

  /**
   * Updates the actual value of the current data.
   * <p>
   * Note: GWID must be previously configured for writing by {@link #configureCurrDataWriter(IGwidList, IGwidList)}.
   *
   * @param aGwid {@link Gwid} - concrete GWID of RTdata with {@link IDtoRtdataInfo#isCurr()} = <code>true</code>
   * @param aValue {@link IAtomicValue} - current value of RTdata
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  void writeCurrData( Gwid aGwid, IAtomicValue aValue );

  /**
   * Writes RTdata data history.
   * <p>
   * Arguments are as specified in {@link ISkWriteHistDataChannel#writeValues(ITimeInterval, ITimedList)}.
   *
   * @param aGwid {@link Gwid} - concrete GWID of RTdata with {@link IDtoRtdataInfo#isHist()} = <code>true</code>
   * @param aInterval {@link ITimeInterval} - time interval covered by values
   * @param aValues {@link Gwid},{@link ITimedList}&lt;{@link ITemporalAtomicValue}&gt; - the values
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  void writeHistData( Gwid aGwid, ITimeInterval aInterval, ITimedList<ITemporalAtomicValue> aValues );

  /**
   * Returns the single RTdata history for specified time interval.
   *
   * @param aInterval {@link IQueryInterval} - query time interval
   * @param aGwid {@link Gwid} - valid concrete single RTdata GWID of one object
   * @return {@link ITimedList}&lt;{@link SkEvent}&gt; - list of the queried entities
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ITimedList<ITemporalAtomicValue> queryObjRtdata( IQueryInterval aInterval, Gwid aGwid );

}
