package org.toxsoft.uskat.onews.lib;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Validates changes to {@link ISkOneWsService}.
 *
 * @author hazard157
 */
public interface ISkOneWsServiceValidator {

  /**
   * Checks if the specified profile can be associated with the specified role.
   * <p>
   * Check for:
   * <ul>
   * <li>error - profile with specified ID does not exists;</li>
   * <li>error - role with specified ID does not exists.</li>
   * </ul>
   *
   * @param aRoleId String - the role ID
   * @param aProfileId String - the profile ID
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such role
   * @throws TsItemNotFoundRtException no such profile
   */
  ValidationResult canSetRoleProfile( String aRoleId, String aProfileId );

  /**
   * Checks if new profile can be creatred.
   * <p>
   * Check for:
   * <ul>
   * <li>error - profile ID is not an IDpath;</li>
   * <li>error - profile with the specified ID already exists;</li>
   * <li>error - list of rules is empty.</li>
   * </ul>
   *
   * @param aProfileId String - the profile ID
   * @param aAttrs {@link IOptionSet} - attributes of profile object
   * @param aRules {@link IList}&lt;{@link OneWsRule}&gt; - rules in the order in which they are applied
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canCreateProfile( String aProfileId, IOptionSet aAttrs, IList<OneWsRule> aRules );

  /**
   * Checks if existing profile can be edited.
   * <p>
   * Check for:
   * <ul>
   * <li>error - profile with the specified ID does not exists;</li>
   * <li>error - list of rules is empty;</li>
   * <li>error - attempt to change builtin profile.</li>
   * </ul>
   *
   * @param aProfileId String - the profile ID
   * @param aAttrs {@link IOptionSet} - attributes of profile object
   * @param aRules {@link IList}&lt;{@link OneWsRule}&gt; - rules in the order in which they are applied
   * @param aExistingProfile {@link IOneWsProfile} - an existing profile
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canEditProfile( String aProfileId, IOptionSet aAttrs, IList<OneWsRule> aRules,
      IOneWsProfile aExistingProfile );

  /**
   * Removes profile.
   * <p>
   * Check for:
   * <ul>
   * <li>error - attempt to remove builtin profile;</li>
   * <li>warning - profile with specified ID does not exists.</li>
   * </ul>
   *
   * @param aProfileId String - the profile ID
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException attempt to remove builtin profile
   */
  ValidationResult canRemoveProfile( String aProfileId );

  /**
   * Adds ability kind to the lisе of known ones - registers user-speicifed kind.
   * <p>
   * Check for:
   * <ul>
   * <li>error - attempt to redefine builtin ability kind;</li>
   * <li>warning - ability kind with specified ID already exists.</li>
   * </ul>
   *
   * @param aKind {@link IStridableParameterized} - ablity (component) kind description
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canDefineAbilityKind( IStridableParameterized aKind );

  /**
   * Adds ability to the list of known ones - registers user-speicifed OneWs component.
   * <p>
   * Check for:
   * <ul>
   * <li>warning - ability with specified ID already exists.</li>
   * </ul>
   *
   * @param aAbility {@link IOneWsAbility} - ability (component) description
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canDefineAbility( IOneWsAbility aAbility );

}
