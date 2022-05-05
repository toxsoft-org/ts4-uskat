package org.toxsoft.uskat.core.api.sysdescr.dto;

import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.uskat.core.api.sysdescr.*;

/**
 * Class property information base interface.
 *
 * @author hazard157
 */
public interface IDtoClassPropInfoBase
    extends IStridableParameterized, Cloneable {

  /**
   * Returns the class property kind.
   *
   * @return {@link ESkClassPropKind} - the class property kind
   */
  ESkClassPropKind kind();

  /**
   * Creates the instance of the 'deep copy' of this object.
   *
   * @param <T> - expected type of this object
   * @return &lt;T&gt; - 'deep copy' of this object
   */
  <T extends IDtoClassPropInfoBase> T makeCopy();

}
