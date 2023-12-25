package org.toxsoft.uskat.core.api.sysdescr.dto;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * Information about RTdata property of class.
 * <p>
 * Note: option values from {@link #params()} override values from {@link #dataType()} parameters.For convenience method
 * {@link SkHelperUtils#getConstraint(IDtoRtdataInfo, String)} may be used.
 *
 * @author hazard157
 */
public interface IDtoRtdataInfo
    extends IDtoClassPropInfoBase {

  /**
   * Returns the data type of the RTData.
   * <p>
   * Note: setting option value to {@link IDtoRtdataInfo#params()} will override same option value of returned data
   * type. Such approach allows to use same data type for many attributes and RTdata still having ability to specify
   * different values for options like {@link IAvMetaConstants#TSID_DEFAULT_VALUE} or
   * {@link IAvMetaConstants#TSID_FORMAT_STRING}.
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
