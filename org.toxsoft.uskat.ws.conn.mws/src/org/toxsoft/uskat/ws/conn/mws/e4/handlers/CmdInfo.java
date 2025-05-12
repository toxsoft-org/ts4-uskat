package org.toxsoft.uskat.ws.conn.mws.e4.handlers;

import org.eclipse.e4.core.di.annotations.*;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.dialogs.*;
import org.toxsoft.uskat.core.gui.conn.*;
import org.toxsoft.uskat.ws.conn.mws.*;

/**
 * Command {@link ISkWsConnConstants#CMDID_SKCONN_INFO}.
 * <p>
 * Show dialog with connection information.
 * <p>
 * Command is enabled when connection is open.
 *
 * @author hazard157
 */
public class CmdInfo {

  @Execute
  void exec( Shell aShell ) {
    // TODO CmdInfo.exec()
    TsDialogUtils.underDevelopment( aShell );
  }

  @CanExecute
  boolean canExec( ISkConnectionSupplier aConnectionSupplier ) {
    return aConnectionSupplier.defConn().state().isOpen();
  }

}
