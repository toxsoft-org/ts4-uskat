package org.toxsoft.uskat.backend.s5.gui;

import org.eclipse.e4.core.contexts.*;
import org.toxsoft.core.tsgui.bricks.quant.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.uskat.backend.s5.gui.m5.hostlist.*;
import org.toxsoft.uskat.backend.s5.gui.utils.*;
import org.toxsoft.uskat.core.gui.conn.cfg.*;
import org.toxsoft.uskat.s5.utils.*;

/**
 * The library quant.
 *
 * @author hazard157
 */
public class QuantSkBackendS5Gui
    extends AbstractQuant {

  /**
   * Constructor.
   */
  public QuantSkBackendS5Gui() {
    super( QuantSkBackendS5Gui.class.getSimpleName() );
    S5ValobjUtils.registerS5Keepers();
  }

  @Override
  protected void doInitApp( IEclipseContext aAppContext ) {
    ConnectionConfigService ccService = (ConnectionConfigService)aAppContext.get( IConnectionConfigService.class );
    ccService.registerPovider( S5ConnectionConfigProvider.INSTANCE );
  }

  @Override
  protected void doInitWin( IEclipseContext aWinContext ) {
    ISkBackendS5GuiConstants.init( aWinContext );
    //
    IValedControlFactoriesRegistry vcfReg = aWinContext.get( IValedControlFactoriesRegistry.class );
    vcfReg.registerFactory( ValedS5HostList.FACTORY );
    vcfReg.registerFactory( ValedAvValobjS5HostList.FACTORY );
    //
    IM5Domain m5 = aWinContext.get( IM5Domain.class );
    m5.addModel( new S5HostM5Model() );
    m5.addModel( new S5HostListM5Model() );
  }

}
