package org.toxsoft.uskat.core.api.rtdserv;

import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.*;

/**
 * Base interface for single RTdata I/O.
 * <p>
 * Channels are closeable entities and shall be closed after use. However some channels may be shared resources and the
 * resources will be released when last user closes shared channel.
 *
 * @author hazard157
 */
public interface ISkRtdataChannel
    extends ICloseable {

  /**
   * Returns RTdata concrete GWID of kind {@link EGwidKind#GW_RTDATA}.
   *
   * @return {@link Gwid} - RTdata cncrete GWID
   */
  Gwid gwid();

  /**
   * Determines if channel is in working state.
   * <p>
   * Non-operational channel may start to work. For example, a newly created channel usually waits for some time for
   * data to arrive from the server and is not operational. Or channel may not work forever, for example, when the
   * object was deleted.
   * <p>
   * Access to the data of the non-operational channels may throw an exception.
   *
   * @return boolean - <code>true</code> when channel is in operational state
   */
  boolean isOk();

}
