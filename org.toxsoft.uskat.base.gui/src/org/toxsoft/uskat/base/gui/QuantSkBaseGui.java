package org.toxsoft.uskat.base.gui;

import org.eclipse.e4.core.contexts.*;
import org.toxsoft.core.tsgui.bricks.quant.*;
import org.toxsoft.uskat.base.gui.conn.*;

/**
 * The library quant.
 *
 * @author hazard157
 */
public class QuantSkBaseGui
    extends AbstractQuant {

  /**
   * Constructor.
   */
  public QuantSkBaseGui() {
    super( QuantSkBaseGui.class.getSimpleName() );
  }

  @Override
  protected void doInitApp( IEclipseContext aAppContext ) {
    ISkConnectionSupplier cs = new SkConnectionSupplier();
    aAppContext.set( ISkConnectionSupplier.class, cs );
  }

  @Override
  protected void doInitWin( IEclipseContext aWinContext ) {
    // nop
  }

}
