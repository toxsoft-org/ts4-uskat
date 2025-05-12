package org.toxsoft.uskat.ws.conn.mws.e4.handlers;

import org.eclipse.e4.core.contexts.*;
import org.eclipse.e4.core.di.annotations.*;
import org.toxsoft.core.tsgui.bricks.ctx.impl.*;
import org.toxsoft.uskat.core.gui.conn.*;
import org.toxsoft.uskat.ws.conn.mws.*;

/**
 * Command {@link ISkWsConnConstants#CMDID_SKCONN_EDIT}.
 * <p>
 * Invokes dialog to edit list of servers (connection configurations).
 * <p>
 * Command is always enabled.
 *
 * @author hazard157
 */
public class CmdEdit {

  @Execute
  void exec( IEclipseContext aEclipseContext ) {
    SkConnGuiUtils.editCfgs( new TsGuiContext( aEclipseContext ) );
  }

  @CanExecute
  boolean canExec() {
    return true;
  }

}
