package org.toxsoft.uskat.core.api.users.ability;

import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.users.*;

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
   * Returns abilities allowed for the currently logged user role.
   *
   * @return {@link IStridablesList}&lt;{@link ISkAbility}&gt; - abilities allowed for the role
   */
  IStridablesList<ISkAbility> listCurrentRoleAbilities();

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
   * Determines if role of the currently logged user has access to the specified ability.
   * <p>
   * For unknown abilities method returns <code>false</code>.
   *
   * @param aAbilityId String - the ability ID
   * @return boolean - <code>true</code> if role is allowed to use the specified ability
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  boolean isAbilityAllowed( String aAbilityId );

  /**
   * Enable/disables access to the specified abilities of the role.
   * <p>
   * The access the the abilities not listed in <code>aAbilityIds</code> remains unchanged.
   * <p>
   * Unknown or duplicate ability IDs are ignored.
   *
   * @param aRoleId String - the role ID
   * @param aAbilityIds {@link IStringList} - list of the the ability IDs
   * @param aEnable boolean - <code>true</code> allow, <code>false</code> - disallow specified abilities for role
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed validation {@link ISkAbilityManagerValidator}
   */
  void changeRoleAbilities( String aRoleId, IStringList aAbilityIds, boolean aEnable );

  /**
   * Replaces existing abilities of the role with new set of the enabled abilities.
   * <p>
   * After call of this method all abilities from the <code>aEnabledAbilityIds</code> become enabled while all others
   * became disabled.
   * <p>
   * Unknown or duplicate ability IDs are ignored.
   *
   * @param aRoleId String - the role ID
   * @param aEnabledAbilityIds {@link IStringList} - list of the the ability IDs
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed validation {@link ISkAbilityManagerValidator}
   */
  void setRoleAbilities( String aRoleId, IStringList aEnabledAbilityIds );

  // ------------------------------------------------------------------------------------
  // Abilities and kinds management

  /**
   * Finds the ability.
   *
   * @param aAbilityId String - ability ID
   * @return {@link ISkAbilityKind} - found ability or <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ISkAbility findAbility( String aAbilityId );

  /**
   * Finds the ability kind.
   *
   * @param aKindId String - ability kind ID
   * @return {@link ISkAbilityKind} - found kind or <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ISkAbilityKind findKind( String aKindId );

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
   * {@link ISkUserServiceHardConstants#ABILITY_KIND_ID_UNDEFINED}.
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
  default void setRoleAbility( String aRoleId, String aAbilityId, boolean aEnable ) {
    changeRoleAbilities( aRoleId, new SingleStringList( aAbilityId ), aEnable );
  }

  @SuppressWarnings( "javadoc" )
  default void enableRoleAbility( String aRoleId, String aAbilityId ) {
    changeRoleAbilities( aRoleId, new SingleStringList( aAbilityId ), true );
  }

  @SuppressWarnings( "javadoc" )
  default void disableRoleAbility( String aRoleId, String aAbilityId ) {
    changeRoleAbilities( aRoleId, new SingleStringList( aAbilityId ), false );
  }

}
