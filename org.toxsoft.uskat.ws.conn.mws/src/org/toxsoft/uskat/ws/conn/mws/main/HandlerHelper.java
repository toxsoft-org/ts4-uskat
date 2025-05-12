package org.toxsoft.uskat.ws.conn.mws.main;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.api.users.ISkUserServiceHardConstants.*;
import static org.toxsoft.uskat.core.connection.ISkConnectionConstants.*;
import static org.toxsoft.uskat.ws.conn.mws.ISkWsConnConstants.*;
import static org.toxsoft.uskat.ws.conn.mws.l10n.ISkWsConnSharedResources.*;

import org.eclipse.jface.dialogs.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.ctx.impl.*;
import org.toxsoft.core.tsgui.dialogs.*;
import org.toxsoft.core.tsgui.dialogs.datarec.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tsgui.m5.std.models.av.*;
import org.toxsoft.core.tsgui.panels.misc.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.misc.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.ctx.impl.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.login.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.conn.*;
import org.toxsoft.uskat.core.gui.conn.cfg.*;
import org.toxsoft.uskat.core.gui.utils.*;

/**
 * {@link IHandlerHelper} implementation.
 *
 * @author hazard157
 */
public class HandlerHelper
    implements IHandlerHelper, ISkGuiContextable {

  private final ITsGuiContext tsContext;
  private final ISkConnection skConn;

  /**
   * Constructor.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public HandlerHelper( ITsGuiContext aContext ) {
    TsNullArgumentRtException.checkNull( aContext );
    tsContext = aContext;
    skConn = connectionSupplier().defConn();
  }

  // ------------------------------------------------------------------------------------
  // IHandlerHelper
  //

  @Override
  public IConnectionConfig findLastConfig() {
    String lastId = getAppPrefsValue( PREFBUNDLEID_CONN_CONFIGS, APPREF_LAST_CONNECTION_ID ).asString();
    if( lastId.isBlank() ) {
      TsDialogUtils.warn( getShell(), MSG_WARN_NO_LAST_CONN );
      return null;
    }
    IConnectionConfigService ccService = tsContext().get( IConnectionConfigService.class );
    IConnectionConfig cfg = ccService.listConfigs().findByKey( lastId );
    if( cfg == null ) {
      TsDialogUtils.warn( getShell(), FMT_WARN_LAST_CONN_DELETED, lastId );
    }
    return cfg;
  }

  @Override
  public ITsContext prepareConnArgs( IConnectionConfig aCfg ) {
    TsNullArgumentRtException.checkNull( aCfg );
    ITsContext connArgs = new TsContext();
    ValidationResult vr = SkConnGuiUtils.prepareSkConnArgs( connArgs, aCfg, tsContext() );
    if( TsDialogUtils.askContinueOnValidation( getShell(), vr, MSG_ASK_CONNECT_ON_CFG_WARN ) != ETsDialogCode.YES ) {
      return null;
    }
    // ask user for login info (with default role!)
    IConnectionConfigService ccService = tsContext().get( IConnectionConfigService.class );
    IConnectionConfigProvider ccProvider = ccService.listProviders().getByKey( aCfg.providerId() );
    ESkAuthentificationType authentificationType = ccProvider.backendMetaInfo().getAuthentificationType();
    ILoginInfo initVal = null; // TODO use last usere's login
    ITsGuiContext ctx = new TsGuiContext( tsContext() );
    PanelLoginInfo.OPDEF_IS_ROLE_USED.setValue( ctx.params(), AV_FALSE ); // no role field in dialog
    ILoginInfo loginInfo = SkConnGuiUtils.askUserPassword( authentificationType, initVal, ctx );
    if( loginInfo == null ) {
      return null;
    }
    ARGDEF_LOGIN.setValue( connArgs.params(), avStr( loginInfo.login() ) );
    ARGDEF_PASSWORD.setValue( connArgs.params(), avStr( loginInfo.password() ) );
    ARGDEF_ROLE.setValue( connArgs.params(), avStr( ROLE_ID_USKAT_DEFAULT ) );
    return connArgs;
  }

  @Override
  public boolean openConnection( ITsContext aConnArgs ) {
    TsNullArgumentRtException.checkNull( aConnArgs );
    TsIllegalStateRtException.checkTrue( skConn.state().isOpen() );
    ProgressMonitorDialog pmDlg = new ProgressMonitorDialog( getShell() );
    try {
      pmDlg.run( true, false, aMonitor -> skConn().open( aConnArgs ) );
      if( getAppPrefsValue( PREFBUNDLEID_CONN_CONFIGS, APPREF_IS_DIALOG_AFTER_CONNECT ).asBool() ) {
        ISkBackendInfo binf = skConn().backendInfo();
        String binfStr = StridUtils.printf( StridUtils.FORMAT_ID_NAME, binf );
        TsDialogUtils.info( getShell(), FMT_CONNECTION_SUCCESS, binfStr );
      }
      return true;
    }
    catch( Exception ex ) {
      LoggerUtils.errorLogger().error( ex );
      String msg = ex.getMessage();
      if( msg != null && !msg.isBlank() ) {
        TsDialogUtils.error( getShell(), FMT_ERR_OPEN_CONNECTION, ex.getMessage() );
      }
      else {
        TsDialogUtils.error( getShell(), MSG_ERR_OPEN_CONNECTION );
      }
      return false;
    }
  }

  @Override
  public void showServerInfo() {
    TsIllegalStateRtException.checkFalse( skConn.state().isOpen() );
    ISkBackendInfo binf = skConn().backendInfo();
    ITsDialogInfo di =
        new TsDialogInfo( tsContext(), STR_DLG_SERVER_INFO, STR_DLG_SERVER_INFO_D, ITsDialogConstants.DF_NO_APPROVE );
    IM5Model<IdValue> model = m5().getModel( IdValueM5Model.MODEL_ID, IdValue.class );
    M5DefaultItemsProvider<IdValue> ip = new M5DefaultItemsProvider<>();
    ip.items().add( new IdValue( TSID_ID, avStr( binf.id() ) ) );
    ip.items().add( new IdValue( TSID_NAME, avStr( binf.nmName() ) ) );
    ip.items().add( new IdValue( TSID_DESCRIPTION, avStr( binf.description() ) ) );
    for( String pid : binf.params().keys() ) {
      switch( pid ) {
        case TSID_ID:
        case TSID_NAME:
        case TSID_DESCRIPTION: {
          break;
        }
        default: {
          IAtomicValue pval = binf.params().getByKey( pid );
          ip.items().add( new IdValue( pid, pval ) );
          break;
        }
      }
    }
    IM5CollectionPanel<IdValue> panel = model.panelCreator().createCollViewerPanel( tsContext, ip );
    M5GuiUtils.showCollPanel( di, panel );
  }

  @Override
  public void closeCOnnection() {
    if( !skConn().state().isOpen() ) {
      TsDialogUtils.info( getShell(), MSG_ALREADY_DISCONNECTED );
      return;
    }
    ISkBackendInfo binf = skConn.backendInfo();
    String serverName = StridUtils.printf( StridUtils.FORMAT_ID_NAME, binf );
    if( TsDialogUtils.askYesNoCancel( getShell(), FMT_ASK_REALLY_DISCONNECT, serverName ) == ETsDialogCode.YES ) {
      try {
        skConn.close();
        if( getAppPrefsValue( PREFBUNDLEID_CONN_CONFIGS, APPREF_IS_DIALOG_AFTER_CONNECT ).asBool() ) {
          TsDialogUtils.info( getShell(), MSG_SUCCESSFULLY_DISCONNECTED );
        }
      }
      catch( Exception ex ) {
        LoggerUtils.errorLogger().error( ex );
        TsDialogUtils.warn( getShell(), MSG_WARN_DISCONNECTION_ERROR );
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // ITsGuiContextable
  //

  @Override
  public ITsGuiContext tsContext() {
    return tsContext;
  }

  // ------------------------------------------------------------------------------------
  // ISkGuiContextable
  //

  @Override
  public ISkConnection skConn() {
    return skConn;
  }

}
