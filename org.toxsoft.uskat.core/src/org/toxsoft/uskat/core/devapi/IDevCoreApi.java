package org.toxsoft.uskat.core.devapi;

import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;

/**
 * Core API extensions for uskat service developers.
 *
 * @author goga
 */
public interface IDevCoreApi
    extends ISkCoreApi {

  /**
   * Returns backend addon.
   *
   * @param <T> - expected interface of the addons
   * @param aAddonInterface {@link Class}&lt;T&gt; - expected interface of the addons
   * @return &lt;T&gt; - found addon or <code>null</code> if no such addon exists
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  <T> T getBackendAddon( Class<T> aAddonInterface );

  /**
   * Fire core event using eventer {@link ISkCoreApi#eventer()}.
   *
   * @param aSkCoreEvent {@link org.toxsoft.uskat.core.SkCoreEvent} - the event
   */
  void fireCoreEvent( SkCoreEvent aSkCoreEvent );

  /**
   * Returns the core entities localization support.
   *
   * @return {@link ICoreL10n} - the core localizer
   */
  ICoreL10n l10n();

}
