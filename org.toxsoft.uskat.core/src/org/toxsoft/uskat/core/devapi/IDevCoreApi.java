package org.toxsoft.uskat.core.devapi;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.txtmatch.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Core API extensions for uskat service developers.
 *
 * @author hazard157
 */
public interface IDevCoreApi
    extends ISkCoreApi {

  /**
   * Finds backend addon.
   *
   * @param <T> - expected interface/class of the addon
   * @param aAddonId String - the ID of the addon
   * @param aExpectedType {@link Class}&lt;T&gt; - expected interface of the addons
   * @return &lt;T&gt; - found addon or <code>null</code> if no such optional addon exists
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws ClassCastException addon was found but not of excpected type
   */
  <T> T findBackendAddon( String aAddonId, Class<T> aExpectedType );

  /**
   * Returns the core entities localization support.
   *
   * @return {@link ICoreL10n} - the core localizer
   */
  ICoreL10n l10n();

  /**
   * Returns arguemjnts used in {@link ISkConnection#open(ITsContextRo)}.
   *
   * @return {@link ITsContextRo} - connection opening arguments
   */
  ITsContextRo openArgs();

  /**
   * Returns rules of classes claiming by the services.
   * <p>
   * Note: even after connection is open the map is updated every time when new service is registered with method
   * {@link ISkCoreApi#addService(ISkServiceCreator)}.
   *
   * @return {@link IStringMap}&lt;{@link IList}&lt;{@link TextMatcher}&gt;&gt; - map "service ID" - "rules list"
   */
  IStringMap<IList<TextMatcher>> mapClaimedClassRules();

  // FIXME comment!
  void doJobInCoreMainThread();

}
