package org.toxsoft.uskat.ws.conn.mws.e4.handlers;

import org.eclipse.e4.core.di.annotations.*;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.dialogs.*;
import org.toxsoft.uskat.core.gui.conn.*;
import org.toxsoft.uskat.ws.conn.mws.*;

/**
 * Command {@link ISkWsConnConstants#CMDID_SKCONN_SELECT}.
 * <p>
 * Invokes connection selection dialog and connects to the selected server. If no server is configured, invokes the
 * warning dialog.
 * <p>
 * Command is enabled when connection is closed.
 *
 * @author hazard157
 */
public class CmdSelect {

  @Execute
  void exec( Shell aShell ) {
    // TODO CmdSelect.exec()
    TsDialogUtils.underDevelopment( aShell );
  }

  @CanExecute
  boolean canExec( ISkConnectionSupplier aConnectionSupplier ) {
    return !aConnectionSupplier.defConn().state().isOpen();
  }

}
