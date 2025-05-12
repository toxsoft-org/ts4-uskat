package org.toxsoft.uskat.core.gui.conn;

import static org.toxsoft.core.tsgui.dialogs.datarec.ITsDialogConstants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.core.api.users.ISkUserServiceHardConstants.*;
import static org.toxsoft.uskat.core.gui.conn.l10n.ISkCoreGuiConnSharedResources.*;
import static org.toxsoft.uskat.core.gui.conn.m5.IConnectionConfigM5Constants.*;

import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.ctx.impl.*;
import org.toxsoft.core.tsgui.dialogs.*;
import org.toxsoft.core.tsgui.dialogs.datarec.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.panels.misc.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.login.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.conn.cfg.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * Helper methods to work with the Sk-connection in GUI environment.
 *
 * @author hazard157
 */
public class SkConnGuiUtils {

  /**
   * Invokes dialog and asks (if necessary) user to enter login and password.
   * <p>
   * Does not asks for role ID.
   * <p>
   * Depending on authentification type, method may return immediately with the some default values.
   *
   * @param aType {@link EAtomicType} - authentification type
   * @param aInitVals {@link ILoginInfo} - initial values when asking user, may be <code>null</code>
   * @param aContext {@link ITsGuiContext} - the context
   * @return {@link ILoginInfo} - filled info or <code>null</code> on cancel
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static ILoginInfo askUserPassword( ESkAuthentificationType aType, ILoginInfo aInitVals,
      ITsGuiContext aContext ) {
    TsNullArgumentRtException.checkNulls( aType, aContext );
    return switch( aType ) {
      case NONE -> new LoginInfo( USER_ID_ROOT, INITIAL_ROOT_PASSWORD, ROLE_ID_USKAT_DEFAULT );
      case SIMPLE -> {
        ITsGuiContext ctx = new TsGuiContext( aContext );
        PanelLoginInfo.OPDEF_IS_ROLE_USED.setValue( ctx.params(), AV_FALSE ); // no role field in dialog
        yield PanelLoginInfo.edit( ctx, aInitVals, ITsValidator.PASS );
      }
      default -> throw new TsNotAllEnumsUsedRtException();
    };
  }

  /**
   * Edits configuration of the {@link IConnectionConfigService} found in the context.
   *
   * @param aContext {@link ITsGuiContext} =- the GUI context
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static void editCfgs( ITsGuiContext aContext ) {
    TsNullArgumentRtException.checkNull( aContext );
    IM5Domain m5 = aContext.get( IM5Domain.class );
    IM5Model<IConnectionConfig> model = m5.getModel( MID_SK_CONN_CFG, IConnectionConfig.class );
    IConnectionConfigService ccService = aContext.get( IConnectionConfigService.class );
    IM5LifecycleManager<IConnectionConfig> lm = model.getLifecycleManager( ccService );
    TsDialogInfo cdi = new TsDialogInfo( aContext, DLG_EDIT_CONFIGS, DLG_EDIT_CONFIGS_D, DF_NO_APPROVE );
    cdi.setMinSizeShellRelative( 10, 50 );
    cdi.setMaxSizeShellRelative( 50, 80 );
    IM5CollectionPanel<IConnectionConfig> panel =
        model.panelCreator().createCollEditPanel( aContext, lm.itemsProvider(), lm );
    M5GuiUtils.showCollPanel( cdi, panel );
  }

  /**
   * Selects configuration of the {@link IConnectionConfigService} found in the context.
   * <p>
   * Method assumes that selection is done for immediate connection to the server so appropriate message is displayed to
   * the user.
   *
   * @param aContext {@link ITsGuiContext} =- the GUI context
   * @param aInitalCfgId String - initially selected configuration ID or <code>null</code>
   * @return {@link IConnectionConfig} - selected configuration or <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static IConnectionConfig selectCfgToConnect( ITsGuiContext aContext, String aInitalCfgId ) {
    TsNullArgumentRtException.checkNull( aContext );
    IM5Domain m5 = aContext.get( IM5Domain.class );
    IM5Model<IConnectionConfig> model = m5.getModel( MID_SK_CONN_CFG, IConnectionConfig.class );
    IConnectionConfigService ccService = aContext.get( IConnectionConfigService.class );
    IM5LifecycleManager<IConnectionConfig> lm = model.getLifecycleManager( ccService );
    TsDialogInfo cdi = new TsDialogInfo( aContext, DLG_SELECT_CFG, DLG_SELECT_CFG_D );
    cdi.setMinSizeShellRelative( 10, 50 );
    cdi.setMaxSizeShellRelative( 50, 80 );
    IConnectionConfig initCfg = null;
    if( aInitalCfgId != null ) {
      initCfg = ccService.listConfigs().findByKey( aInitalCfgId );
    }
    return M5GuiUtils.askSelectItem( cdi, model, initCfg, lm.itemsProvider(), null );
  }

  /**
   * Prepares Sk-connection opening arguments for the GUI environment.
   * <p>
   * Prepered references and options for Sk-connection opening will be written to the <code>aConnArgs</code>.
   * <p>
   * Returned error means that prepared arguments can not be used for connection opening,
   * {@link ISkConnection#open(ITsContextRo)} will thow an exception.
   * <p>
   * Note: following options may be required to be added to the <code>aConnArgs</code>:
   * {@link ISkConnectionConstants#ARGDEF_LOGIN}, {@link ISkConnectionConstants#ARGDEF_PASSWORD} and
   * {@link ISkConnectionConstants#ARGDEF_ROLE}.
   *
   * @param aConnArgs {@link ITsContext} - editable arguments to be prepared
   * @param aCfg {@link IConnectionConfig} - connecion opening parameters
   * @param aContext {@link ITsGuiContext} - the GUI application context
   * @return {@link ValidationResult} - preparation success indication
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static ValidationResult prepareSkConnArgs( ITsContext aConnArgs, IConnectionConfig aCfg,
      ITsGuiContext aContext ) {
    TsNullArgumentRtException.checkNulls( aConnArgs, aCfg, aContext );
    IConnectionConfigService ccService = aContext.get( IConnectionConfigService.class );
    // fill opening arguments with the connection kind specific options and references
    IConnectionConfigProvider ccProvider = ccService.listProviders().findByKey( aCfg.providerId() );
    if( ccProvider == null ) {
      return ValidationResult.error( FMT_ERR_UNKNOWN_PROVIDER, aCfg.id() );
    }
    ccProvider.fillArgs( aConnArgs, aCfg.opValues() );
    // fill arguments with the GUI-specific references
    Display display = aContext.get( Display.class );
    ISkCoreConfigConstants.REFDEF_THREAD_EXECUTOR.setRef( aConnArgs, new SkGuiThreadExecutor( display ) );
    // assume defult role (this is harmless operation for connection that does not need login)
    ISkConnectionConstants.ARGDEF_ROLE.setValue( aConnArgs.params(),
        avStr( ISkUserServiceHardConstants.ROLE_ID_USKAT_DEFAULT ) );
    ccProvider.fillArgs( aConnArgs, aCfg.opValues() );
    return ValidationResult.SUCCESS;
  }

  /**
   * Opens the connection with the specified parameters displaying the progress dialog.
   * <p>
   * TODO what this method does?
   *
   * @param aConn {@link ISkConnection} - the connectio to open
   * @param aCfg {@link IConnectionConfig} - connecion opening parameters
   * @param aContext {@link ITsGuiContext} - the GUI application context
   * @return {@link ValidationResult} - opening process success indication
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException connecion is already open
   */
  public static ValidationResult openConnection( ISkConnection aConn, IConnectionConfig aCfg, ITsGuiContext aContext ) {
    TsNullArgumentRtException.checkNulls( aConn, aCfg, aContext );
    TsIllegalArgumentRtException.checkTrue( aConn.state().isOpen() );

    // TODO SkConnGuiUtils.openConnection()

    TsDialogUtils.underDevelopment( aContext.get( Shell.class ) );
    return ValidationResult.error( "Under development" );
  }

  /**
   * No subclasses.
   */
  private SkConnGuiUtils() {
    // nop
  }

}
