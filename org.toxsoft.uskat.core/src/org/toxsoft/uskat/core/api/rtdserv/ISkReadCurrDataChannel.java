package org.toxsoft.uskat.core.api.rtdserv;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Channel to read current value of the single RTdata.
 * <p>
 * Read channels are shared resources. Each request of read channel increases internal usage counter. Eah
 * {@link #close()} descreases counter. Channel will be closed when counter reaches 0. Call {@link #close()} on closed
 * channel is harmlessly ignored.
 *
 * @author hazard157
 */
public interface ISkReadCurrDataChannel
    extends ISkRtdataChannel {

  /**
   * Returns the current value of the RTdata.
   *
   * @return {@link IAtomicValue} - current value never is <code>null</code>
   * @throws TsIllegalStateRtException channel is in non-operational state
   */
  IAtomicValue getValue();

}
