package org.toxsoft.uskat.core.api.sysdescr.dto;

import org.toxsoft.core.tslib.av.metainfo.*;

/**
 * Information about attribute property of class.
 *
 * @author hazard157
 */
public interface IDtoAttrInfo
    extends IDtoClassPropInfoBase {

  /**
   * Returns the data type of the attribute.
   *
   * @return {@link IDataType} - the data type
   */
  IDataType dataType();

}
