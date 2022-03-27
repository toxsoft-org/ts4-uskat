package org.toxsoft.uskat.core.api.sysdescr.dto;

import org.toxsoft.core.tslib.av.metainfo.*;

/**
 * Information about RTdata property of class.
 *
 * @author hazard157
 */
public interface IDtoRtdataInfo
    extends IDtoClassPropInfoBase {

  /**
   * Returns the data type of the RTData.
   *
   * @return {@link IDataType} - the data type
   */
  IDataType dataType();

  /**
   * Determines if the current value of this data will be tracked in real-time.
   *
   * @return boolean - the current data flag
   */
  boolean isCurr();

  /**
   * Determines if the history of this data will be stored.
   *
   * @return boolean - historical data flag
   */
  boolean isHist();

  /**
   * Determines if this is synchronous data.
   *
   * @return boolean - the synchronous data flag
   */
  boolean isSync();

  /**
   * Returns the time interval (slot width) for synchronous data.
   *
   * @return long - the time interval in milliseconds
   */
  long syncDataDeltaT();

}
