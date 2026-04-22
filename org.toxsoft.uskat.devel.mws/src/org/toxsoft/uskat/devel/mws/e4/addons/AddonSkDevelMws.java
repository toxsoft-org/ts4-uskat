package org.toxsoft.uskat.devel.mws.e4.addons;

import org.eclipse.e4.core.contexts.*;
import org.toxsoft.core.tsgui.bricks.quant.*;
import org.toxsoft.core.tsgui.mws.bases.*;
import org.toxsoft.uskat.devel.mws.*;

/**
 * Plugin addon - initializes all subsystems and modules..
 *
 * @author hazard157
 */
public class AddonSkDevelMws
    extends MwsAbstractAddon {

  /**
   * Constructor.
   */
  public AddonSkDevelMws() {
    super( Activator.PLUGIN_ID );
    // HERE register keepers
  }

  // ------------------------------------------------------------------------------------
  // MwsAbstractAddon
  //

  @Override
  protected void doRegisterQuants( IQuantRegistrator aQuantRegistrator ) {
    // HERE register quants
  }

  @Override
  protected void initApp( IEclipseContext aAppContext ) {
    // nop
  }

  @Override
  protected void initWin( IEclipseContext aWinContext ) {
    ISkDevelMwsConstants.init( aWinContext );
  }

}
