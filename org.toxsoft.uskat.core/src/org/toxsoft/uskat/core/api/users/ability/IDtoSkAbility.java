package org.toxsoft.uskat.core.api.users.ability;

import org.toxsoft.core.tslib.bricks.strid.*;

/**
 * Information about ability is used for ability creation/editing.
 *
 * @author hazard157
 */
public interface IDtoSkAbility
    extends IStridableParameterized {

  /**
   * Returns the ability kind ID.
   *
   * @return String - the ability kind ID (an IDpath)
   */
  String kindId();

}
