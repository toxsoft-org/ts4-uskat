package org.toxsoft.uskat.ws.conn.mws.e4.addons;

import org.eclipse.e4.core.contexts.*;
import org.toxsoft.core.tsgui.mws.bases.*;
import org.toxsoft.uskat.ws.conn.mws.*;

/**
 * Plugin addon - initializes all subsystems and modules..
 *
 * @author hazard157
 */
public class AddonSkWsConn
    extends MwsAbstractAddon {

  /**
   * Constructor.
   */
  public AddonSkWsConn() {
    super( Activator.PLUGIN_ID );
  }

  // ------------------------------------------------------------------------------------
  // MwsAbstractAddon
  //

  @Override
  protected void initApp( IEclipseContext aAppContext ) {
    // nop
  }

  @Override
  protected void initWin( IEclipseContext aWinContext ) {
    ISkWsConnConstants.init( aWinContext );
  }

}
