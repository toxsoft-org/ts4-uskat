package org.toxsoft.uskat.refbooks.gui;

import org.eclipse.e4.core.contexts.*;
import org.toxsoft.core.tsgui.bricks.quant.*;

/**
 * The library quant.
 *
 * @author hazard157
 */
public class QuantSkRefbooksGui
    extends AbstractQuant {

  protected QuantSkRefbooksGui() {
    super( QuantSkRefbooksGui.class.getSimpleName() );
  }

  @Override
  protected void doInitApp( IEclipseContext aAppContext ) {
    // nop
  }

  @Override
  protected void doInitWin( IEclipseContext aWinContext ) {
    ISkRefbooksGuiConstants.init( aWinContext );
  }

}
