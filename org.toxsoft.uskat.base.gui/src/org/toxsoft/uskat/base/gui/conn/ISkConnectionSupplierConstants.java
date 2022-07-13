package org.toxsoft.uskat.base.gui.conn;

import static org.toxsoft.uskat.core.ISkHardConstants.*;

import org.toxsoft.core.tslib.bricks.strid.more.*;

/**
 * Constants used by {@link ISkConnectionSupplier}.
 *
 * @author hazard157
 */
public interface ISkConnectionSupplierConstants {

  /**
   * Connection ID used for applications with single connection.
   */
  IdChain CONN_IDC_THE_ONLY = new IdChain( SK_CORE_ID, "TheOnlyOne" ); //$NON-NLS-1$

}
