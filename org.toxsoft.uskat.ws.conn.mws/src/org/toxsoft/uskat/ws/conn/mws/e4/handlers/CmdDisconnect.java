package org.toxsoft.uskat.ws.conn.mws.e4.handlers;

import org.eclipse.e4.core.di.annotations.*;
import org.toxsoft.uskat.core.gui.conn.*;
import org.toxsoft.uskat.ws.conn.mws.*;
import org.toxsoft.uskat.ws.conn.mws.main.*;

/**
 * Command {@link ISkWsConnConstants#CMDID_SKCONN_DISCONNECT}.
 * <p>
 * Disconnects current connection {@link ISkConnectionSupplier#defConn()}.
 * <p>
 * Command is enabled when connection is open.
 *
 * @author hazard157
 */
public class CmdDisconnect {

  @Execute
  void exec( IHandlerHelper aHandlerHelper ) {
    aHandlerHelper.closeCOnnection();
  }

  @CanExecute
  boolean canExec( ISkConnectionSupplier aConnectionSupplier ) {
    return aConnectionSupplier.defConn().state().isOpen();
  }

}
