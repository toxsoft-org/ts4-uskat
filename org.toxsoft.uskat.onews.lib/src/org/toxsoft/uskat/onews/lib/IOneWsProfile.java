package org.toxsoft.uskat.onews.lib;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;

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
    extends ISkObject {

  /**
   * Determines of specified ability is allowed to be used in this profile.
   *
   * @param aAbility {@link IOneWsAbility} - the ability (component) to be checked
   * @return boolean - <code>true</code> if aility is allowed to be used
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  boolean isAllowed( IOneWsAbility aAbility );

  /**
   * Returns the rules making this profile.
   *
   * @return {@link IListEdit}&lt;{@link OneWsRule}&gt; - rules in the order in which they are applied
   */
  IList<OneWsRule> rules();

}
