package org.toxsoft.uskat.mnemo.gui;

import org.eclipse.e4.core.contexts.*;
import org.toxsoft.core.tsgui.bricks.quant.*;

/**
 * The library quant.
 *
 * @author goga
 */
public class QuantSkMnemoGui
    extends AbstractQuant {

  /**
   * Constructor.
   */
  public QuantSkMnemoGui() {
    super( QuantSkMnemoGui.class.getSimpleName() );
  }

  @Override
  protected void doInitApp( IEclipseContext aAppContext ) {
    // nop
  }

  @Override
  protected void doInitWin( IEclipseContext aWinContext ) {
    ISkMnemoGuiConstants.init( aWinContext );
  }

}
