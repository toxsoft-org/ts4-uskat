package org.toxsoft.uskat.ws.conn.mws.e4.handlers;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.ws.conn.mws.ISkWsConnConstants.*;
import static org.toxsoft.uskat.ws.conn.mws.l10n.ISkWsConnSharedResources.*;

import org.eclipse.e4.core.di.annotations.*;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.dialogs.*;
import org.toxsoft.core.tslib.bricks.apprefs.*;
import org.toxsoft.uskat.core.gui.conn.*;
import org.toxsoft.uskat.core.gui.conn.cfg.*;
import org.toxsoft.uskat.ws.conn.mws.*;

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
  void exec( Shell aShell, IAppPreferences aAppPrefs, IConnectionConfigService aCcService ) {
    String lastId = aAppPrefs.getBundle( PBID_CONN_CONFIGS ).prefs().getStr( APREFID_LAST_CONNECTION_ID, EMPTY_STRING );
    if( lastId.isEmpty() ) {
      TsDialogUtils.warn( aShell, MSG_WARN_NO_LAST_CONN );
      return;
    }
    if( !aCcService.listConfigs().hasKey( lastId ) ) {
      TsDialogUtils.warn( aShell, FMT_WARN_LAST_CONN_DELETED, lastId );
      return;
    }

    // TODO CmdConnect.exec()
    TsDialogUtils.underDevelopment( aShell );
  }

  @CanExecute
  boolean canExec( ISkConnectionSupplier aConnectionSupplier ) {
    return !aConnectionSupplier.defConn().state().isOpen();
  }

}
