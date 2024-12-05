package org.toxsoft.uskat.core.api.users.ability;

import org.toxsoft.core.tslib.bricks.validator.*;

/**
 * Ability manager changes validator.
 *
 * @author hazard157
 */
public interface ISkAbilityManagerValidator {

  ValidationResult canResetRoleAbilities( String aRoleId, boolean aEnableAll );

  ValidationResult canSetRoleAbility( String aRoleId, String aAbilityId, boolean aEnable );

  ValidationResult canDefineAbility( IDtoSkAbility aDto );

  ValidationResult canRemoveAbility( String aAbilityId );

  ValidationResult canDefineKind( IDtoSkAbilityKind aDto );

  ValidationResult canRemoveKind( String aKindId );

}
