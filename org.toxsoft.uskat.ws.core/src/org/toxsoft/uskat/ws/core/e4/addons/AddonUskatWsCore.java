package org.toxsoft.uskat.ws.core.e4.addons;

import org.eclipse.e4.core.contexts.*;
import org.toxsoft.core.tsgui.bricks.quant.*;
import org.toxsoft.core.tsgui.mws.bases.*;
import org.toxsoft.uskat.core.gui.*;
import org.toxsoft.uskat.ws.core.*;
import org.toxsoft.uskat.ws.core.Activator;

/**
 * Plugin addon - initializes all subsystems and modules..
 *
 * @author hazard157
 */
public class AddonUskatWsCore
    extends MwsAbstractAddon {

  /**
   * Constructor.
   */
  public AddonUskatWsCore() {
    super( Activator.PLUGIN_ID );
  }

  // ------------------------------------------------------------------------------------
  // MwsAbstractAddon
  //

  @Override
  protected void doRegisterQuants( IQuantRegistrator aQuantRegistrator ) {
    aQuantRegistrator.registerQuant( new QuantSkCoreGui() );
  }

  @Override
  protected void initApp( IEclipseContext aAppContext ) {
    // nop
  }

  @Override
  protected void initWin( IEclipseContext aWinContext ) {
    IUskatWsCoreConstants.init( aWinContext );
  }

}
