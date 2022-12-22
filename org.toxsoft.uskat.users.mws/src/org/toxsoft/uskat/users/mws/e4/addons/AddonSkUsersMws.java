package org.toxsoft.uskat.users.mws.e4.addons;

import org.eclipse.e4.core.contexts.*;
import org.toxsoft.core.tsgui.mws.bases.*;
import org.toxsoft.core.tsgui.rcp.Activator;
import org.toxsoft.uskat.users.mws.*;
import org.toxsoft.uskat.users.mws.e4.service.*;

/**
 * Plugin's addon.
 *
 * @author hazard157
 */
public class AddonSkUsersMws
    extends MwsAbstractAddon {

  /**
   * Constructor.
   */
  public AddonSkUsersMws() {
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
    ISkUsersMwsConstants.init( aWinContext );
    //
    ICurrentUsersMwsSkRoleService currentUsersMwsSkRoleService = new CurrentUsersMwsSkRoleService();
    aWinContext.set( ICurrentUsersMwsSkRoleService.class, currentUsersMwsSkRoleService );
    ICurrentUsersMwsSkUserService currentUsersMwsSkUserService = new CurrentUsersMwsSkUserService();
    aWinContext.set( ICurrentUsersMwsSkUserService.class, currentUsersMwsSkUserService );
  }

}
