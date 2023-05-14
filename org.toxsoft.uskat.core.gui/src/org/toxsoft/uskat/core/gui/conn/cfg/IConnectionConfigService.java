package org.toxsoft.uskat.core.gui.conn.cfg;

import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * {@link IConnectionConfig} management service.
 * <p>
 * Reference to this service must be placed in application context.
 *
 * @author hazard157
 */
public sealed interface IConnectionConfigService permits ConnectionConfigService {

  /**
   * Returns all defined connection configuration.
   *
   * @return IStridablesList:lt;{@link IConnectionConfig}&gt; - the list of defined connection configuration
   */
  IStridablesList<IConnectionConfig> listConfigs();

  /**
   * Defines (either adds new or replaces existing) configuration.
   *
   * @param aCfg {@link IConnectionConfig} - the configuration
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException validation by {@link IConnectionConfigServiceValidator} failed
   */
  void defineConfig( IConnectionConfig aCfg );

  /**
   * Removes the configuration.
   *
   * @param aCfgId String - configuration ID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException validation by {@link IConnectionConfigServiceValidator} failed
   */
  void removeConfig( String aCfgId );

  // ------------------------------------------------------------------------------------
  // Providers

  /**
   * Returns all registered connection configuration providers.
   *
   * @return IStridablesList:lt;{@link IConnectionConfigProvider}&gt; - the list of registered providers
   */
  IStridablesList<IConnectionConfigProvider> listProviders();

  /**
   * Registers the provider.
   *
   * @param aProvider {@link IConnectionConfigProvider} - the provider
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemAlreadyExistsRtException provider with the same ID is already registered
   */
  void registerPovider( IConnectionConfigProvider aProvider );

  // ------------------------------------------------------------------------------------
  // Service support

  /**
   * Returns the service validator.
   *
   * @return {@link ITsValidationSupport}&lt;{@link IConnectionConfigServiceValidator}&gt; - the service validator
   */
  ITsValidationSupport<IConnectionConfigServiceValidator> svs();

  /**
   * Returns the service eventer.
   *
   * @return {@link ITsEventer}&lt;{@link IConnectionConfigServiceListener}&gt; - the service eventer
   */
  ITsEventer<IConnectionConfigServiceListener> eventer();

}
