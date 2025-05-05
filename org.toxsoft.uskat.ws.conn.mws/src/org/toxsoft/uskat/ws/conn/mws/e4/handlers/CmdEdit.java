package org.toxsoft.uskat.ws.conn.mws.e4.handlers;

import org.eclipse.e4.core.di.annotations.*;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.dialogs.*;
import org.toxsoft.core.tsgui.m5.gui.*;
import org.toxsoft.uskat.ws.conn.mws.*;

/**
 * Command {@link ISkWsConnConstants#CMDID_SKCONN_EDIT}.
 * <p>
 * Invokes dialog to edit list of servers (connection configurations).
 *
 * @author hazard157
 */
public class CmdEdit {

  @Execute
  void exec( Shell aShell ) {

    M5GuiUtils.editModownColl( null, null, null, null )

    // TODO CmdEdit.exec()
    TsDialogUtils.underDevelopment( aShell );
  }

  @CanExecute
  boolean canExec() {
    // TODO CmdEdit.canExec()
    return false;
  }

}
