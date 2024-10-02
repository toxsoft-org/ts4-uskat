package org.toxsoft.uskat.ws.exe.e4.addons;

import org.eclipse.e4.core.contexts.*;
import org.toxsoft.core.tsgui.mws.bases.*;
import org.toxsoft.uskat.ws.exe.*;

/**
 * Application main addons.
 *
 * @author hazard157
 */
public class AddonUskatWsExe
    extends MwsAbstractAddon {

  /**
   * Constructor.
   */
  public AddonUskatWsExe() {
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
    // nop
  }

}
