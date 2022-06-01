package org.toxsoft.uskat.core.backend.api;

import org.toxsoft.core.tslib.bricks.strid.*;

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

}
