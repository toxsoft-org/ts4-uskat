package org.toxsoft.uskat.core.gui.conn;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * {@link ISkConnectionSupplier} actions validator.
 *
 * @author hazard157
 */
public interface ISkConnectionSupplierValidator {

  /**
   * Check if the connection can be chhosen as the default one.
   *
   * @param aKey {@link IdChain} - the key or {@link IdChain#NULL} to reset
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canSetDefaultConnection( IdChain aKey );

  /**
   * Checks if the connection can be created.
   *
   * @param aKey {@link IdChain} - connection key
   * @param aContext {@link ITsGuiContext} - the context for connection and it's M5-domain
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canCreateConnection( IdChain aKey, ITsGuiContext aContext );

  /**
   * Checks if connection can be removed.
   *
   * @param aKey {@link IdChain} - the key
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canRemoveConnection( IdChain aKey );

}
