package org.toxsoft.uskat.core.backend;

import org.toxsoft.tslib.bricks.strid.IStridableParameterized;
import org.toxsoft.tslib.gw.skid.Skid;

/**
 * Information about backend.
 *
 * @author hazard157
 */
public interface ISkBackendInfo
    extends IStridableParameterized {

  /**
   * Returns the backend start time.
   *
   * @return long - the backend start time (millisecons after epoch)
   */
  long startTime();

  /**
   * Returns the session ID.
   *
   * @return {@link Skid} - session ID
   */
  Skid sessionId();

}
