package org.toxsoft.uskat.core.api.users.ability;

import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.users.*;

/**
 * The ability describes a part of the system functions, access to which can be denied for individual roles.
 *
 * @author hazard157
 */
public interface ISkAbility
    extends ISkObject {

  /**
   * The {@link ISkAbility} class identifier.
   */
  String CLASS_ID = ISkUserServiceHardConstants.CLSID_ABILITY;

  /**
   * Returns the kind of ability.
   *
   * @return {@link ISkAbilityKind} - the kind, never is <code>null</code>
   */
  ISkAbilityKind kind();

}
