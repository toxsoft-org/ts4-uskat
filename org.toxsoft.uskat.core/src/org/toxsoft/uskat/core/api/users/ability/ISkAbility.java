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

  /**
   * Determines if ability is enabled.
   * <p>
   * The ability may be temporarily disabled by setting attribute
   * {@link ISkUserServiceHardConstants#ATRID_ABILITY_IS_ENABLED} attribute to <code>false</code>. Any role, including
   * root one, will return {@link ISkRole#isAbilityAllowed(String)} = <code>false</code>.
   * <p>
   * Disabling ability has sense in few cases. For example when adding new features to the system it may be disabled
   * until fully tested.
   *
   * @return boolean - ability is enabled for allowed roles<br>
   *         <code>true</code> if user is allowed to connect to the server<br>
   *         <code>false</code> if there ability is temporary disabled for all users including root
   */
  boolean isEnabled();

}
