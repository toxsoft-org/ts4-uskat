package org.toxsoft.uskat.sded.gui;

import org.eclipse.e4.core.contexts.*;
import org.toxsoft.core.tsgui.bricks.quant.*;
import org.toxsoft.uskat.base.gui.km5.*;
import org.toxsoft.uskat.sded.gui.km5.objed.*;
import org.toxsoft.uskat.sded.gui.km5.sded.*;

/**
 * The library quant.
 *
 * @author hazard157
 */
public class QuandSkSdedGui
    extends AbstractQuant {

  /**
   * Constructor.
   */
  public QuandSkSdedGui() {
    super( QuandSkSdedGui.class.getSimpleName() );
    KM5Utils.registerContributorCreator( KM5SdedContributor.CREATOR );
    KM5Utils.registerContributorCreator( KM5ObjedContributor.CREATOR );
  }

  @Override
  protected void doInitApp( IEclipseContext aAppContext ) {
    // nop
  }

  @Override
  protected void doInitWin( IEclipseContext aWinContext ) {
    ISkSdedGuiConstants.init( aWinContext );
  }

}
