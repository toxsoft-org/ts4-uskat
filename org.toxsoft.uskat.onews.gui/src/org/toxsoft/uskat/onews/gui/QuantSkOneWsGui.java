package org.toxsoft.uskat.onews.gui;

import org.eclipse.e4.core.contexts.*;
import org.toxsoft.core.tsgui.bricks.quant.*;
import org.toxsoft.uskat.base.gui.km5.*;
import org.toxsoft.uskat.core.impl.*;
import org.toxsoft.uskat.onews.gui.km5.*;
import org.toxsoft.uskat.onews.lib.impl.*;

/**
 * The library quant.
 *
 * @author hazard157
 */
public class QuantSkOneWsGui
    extends AbstractQuant {

  /**
   * Constructor.
   */
  public QuantSkOneWsGui() {
    super( QuantSkOneWsGui.class.getSimpleName() );
    KM5Utils.registerContributorCreator( KM5OneWsContributor.CREATOR );
    SkCoreUtils.registerSkServiceCreator( SkExtServOneWs.CREATOR );
  }

  @Override
  protected void doInitApp( IEclipseContext aAppContext ) {
    // nop
  }

  @Override
  protected void doInitWin( IEclipseContext aWinContext ) {
    ISkOneWsGuiConstants.init( aWinContext );
  }

}
