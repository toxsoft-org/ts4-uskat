package org.toxsoft.uskat.core.api.sysdescr.dto;

import org.toxsoft.tslib.bricks.strid.IStridableParameterized;
import org.toxsoft.uskat.core.api.sysdescr.ESkClassPropKind;

/**
 * Class property information base interface.
 *
 * @author hazard157
 */
public interface IDtoClassPropInfoBase
    extends IStridableParameterized {

  /**
   * Returns the class property kind.
   *
   * @return {@link ESkClassPropKind} - the class property kind
   */
  ESkClassPropKind kind();

}
