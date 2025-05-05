package org.toxsoft.uskat.ws.conn.mws.e4.handlers;

import org.eclipse.e4.core.di.annotations.*;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.dialogs.*;
import org.toxsoft.uskat.ws.conn.mws.*;

/**
 * Command {@link ISkWsConnConstants#CMDID_SKCONN_CONNECT}.
 * <p>
 * Connects to the connection marked as the "default connection", if no connection is default then executes command
 * {@link ISkWsConnConstants#CMDID_SKCONN_SELECT}.
 *
 * @author hazard157
 */
public class CmdConnect {

  @Execute
  void exec( Shell aShell ) {
    // TODO CmdConnect.exec()
    TsDialogUtils.underDevelopment( aShell );
  }

  @CanExecute
  boolean canExec() {
    // TODO CmdConnect.canExec()
    return false;
  }

}
