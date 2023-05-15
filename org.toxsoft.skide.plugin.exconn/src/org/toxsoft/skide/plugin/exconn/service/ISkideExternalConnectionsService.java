package org.toxsoft.skide.plugin.exconn.service;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.conn.*;

/**
 * The external connections management API.
 * <p>
 * The reference to this interface are put in the application level context.
 *
 * @author hazard157
 */
public interface ISkideExternalConnectionsService {

  /**
   * Invokes connection configuration selection dialog and opens the selected connection.
   * <p>
   * Method returns the ID of the open connection. ID is used as the key to the {@link ISkConnectionSupplier} service,
   * so {@link ISkConnection} may be retrieved by {@link ISkConnectionSupplier#getConn(IdChain)}.
   * <p>
   * The caller must close connection.
   *
   * @param aContext {@link ITsGuiContext} - the context for connection creation
   * @return {@link IdChain} - the ID of the open connection or <code>null</code> if user cancels selection
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  IdChain selectConfigAndOpenConnection( ITsGuiContext aContext );

}
