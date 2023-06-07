package org.toxsoft.uskat.core.gui.conn.cfg;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.backend.metainf.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * The backend-specific connection configurations provider.
 * <p>
 * Provider must be registered by {@link IConnectionConfigService#registerPovider(IConnectionConfigProvider)}.
 * <p>
 * To open Sk-connection there are two set of arguments:
 * <ul>
 * <li>backend specific arguments - are most important part of the arguments;</li>
 * <li>common arguments - are the same for all backends.</li>
 * </ul>
 * This implementation handles common arguments by itself and uses {@link ISkBackendMetaInfo} to handle backend-specific
 * arguments.
 * <p>
 * Provider identification as {@link IStridable} is the same as {@link ISkBackendMetaInfo},
 *
 * @author hazard157
 */
public sealed interface IConnectionConfigProvider
    extends IStridableParameterized permits ConnectionConfigProvider {

  /**
   * Returns the underlying backend meta-information.
   *
   * @return {@link ISkBackendMetaInfo} - information about backend
   */
  ISkBackendMetaInfo backendMetaInfo();

  /**
   * Returns all options definition with backend options included.
   *
   * @return IStridablesList:lt;{@link IDataDef}&gt; - the list of option definitions
   */
  IStridablesList<IDataDef> opDefs();

  /**
   * Check option values against definitions {@link #opDefs()}.
   * <p>
   * The options not listed in {@link #opDefs()} are ignored
   *
   * @param aOpValues {@link IOptionSet} - the connection argument option values to validate
   * @return {@link ValidationResult} - the validation result
   */
  ValidationResult validateOpValues( IOptionSet aOpValues );

  /**
   * Prepares an argument of the method {@link ISkConnection#open(ITsContextRo)}.
   *
   * @param aSkConnArgs {@link ITsContext} - editable arguments
   * @param aOptions {@link IOptionSet} - the options values
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException validation failed
   */
  void fillArgs( ITsContext aSkConnArgs, IOptionSet aOptions );

}
