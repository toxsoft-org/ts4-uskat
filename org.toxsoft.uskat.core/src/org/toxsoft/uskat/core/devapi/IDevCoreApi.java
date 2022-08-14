package org.toxsoft.uskat.core.devapi;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Core API extensions for uskat service developers.
 *
 * @author hazard157
 */
public interface IDevCoreApi
    extends ISkCoreApi {

  // TODO objects locking ???

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
   * Determines ID of service claiming ownership of entities of the specified class.
   * <p>
   * All classes not explicitly claimed by any service is considered to be"oned" by {@link ISkSysdescr#SERVICE_ID}.
   *
   * @param aClassId String - ID of class to be checked
   * @return String - ID of claiming service ID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  String determineClassClaimingServiceId( String aClassId );

  // FIXME comment!
  void doJobInCoreMainThread();

}
