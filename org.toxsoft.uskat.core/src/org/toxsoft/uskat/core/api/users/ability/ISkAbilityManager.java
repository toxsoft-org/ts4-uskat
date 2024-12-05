package org.toxsoft.uskat.core.api.users.ability;

import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Manages abilities - rights of the role to access various functions of the system.
 *
 * @author hazard157
 */
public interface ISkAbilityManager {

  // ------------------------------------------------------------------------------------
  // Role rights management

  IStridablesList<ISkAbility> listRoleAbilities( String aRoleId );

  /**
   * Determines if role has access to the specified ability.
   * <p>
   * For unknown abilities method returns <code>false</code>.
   *
   * @param aRoleId String - the role ID
   * @param aAbilityId String - the ability ID
   * @return boolean - <code>true</code> if role is allowed to use the specified ability
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException unknown role ID
   */
  boolean isAbilityAllowed( String aRoleId, String aAbilityId );

  void resetRoleAbilities( String aRoleId, boolean aEnableAll );

  void setRoleAbility( String aRoleId, String aAbilityId, boolean aEnable );

  default void enableRoleAbility( String aRoleId, String aAbilityId ) {
    setRoleAbility( aRoleId, aAbilityId, true );
  }

  default void disableRoleAbility( String aRoleId, String aAbilityId ) {
    setRoleAbility( aRoleId, aAbilityId, false );
  }

  // ------------------------------------------------------------------------------------
  // Abilities and kinds management

  IStridablesList<ISkAbility> listAbilities();

  IStridablesList<ISkAbilityKind> listKinds();

  void defineAbility( IDtoSkAbility aDto );

  void removeAbility( String aAbilityId );

  void defineKind( IDtoSkAbilityKind aDto );

  void removeKind( String aKindId );

  // ------------------------------------------------------------------------------------
  // Service support

  /**
   * Returns the service validator.
   *
   * @return {@link ITsValidationSupport}&lt;{@link ISkAbilityManagerValidator}&gt; - the service validator
   */
  ITsValidationSupport<ISkAbilityManagerValidator> svs();

  /**
   * Returns the service eventer.
   *
   * @return {@link ITsEventer}&lt;{@link ISkAbilityManagerListener}&gt; - the service eventer
   */
  ITsEventer<ISkAbilityManagerListener> eventer();

}
