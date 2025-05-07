package org.toxsoft.uskat.ws.conn.mws.e4.handlers;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.core.api.users.ISkUserServiceHardConstants.*;
import static org.toxsoft.uskat.core.connection.ISkConnectionConstants.*;
import static org.toxsoft.uskat.ws.conn.mws.l10n.ISkWsConnSharedResources.*;

import org.eclipse.e4.core.contexts.*;
import org.eclipse.e4.core.di.annotations.*;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.ctx.impl.*;
import org.toxsoft.core.tsgui.dialogs.*;
import org.toxsoft.core.tsgui.mws.services.e4helper.*;
import org.toxsoft.core.tsgui.panels.misc.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.ctx.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.login.*;
import org.toxsoft.uskat.core.gui.conn.*;
import org.toxsoft.uskat.core.gui.conn.cfg.*;
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
  void exec( Shell aShell, IEclipseContext aEclipseContext, ITsE4Helper aHelper ) {
    ITsGuiContext ctx = new TsGuiContext( aEclipseContext );
    // ask user to select the server
    IConnectionConfig cfg = SkConnGuiUtils.selectCfgToConnect( ctx );
    if( cfg == null ) {
      return;
    }
    // prepare connection arguments and check validity
    ITsContext connArgs = new TsContext();
    ValidationResult vr = SkConnGuiUtils.prepareSkConnArgs( connArgs, cfg, ctx );
    if( TsDialogUtils.askContinueOnValidation( aShell, vr, MSG_ASK_CONNECT_ON_CFG_WARN ) != ETsDialogCode.YES ) {
      return;
    }
    // ask user for login info (with default role!)
    ILoginInfo initVal = null; // TODO use last usere's login
    PanelLoginInfo.OPDEF_IS_ROLE_USED.setValue( ctx.params(), AV_FALSE ); // no role field in dialog
    ILoginInfo loginInfo = PanelLoginInfo.edit( ctx, initVal, ITsValidator.PASS );
    if( loginInfo == null ) {
      return;
    }
    ARGDEF_LOGIN.setValue( connArgs.params(), avStr( loginInfo.login() ) );
    ARGDEF_PASSWORD.setValue( connArgs.params(), avStr( loginInfo.password() ) );
    ARGDEF_ROLE.setValue( connArgs.params(), avStr( ROLE_ID_USKAT_DEFAULT ) );
    // open the connection with progress dialog

    try {

      // DEBUG
      TsDialogUtils.underDevelopment( aShell );
      // HandlerUtils.openConnection( aEclipseContext, cfg, null );

      // TODO remember last connection config ID

    }
    finally {
      aHelper.updateHandlersCanExecuteState();
    }
  }

  @CanExecute
  boolean canExec( ISkConnectionSupplier aConnectionSupplier ) {
    return !aConnectionSupplier.defConn().state().isOpen();
  }

}
