package org.toxsoft.uskat.base.gui;

import org.eclipse.e4.core.contexts.*;
import org.toxsoft.core.tsgui.bricks.quant.*;
import org.toxsoft.uskat.base.gui.conn.*;
import org.toxsoft.uskat.base.gui.km5.*;
import org.toxsoft.uskat.base.gui.km5.sgw.*;

/**
 * The library quant.
 *
 * @author hazard157
 */
public class QuantSkBaseGui
    extends AbstractQuant {

  private ISkConnectionSupplier connectionSupplier = null;

  /**
   * Constructor.
   */
  public QuantSkBaseGui() {
    super( QuantSkBaseGui.class.getSimpleName() );
    KM5Utils.registerContributorCreator( KM5SgwContributor.CREATOR );
  }

  @Override
  protected void doInitApp( IEclipseContext aAppContext ) {
    connectionSupplier = new SkConnectionSupplier();
    aAppContext.set( ISkConnectionSupplier.class, connectionSupplier );
  }

  @Override
  protected void doInitWin( IEclipseContext aWinContext ) {
    // nop
  }

  @Override
  protected void doClose() {
    if( connectionSupplier != null ) {
      connectionSupplier.close();
    }
  }

}
