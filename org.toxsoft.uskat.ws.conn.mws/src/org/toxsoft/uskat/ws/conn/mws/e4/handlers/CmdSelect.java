package org.toxsoft.uskat.ws.conn.mws.e4.handlers;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.ws.conn.mws.ISkWsConnConstants.*;

import org.eclipse.e4.core.contexts.*;
import org.eclipse.e4.core.di.annotations.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.ctx.impl.*;
import org.toxsoft.core.tsgui.mws.services.e4helper.*;
import org.toxsoft.core.tslib.bricks.apprefs.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.uskat.core.gui.conn.*;
import org.toxsoft.uskat.core.gui.conn.cfg.*;
import org.toxsoft.uskat.ws.conn.mws.*;
import org.toxsoft.uskat.ws.conn.mws.main.*;

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
  void exec( IEclipseContext aEclipseContext, ITsE4Helper aHelper ) {
    ITsGuiContext ctx = new TsGuiContext( aEclipseContext );
    IHandlerHelper hh = new HandlerHelper( ctx );
    IAppPreferences aprefs = ctx.get( IAppPreferences.class );
    // ask user to select the server
    String lastCfgId = aprefs.getBundle( PREFBUNDLEID_CONN_CONFIGS ).prefs().getStr( APPREF_LAST_CONNECTION_ID );
    IConnectionConfig cfg = SkConnGuiUtils.selectCfgToConnect( ctx, lastCfgId );
    if( cfg == null ) {
      return;
    }
    // prepare connection arguments and check validity
    ITsContext connArgs = hh.prepareConnArgs( cfg );
    if( connArgs == null ) {
      return;
    }
    // open the default connection with progress dialog
    if( hh.openConnection( connArgs ) ) {
      APPREF_LAST_CONNECTION_ID.setValue( aprefs.getBundle( PREFBUNDLEID_CONN_CONFIGS ).prefs(), avStr( cfg.id() ) );
    }
    aHelper.updateHandlersCanExecuteState();
  }

  @CanExecute
  boolean canExec( ISkConnectionSupplier aConnectionSupplier ) {
    return !aConnectionSupplier.defConn().state().isOpen();
  }

}
