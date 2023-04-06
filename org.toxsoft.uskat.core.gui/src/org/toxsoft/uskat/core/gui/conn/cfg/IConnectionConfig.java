package org.toxsoft.uskat.core.gui.conn.cfg;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strid.*;

/**
 * Sk-connection configuration data.
 *
 * @author hazard157
 */
public sealed interface IConnectionConfig
    extends IStridableParameterized permits ConnectionConfig {

  /**
   * Returns the ID of the connection provider from the list {@link IConnectionConfigService#listProviders()}.
   *
   * @return String - the connection provider ID
   */
  String providerId();

  /**
   * Returns the option values of the connection arguments.
   *
   * @return {@link IOptionSet} - connection option values
   */
  IOptionSet opValues();

}
