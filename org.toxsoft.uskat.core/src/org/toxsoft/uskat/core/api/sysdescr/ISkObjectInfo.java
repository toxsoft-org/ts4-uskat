package org.toxsoft.uskat.core.api.sysdescr;

import org.toxsoft.core.tslib.bricks.strid.*;

/**
 * Information about object.
 *
 * @author dima
 */
public interface ISkObjectInfo
    extends IStridableParameterized {

  /**
   * Returns class information.
   *
   * @return {@link ISkClassInfo} - class info
   */
  ISkClassInfo classInfo();

}
