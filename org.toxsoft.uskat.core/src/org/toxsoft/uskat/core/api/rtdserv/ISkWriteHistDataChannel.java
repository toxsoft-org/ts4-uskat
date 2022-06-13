package org.toxsoft.uskat.core.api.rtdserv;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.errors.*;
import org.toxsoft.core.tslib.av.temporal.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * Channel to write historic RTdata values to the permamnent storage.
 *
 * @author hazard157
 */
public interface ISkWriteHistDataChannel
    extends ISkRtdataChannel {

  /**
   * Writes all values for the specified time of interval.
   * <p>
   * Note: the time interval argument is interpreted as period for which <b>all values</b> are specified.
   * <p>
   * For example, all absent slots of syncroneous RTdata (with {@link IDtoRtdataInfo#isSync()}=<code>true</code>) will
   * be considered as unknown values {@link IAtomicValue#NULL}. However next writes may add data to the "holes".
   *
   * @param aInterval {@link ITimeInterval} - time interval covered by values
   * @param aValues {@link Gwid},{@link ITimedList}&lt;{@link ITemporalAtomicValue}&gt; - the values
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalStateRtException channel is not working, {@link #isOk()} = <code>false</code>
   * @throws TsIllegalArgumentRtException any value is out of specified time interval
   * @throws AvTypeCastRtException incomatible atimoc type of value(s)
   */
  void writeValues( ITimeInterval aInterval, ITimedList<ITemporalAtomicValue> aValues );

}
