package org.toxsoft.uskat.users.gui;

import org.eclipse.e4.core.contexts.*;
import org.toxsoft.core.tsgui.bricks.quant.*;
import org.toxsoft.uskat.base.gui.km5.*;
import org.toxsoft.uskat.users.gui.km5.*;

/**
 * The library quant.
 *
 * @author hazard157
 */
public class QuantSkUsersGui
    extends AbstractQuant {

  protected QuantSkUsersGui() {
    super( QuantSkUsersGui.class.getSimpleName() );
    KM5Utils.registerContributorCreator( KM5UsersContributor.CREATOR );
  }

  @Override
  protected void doInitApp( IEclipseContext aAppContext ) {
    // nop
  }

  @Override
  protected void doInitWin( IEclipseContext aWinContext ) {
    ISkUsersGuiConstants.init( aWinContext );
  }

}
