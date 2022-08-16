package org.toxsoft.uskat.onews.lib;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.*;

/**
 * OneWS managing service.
 * <p>
 * What is "<b>OneWS</b>" (<b>One</b> <b>W</b>ork<b>s</b>tation)?
 * <p>
 * This consept declares that in USkat based system there is only one set of workstation software including whole
 * functionality. Part of functionality (components, abilities) may be trune on or off depending on user role. As far as
 * anyone must login to the system with the role he/she gots subset of funcionality allowed for the role seloected at
 * login time.
 * <p>
 * <p>
 * Here are the main entities of the OneWS service:
 * <ul>
 * <li>{@link ISkOneWsService} - USkat extension service, the point of entry;</li>
 * <li>{@link IOneWsProfile} - WS profile defines availability of componenta/abilities;</li>
 * <li>{@link IOneWsAbility} - definition of WS aniltiy/component;</li>
 * <li>{@link OneWsRule} - single rule determining component availability.</li>
 * </ul>
 *
 * @author hazard157
 */
public interface ISkOneWsService
    extends ISkService {

  /**
   * Service identifier.
   */
  String SERVICE_ID = ISkHardConstants.SK_SYSEXT_SERVICE_ID_PREFIX + ".OneWs"; //$NON-NLS-1$

  /**
   * Returns all profiles including builting ueditable profiles.
   *
   * @return {@link IStridablesList}&lt;{@link IOneWsProfile}&gt; - list of all WS profiles
   */
  IStridablesList<IOneWsProfile> listProfiles();

  /**
   * Finds profile by profile ID.
   *
   * @param aProfileId String - the profile ID
   * @return {@link IOneWsProfile} - found profile or <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  IOneWsProfile findProfileById( String aProfileId );

  /**
   * Returns profile for the specified role.
   * <p>
   * If there is no profile for the specified role then returns profile for guest role.
   *
   * @param aRoleId String - the role ID
   * @return {@link IOneWsProfile} - profile for this role
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such role
   */
  IOneWsProfile getProfileByRoleId( String aRoleId );

  /**
   * Lists known kinds of abilities.
   * <p>
   * Both builtin and user-defined kinds are returned in a single list.
   * <p>
   * Note: user-defined kinds are not stored between application runs, on every startup user have to define kind by with
   * method {@link #defineAbilityKind(IStridableParameterized)}.
   *
   * @return {@link IStridablesList}&lt;{@link IStridableParameterized}&gt; - list of WS ability (component) kinds
   */
  IStridablesList<IStridableParameterized> listKnownAbilityKinds();

  /**
   * Returns known abilities (components) of OneWS.
   * <p>
   * Both builtin and user-defined kinds are returned in a single list.
   * <p>
   * Note: user-defined abilities are not stored between application runs, on every startup user have to define kind by
   * with method {@link #defineAbility(IOneWsAbility)}.
   *
   * @return {@link IStridablesList}&lt;{@link IOneWsAbility}&gt; - список комонент (возможностей) единого АРМ
   */
  IStridablesList<IOneWsAbility> listKnownAbilities();

  /**
   * Assosiates profile to the role.
   *
   * @param aRoleId String - the role ID
   * @param aProfileId String - the profile ID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such role
   * @throws TsItemNotFoundRtException no such profile
   */
  void setRoleProfile( String aRoleId, String aProfileId );

  /**
   * Creates new or edits an existing profile.
   *
   * @param aProfileId String - the profile ID
   * @param aAttrs {@link IOptionSet} - attributes of profile object
   * @param aRules {@link IList}&lt;{@link OneWsRule}&gt; - rules in the order in which they are applied
   * @return {@link IOneWsProfile} - created or edited profile
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException profile ID is not an IDpath
   * @throws TsValidationFailedRtException list of rules is empty
   * @throws TsValidationFailedRtException attempt to change builtin profile
   */
  IOneWsProfile defineProfile( String aProfileId, IOptionSet aAttrs, IList<OneWsRule> aRules );

  /**
   * Removes profile.
   * <p>
   * If no such profile exists then method does nothing.
   *
   * @param aProfileId String - the profile ID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException attempt to remove builtin profile
   */
  void removeProfile( String aProfileId );

  /**
   * Adds ability kind to the lisе of known ones - registers user-speicifed kind.
   *
   * @param aKind {@link IStridableParameterized} - ablity (component) kind description
   */
  void defineAbilityKind( IStridableParameterized aKind );

  /**
   * Adds ability to the lisе of known ones - registers user-speicifed OneWs component.
   *
   * @param aAbility {@link IOneWsAbility} - ablity (component) description
   */
  void defineAbility( IOneWsAbility aAbility );

}
