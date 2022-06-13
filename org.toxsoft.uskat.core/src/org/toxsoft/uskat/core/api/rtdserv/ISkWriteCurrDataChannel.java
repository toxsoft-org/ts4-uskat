package org.toxsoft.uskat.core.api.rtdserv;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.errors.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Channel to write current value of the single RTdata.
 * <p>
 * Write channels are not a shared resource. Each request of this channel returns the same instance. Method
 * {@link #close()} flushes and closes channel immediately.
 *
 * @author hazard157
 */
public interface ISkWriteCurrDataChannel
    extends ISkRtdataChannel {

  /**
   * Writes current value as it is known at method call time.
   *
   * @param aValue {@link IAtomicValue} - lastest value of RTData
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws AvTypeCastRtException atomic type of the value does not matches channel atomic type
   */
  void setValue( IAtomicValue aValue );

}
