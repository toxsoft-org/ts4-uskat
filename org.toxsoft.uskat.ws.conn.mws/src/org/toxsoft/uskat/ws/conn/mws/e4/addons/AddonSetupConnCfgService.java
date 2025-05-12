package org.toxsoft.uskat.ws.conn.mws.e4.addons;

import static org.toxsoft.uskat.ws.conn.mws.ISkWsConnConstants.*;

import java.io.*;

import org.eclipse.e4.core.contexts.*;
import org.toxsoft.core.tsgui.mws.bases.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.core.tslib.utils.progargs.*;
import org.toxsoft.uskat.backend.memtext.*;
import org.toxsoft.uskat.backend.s5.gui.utils.*;
import org.toxsoft.uskat.backend.sqlite.*;
import org.toxsoft.uskat.core.gui.conn.cfg.*;
import org.toxsoft.uskat.ws.conn.mws.*;

/**
 * Sets up {@link IConnectionConfigService}.
 * <p>
 * Following actions are performed:
 * <ul>
 * <li>registers all providers known by this plugin;</li>
 * <li>processes {@link ISkWsConnConstants#CLINEARG_CONN_CFG_FILE_NAME} to specify {@link #connCfgFile};</li>
 * <li>{@link IConnectionConfigService} is set up to store data in {@link #connCfgFile};</li>
 * <li>if {@link #connCfgFile} exists on startp, data will be loaded.</li>
 * </ul>
 *
 * @author hazard157
 */
public class AddonSetupConnCfgService
    extends MwsAbstractAddon {

  private final IConnectionConfigServiceListener connConfigServiceListener = new IConnectionConfigServiceListener() {

    @Override
    public void onProvidersListChanged( IConnectionConfigService aSource ) {
      // nop
    }

    @Override
    public void onConfigsListChanged( IConnectionConfigService aSource ) {
      ConnectionConfig.KEEPER.writeColl( connCfgFile, ccService.listConfigs(), true );
    }
  };

  private File                     connCfgFile = null;
  private IConnectionConfigService ccService   = null;

  /**
   * Constructor.
   */
  public AddonSetupConnCfgService() {
    super( Activator.PLUGIN_ID );
  }

  // ------------------------------------------------------------------------------------
  // MwsAbstractAddon
  //

  @Override
  protected void initApp( IEclipseContext aAppContext ) {
    ccService = aAppContext.get( IConnectionConfigService.class );
    // register providers
    ccService.registerPovider( S5ConnectionConfigProvider.INSTANCE );
    ccService.registerPovider( new ConnectionConfigProvider( MtbBackendToFile.PROVIDER, IOptionSet.NULL ) );
    ccService.registerPovider( new ConnectionConfigProvider( SkBackendSqlite.PROVIDER, IOptionSet.NULL ) );
    // process command line and specify storage file
    ProgramArgs progArgs = aAppContext.get( ProgramArgs.class );
    String connCfgFileName = progArgs.getArgValue( CLINEARG_CONN_CFG_FILE_NAME, DEFAULT_CONN_CFG_FILE_NAME );
    connCfgFile = new File( connCfgFileName );
    // load connection configuration data from the file
    if( connCfgFile.exists() ) {
      try {
        IList<IConnectionConfig> ll = ConnectionConfig.KEEPER.readColl( connCfgFile );
        for( IConnectionConfig cc : ll ) {
          ValidationResult vr = ccService.svs().validator().canAddConfig( ccService, cc );
          if( !vr.isError() ) {
            ccService.defineConfig( cc );
          }
          else {
            LoggerUtils.errorLogger().warning( vr.message() );
          }
        }
      }
      catch( Exception ex ) {
        LoggerUtils.errorLogger().error( ex );
      }
    }
    // setup connection configuration data to be stored in the file
    ccService.eventer().addListener( connConfigServiceListener );
  }

  @Override
  protected void initWin( IEclipseContext aWinContext ) {
    // nop
  }

}
