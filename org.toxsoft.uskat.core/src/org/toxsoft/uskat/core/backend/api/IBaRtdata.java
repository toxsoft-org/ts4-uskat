package org.toxsoft.uskat.core.backend.api;

import static org.toxsoft.uskat.core.ISkHardConstants.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.temporal.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.uskat.core.api.evserv.*;

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
  String ADDON_ID = SK_ID + "ba.Rtdata"; //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Текущие данные

  void configureCurrDataReader( IList<Gwid> aRtdGwids );

  void configureCurrDataWriter( IList<Gwid> aRtdGwids );

  void writeCurrData( Gwid aGwid, IAtomicValue aValue );

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
