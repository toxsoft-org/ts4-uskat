package org.toxsoft.uskat.ws.conn.mws.e4.handlers;

import static org.toxsoft.uskat.ws.conn.mws.l10n.ISkWsConnSharedResources.*;

import org.eclipse.e4.core.di.annotations.*;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.dialogs.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.conn.*;
import org.toxsoft.uskat.ws.conn.mws.*;

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
  void exec( Shell aShell, ISkConnectionSupplier aConnectionSupplier ) {
    ISkConnection skConn = aConnectionSupplier.defConn();
    if( skConn.state().isOpen() ) {
      TsDialogUtils.info( aShell, MSG_ALREADY_DISCONNECTED );
      return;
    }
    ISkBackendInfo binf = skConn.backendInfo();
    String serverName = StridUtils.printf( StridUtils.FORMAT_ID_NAME, binf );
    if( TsDialogUtils.askYesNoCancel( aShell, FMT_ASK_REALLY_DISCONNECT, serverName ) == ETsDialogCode.YES ) {
      try {
        skConn.close();
        TsDialogUtils.info( aShell, MSG_SUCCESSFULLY_DISCONNECTED );
      }
      catch( Exception ex ) {
        LoggerUtils.errorLogger().error( ex );
        TsDialogUtils.warn( aShell, MSG_WARN_DISCONNECTION_ERROR );
      }
    }
  }

  @CanExecute
  boolean canExec( ISkConnectionSupplier aConnectionSupplier ) {
    return aConnectionSupplier.defConn().state().isOpen();
  }

}
