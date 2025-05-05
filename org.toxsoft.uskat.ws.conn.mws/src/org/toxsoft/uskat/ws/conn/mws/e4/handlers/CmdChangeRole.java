package org.toxsoft.uskat.ws.conn.mws.e4.handlers;

import org.eclipse.e4.core.di.annotations.*;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.dialogs.*;
import org.toxsoft.uskat.core.gui.conn.*;
import org.toxsoft.uskat.ws.conn.mws.*;

/**
 * Command {@link ISkWsConnConstants#CMDID_SKCONN_CHANGE_ROLE}.
 * <p>
 * Invokes dialog with current user's roles list. After selectiong another role (not with user is currently logged in)
 * closes corrent session and asks login/password for connection with the new role. If user has only one role, displays
 * information dialog instead of role selection.
 * <p>
 * Command is enabled when connection is open.
 *
 * @author hazard157
 */
public class CmdChangeRole {

  @Execute
  void exec( Shell aShell ) {
    // TODO CmdChangeRole.exec()
    TsDialogUtils.underDevelopment( aShell );
  }

  @CanExecute
  boolean canExec( ISkConnectionSupplier aConnectionSupplier ) {
    return !aConnectionSupplier.defConn().state().isOpen();
  }

}
