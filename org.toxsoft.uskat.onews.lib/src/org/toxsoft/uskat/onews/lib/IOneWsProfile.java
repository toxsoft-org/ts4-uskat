package org.toxsoft.uskat.onews.lib;

import org.toxsoft.core.tslib.av.utils.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.users.*;

/**
 * Workstation profile - determines which comonents (abilities) are available to the user.
 * <p>
 * Profile does not lists allowed/disallowed abilities, instead it is set of rules, used to determine if the specified
 * ability is allowed to the user. So it is possible to manage abilities not existing at the time of profile creation.
 * From the other side worstation developrs can deveop abilities and cmponents adopted to the existing rules.
 *
 * @author hazard157
 */
public interface IOneWsProfile
    extends ISkObject, IParameterized {

  /**
   * The {@link IOneWsProfile} class identifier.
   */
  String CLASS_ID = IOneWsConstants.CLSID_OWS_PROFILE;

  /**
   * Determines of specified ability is allowed to be used in this profile.
   *
   * @param aAbility {@link IOneWsAbility} - the ability (component) to be checked
   * @return boolean - <code>true</code> if aility is allowed to be used
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  boolean isAllowed( IOneWsAbility aAbility );

  /**
   * Determines if it is builtin uneditable profile.
   *
   * @return boolean - <code>true</code> this is builtin profile
   */
  boolean isBuiltinProfile();

  /**
   * Returns the rules making this profile.
   *
   * @return {@link IListEdit}&lt;{@link OneWsRule}&gt; - rules in the order in which they are applied
   */
  IList<OneWsRule> rules();

  /**
   * Returns users assiciated with this profile.
   * <p>
   * Note: for guest profile returns only roles explicitly associated with profile.
   *
   * @return {@link IList}&lt;{@link ISkRole}&gt; - roles with this profile
   */
  IList<ISkRole> profileRoles();

}
