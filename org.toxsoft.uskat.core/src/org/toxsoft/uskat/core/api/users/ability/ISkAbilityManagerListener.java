package org.toxsoft.uskat.core.api.users.ability;

import org.toxsoft.core.tslib.coll.primtypes.*;
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
   * <p>
   * Note: when this method is called it is possible that some roles from the list does not exist already.
   *
   * @param aSource {@link ISkUserService} - the event source
   * @param aRoleIds {@link IStringList} - the IDs of the changed roles
   */
  void onRoleAbilitiesChanged( ISkUserService aSource, IStringList aRoleIds );

  /**
   * Called when any ability or kind was defined or removed.
   *
   * @param aSource {@link ISkUserService} - the event source
   */
  void onAbilitiesListChanged( ISkUserService aSource );

}
