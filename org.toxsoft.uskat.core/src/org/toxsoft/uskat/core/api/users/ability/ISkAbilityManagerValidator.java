package org.toxsoft.uskat.core.api.users.ability;

import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Ability manager changes validator.
 *
 * @author hazard157
 */
public interface ISkAbilityManagerValidator {

  /**
   * Checks if access rights to the abilities can be changed for the role.
   * <p>
   * Note: method {@link ISkAbilityManager#changeRoleAbilities(String, IStringList, boolean)} assumes that other
   * abilities, not listed in <code>aAbilityIds</code> does not change access state.
   *
   * @param aRoleId String - the role ID
   * @param aAbilityIds {@link IStringList} - list of the ability IDs to be changed
   * @param aEnable boolean - <code>true</code> allow, <code>false</code> - disallow specified abilities for role
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canChangeRoleAbilities( String aRoleId, IStringList aAbilityIds, boolean aEnable );

  /**
   * Checks if access rights of the role can be changed to the new set of allowed abilities.
   *
   * @param aRoleId String - the role ID
   * @param aAbilityIds {@link IStringList} - list of new set of enabled ability IDs
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canSetRoleAbilities( String aRoleId, IStringList aAbilityIds );

  /**
   * Checks if new ability can be created or existing updated.
   * <p>
   * When creating new ability argument <code>aExistingAbility</code> = <code>null</code>.
   *
   * @param aDto {@link IDtoSkAbility} - information about ability
   * @param aExistingAbility {@link ISkAbility} - the existing ability or <code>null</code>
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canDefineAbility( IDtoSkAbility aDto, ISkAbility aExistingAbility );

  /**
   * Checks if ability can be removed.
   *
   * @param aAbilityId String - the ID of ability to remove
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canRemoveAbility( String aAbilityId );

  /**
   * Checks if new kind can be created or existing updated.
   * <p>
   * When creating new kind argument <code>aExistingKind</code> = <code>null</code>.
   *
   * @param aDto {@link IDtoSkAbilityKind} - information about the kind
   * @param aExistingKind {@link ISkAbilityKind} - the existing kind or <code>null</code>
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canDefineKind( IDtoSkAbilityKind aDto, ISkAbilityKind aExistingKind );

  /**
   * Checks if kind can be removed.
   *
   * @param aKindId String - the ID of kind to remove
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canRemoveKind( String aKindId );

}
