package org.toxsoft.uskat.ws.conn.mws.e4.handlers;

import org.eclipse.e4.core.di.annotations.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.uskat.core.gui.conn.*;
import org.toxsoft.uskat.core.gui.conn.cfg.*;
import org.toxsoft.uskat.ws.conn.mws.*;
import org.toxsoft.uskat.ws.conn.mws.main.*;

/**
 * Command {@link ISkWsConnConstants#CMDID_SKCONN_CONNECT}.
 * <p>
 * Connects to the connection marked as the "default connection", if no connection is default then executes command
 * {@link ISkWsConnConstants#CMDID_SKCONN_SELECT}.
 * <p>
 * Command is enabled when connection is closed.
 *
 * @author hazard157
 */
public class CmdConnect {

  @Execute
  void exec( IHandlerHelper aHandlerHelper ) {
    // get last successful connection
    IConnectionConfig cfg = aHandlerHelper.findLastConfig();
    if( cfg == null ) {
      return;
    }
    ITsContext connArgs = aHandlerHelper.prepareConnArgs( cfg );
    if( connArgs == null ) {
      return;
    }
    aHandlerHelper.openConnection( connArgs );
  }

  @CanExecute
  boolean canExec( ISkConnectionSupplier aConnectionSupplier ) {
    return !aConnectionSupplier.defConn().state().isOpen();
  }

}
