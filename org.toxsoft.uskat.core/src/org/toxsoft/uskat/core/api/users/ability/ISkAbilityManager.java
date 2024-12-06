package org.toxsoft.uskat.core.api.users.ability;

import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Manages abilities - rights of the role to access various functions of the system.
 *
 * @author hazard157
 */
public interface ISkAbilityManager {

  // ------------------------------------------------------------------------------------
  // Role rights management

  /**
   * Returns abilities allowed for the specified role.
   *
   * @param aRoleId String - the role ID
   * @return {@link IStridablesList}&lt;{@link ISkAbility}&gt; - abilities allowed for the role
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException specified role does not exists
   */
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

  /**
   * Enable/disables access to all abilities of the role at once.
   *
   * @param aRoleId String - the role ID
   * @param aEnableAll boolean - <code>true</code> allow all, <code>false</code> - disallow all abilities
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed validation {@link ISkAbilityManagerValidator}
   */
  void resetRoleAbilities( String aRoleId, boolean aEnableAll );

  /**
   * Enable/disables access to the specified ability of the role.
   *
   * @param aRoleId String - the role ID
   * @param aAbilityId String - the ability ID
   * @param aEnable boolean - <code>true</code> allow , <code>false</code> - disallow the ability for role
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed validation {@link ISkAbilityManagerValidator}
   */
  void setRoleAbility( String aRoleId, String aAbilityId, boolean aEnable );

  // ------------------------------------------------------------------------------------
  // Abilities and kinds management

  /**
   * Returns all defined abilities.
   *
   * @return {@link IStridablesList}&lt;{@link ISkAbility}&gt; - list of all abilities
   */
  IStridablesList<ISkAbility> listAbilities();

  /**
   * Returns all defined kinds.
   *
   * @return {@link IStridablesList}&lt;{@link ISkAbilityKind}&gt; - list of all kinds
   */
  IStridablesList<ISkAbilityKind> listKinds();

  /**
   * Creates new or updates existing ability.
   *
   * @param aDto {@link IDtoSkAbility} - information about the ability
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed validation {@link ISkAbilityManagerValidator}
   */
  void defineAbility( IDtoSkAbility aDto );

  /**
   * Removes the specified ability.
   *
   * @param aAbilityId String - ability ID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed validation {@link ISkAbilityManagerValidator}
   */
  void removeAbility( String aAbilityId );

  /**
   * Creates new or updates existing kind.
   *
   * @param aDto {@link IDtoSkAbilityKind} - information about the kind
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed validation {@link ISkAbilityManagerValidator}
   */
  void defineKind( IDtoSkAbilityKind aDto );

  /**
   * Removes the ability kind.
   * <p>
   * Note: removing kind does <b>not</b> removes any ability. Abilities of the removed kind will be listed as of kind
   * {@link ISkAbilityHardConstants#ABILITY_KIND_ID_UNDEFINED}.
   * <p>
   * If the specified kind does not exists then
   *
   * @param aKindId String - the ID of kind to remove
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed validation {@link ISkAbilityManagerValidator}
   */
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

  // ------------------------------------------------------------------------------------
  // Inline methods for convenience

  @SuppressWarnings( "javadoc" )
  default void enableRoleAbility( String aRoleId, String aAbilityId ) {
    setRoleAbility( aRoleId, aAbilityId, true );
  }

  @SuppressWarnings( "javadoc" )
  default void disableRoleAbility( String aRoleId, String aAbilityId ) {
    setRoleAbility( aRoleId, aAbilityId, false );
  }

}
