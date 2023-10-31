package org.toxsoft.uskat.core.api.sysdescr.dto;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * Information about attribute property of class.
 * <p>
 * Note: option values from {@link #params()} override values from {@link #dataType()} parameters. For convenience
 * method {@link SkHelperUtils#getConstraint(IDtoAttrInfo, String)} may be used.
 *
 * @author hazard157
 */
public interface IDtoAttrInfo
    extends IDtoClassPropInfoBase {

  /**
   * Returns the data type of the attribute.
   * <p>
   * Note: setting option value to {@link IDtoRtdataInfo#params()} will override same option value of returned data
   * type. Such approach allows to use same data type for many attributes and RTdata still having ability to specify
   * different values for options like {@link IAvMetaConstants#TSID_DEFAULT_VALUE} or
   * {@link IAvMetaConstants#TSID_FORMAT_STRING}.
   *
   * @return {@link IDataType} - the data type
   */
  IDataType dataType();

}
