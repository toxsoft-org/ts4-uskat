package org.toxsoft.uskat.onews.lib;

import org.toxsoft.core.tslib.bricks.strid.*;

/**
 * Functional ability (software component) of worsktation as a unit that may be turned on/off for roles.
 *
 * @author hazard157
 */
public interface IOneWsAbility
    extends IStridableParameterized {

  /**
   * Returns ability (component) identifier.
   * <p>
   * The identifier must be unique within the entire application.
   *
   * @return String - ability ID (an IDpath)
   */
  @Override
  String id();

  /**
   * Returns ability kind ID.
   *
   * @return String - the ability kind ID (an IDpath)
   */
  String kindId();

}
