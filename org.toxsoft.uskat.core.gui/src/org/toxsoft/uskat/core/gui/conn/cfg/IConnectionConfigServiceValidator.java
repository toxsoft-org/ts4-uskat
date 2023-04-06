package org.toxsoft.uskat.core.gui.conn.cfg;

import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * The service {@link IConnectionConfigService} validator.
 *
 * @author hazard157
 */
public interface IConnectionConfigServiceValidator {

  /**
   * Checks if new configuration may be defined.
   * <p>
   * Used to check call of the method {@link IConnectionConfigService#removeConfig(String)} when configuration with ID
   * {@link IConnectionConfig#id() aCfg.id()} does not exists.
   *
   * @param aSource {@link IConnectionConfigService} - the service
   * @param aCfg {@link IConnectionConfig} - the configuration
   * @return {@link ValidationResult} - the validation result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canAddConfig( IConnectionConfigService aSource, IConnectionConfig aCfg );

  /**
   * Checks if an existing configuration may be replaced.
   * <p>
   * Used to check call of the method {@link IConnectionConfigService#removeConfig(String)} when configuration with ID
   * {@link IConnectionConfig#id() aCfg.id()} already exists.
   *
   * @param aSource {@link IConnectionConfigService} - the service
   * @param aCfg {@link IConnectionConfig} - the configuration
   * @param aOldCfg {@link IConnectionConfig} - the configuration to be replaced
   * @return {@link ValidationResult} - the validation result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canReplaceConfig( IConnectionConfigService aSource, IConnectionConfig aCfg,
      IConnectionConfig aOldCfg );

  /**
   * Checks if configuration can be removed.
   * <p>
   * Used to check call of the method {@link IConnectionConfigService#removeConfig(String)}.
   *
   * @param aSource {@link IConnectionConfigService} - the service
   * @param aCfgId String - configuration ID
   * @return {@link ValidationResult} - the validation result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canRemoveConfig( IConnectionConfigService aSource, String aCfgId );

}
