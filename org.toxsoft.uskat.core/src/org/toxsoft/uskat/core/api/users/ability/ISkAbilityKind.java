package org.toxsoft.uskat.core.api.users.ability;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.uskat.core.api.objserv.*;

/**
 * The ability kind is intended only for visual grouping of abilities.
 *
 * @author hazard157
 */
public interface ISkAbilityKind
    extends ISkObject {

  /**
   * Returns all defined abilities of the specified kind.
   *
   * @return {@link IStridablesList}&lt;{@link ISkAbility}&gt; - abilities of this kind, may be an empty list
   */
  IStridablesList<ISkAbility> listAbilities();

}
