package org.toxsoft.uskat.core.api.users.ability;

import org.toxsoft.uskat.core.api.ugwis.*;
import org.toxsoft.uskat.core.api.users.*;

/**
 * Listener to the {@link ISkAbilityManager} events.
 * <p>
 * Note: the user service is specified as an event source, not an ability manager.
 *
 * @author hazard157
 */
public interface ISkAbilityManagerListener {

  /**
   * Called on any change in role rights to access the abilities.
   *
   * @param aSource {@link ISkUserService} - the event source
   * @param aRoleId String - the ID of the changed role
   */
  void onRoleAbilitiesChanged( ISkUserService aSource, String aRoleId );

  /**
   * Called when any ability or kind was defined or removed.
   *
   * @param aSource {@link ISkUserService} - the event source
   */
  void onAbilitiesListChanged( ISkUgwiService aSource );

}
