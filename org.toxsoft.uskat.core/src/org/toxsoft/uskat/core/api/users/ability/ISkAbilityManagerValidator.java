package org.toxsoft.uskat.core.api.users.ability;

import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Ability manager changes validator.
 *
 * @author hazard157
 */
public interface ISkAbilityManagerValidator {

  /**
   * Checks if access rights to all abilities of the role can be changed at once.
   *
   * @param aRoleId String - the role ID
   * @param aEnableAll boolean - <code>true</code> allow all, <code>false</code> - disallow all abilities
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canResetRoleAbilities( String aRoleId, boolean aEnableAll );

  /**
   * Checks if access right to the ability can be changed for the role.
   * <p>
   * Note: this method is called only if <code>aEnable</code> has different value when the current access right.
   *
   * @param aRoleId String - the role ID
   * @param aAbilityId String - the ability ID
   * @param aEnable boolean - <code>true</code> allow , <code>false</code> - disallow the ability for role
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canSetRoleAbility( String aRoleId, String aAbilityId, boolean aEnable );

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
