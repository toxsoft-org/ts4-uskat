package org.toxsoft.uskat.core.gui;

import org.eclipse.e4.core.contexts.*;
import org.toxsoft.core.tsgui.bricks.quant.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tslib.utils.valobj.*;
import org.toxsoft.uskat.core.api.*;
import org.toxsoft.uskat.core.api.ugwis.*;
import org.toxsoft.uskat.core.api.ugwis.kinds.*;
import org.toxsoft.uskat.core.devapi.*;
import org.toxsoft.uskat.core.gui.conn.*;
import org.toxsoft.uskat.core.gui.conn.cfg.*;
import org.toxsoft.uskat.core.gui.conn.m5.*;
import org.toxsoft.uskat.core.gui.conn.valed.*;
import org.toxsoft.uskat.core.gui.km5.*;
import org.toxsoft.uskat.core.gui.km5.first.*;
import org.toxsoft.uskat.core.gui.km5.sded.objed.*;
import org.toxsoft.uskat.core.gui.km5.sded.sded.*;
import org.toxsoft.uskat.core.gui.km5.sded.sded.editors.*;
import org.toxsoft.uskat.core.gui.km5.sgw.*;
import org.toxsoft.uskat.core.gui.ugwi.valed.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * The library quant.
 *
 * @author hazard157
 */
public class QuantSkCoreGui
    extends AbstractQuant
    implements ISkCoreExternalHandler {

  private ISkConnectionSupplier connectionSupplier = null;

  /**
   * Constructor.
   */
  public QuantSkCoreGui() {
    super( QuantSkCoreGui.class.getSimpleName() );
    TsValobjUtils.registerKeeperIfNone( LinkIdSkidList.KEEPER_ID, LinkIdSkidList.KEEPER );
    // register this quant as Core API handler
    SkCoreUtils.registerCoreApiHandler( this );

    // FIXME --- change KM5 initialization to ISkCoreExternalHandler and move code to processSkCoreInitialization()
    KM5Utils.registerContributorCreator( KM5FirstContributor.CREATOR );
    KM5Utils.registerContributorCreator( KM5SgwContributor.CREATOR );
    KM5Utils.registerContributorCreator( KM5SdedContributor.CREATOR );
    KM5Utils.registerContributorCreator( KM5ObjedContributor.CREATOR );
    // ---

  }

  // ------------------------------------------------------------------------------------
  // ISkCoreExternalHandler
  //

  @Override
  public void processSkCoreInitialization( IDevCoreApi aCoreApi ) {
    ISkUgwiService us = aCoreApi.ugwiService();
    us.registerKind( UgwiKindSkAttr.INSTANCE.createUgwiKind( aCoreApi ) );
    us.registerKind( UgwiKindSkSkid.INSTANCE.createUgwiKind( aCoreApi ) );
    us.registerKind( UgwiKindSkLink.INSTANCE.createUgwiKind( aCoreApi ) );
    // TODO add other builtin UGWI kind GUI helpers
  }

  // ------------------------------------------------------------------------------------
  // AbstractQuant
  //

  @Override
  protected void doInitApp( IEclipseContext aAppContext ) {
    IConnectionConfigService ccService = new ConnectionConfigService();
    aAppContext.set( IConnectionConfigService.class, ccService );
  }

  @Override
  protected void doInitWin( IEclipseContext aWinContext ) {
    ISkCoreGuiConstants.init( aWinContext );
    //
    connectionSupplier = new SkConnectionSupplier( aWinContext );
    aWinContext.set( ISkConnectionSupplier.class, connectionSupplier );
    //
    IM5Domain m5 = aWinContext.get( IM5Domain.class );
    m5.addModel( new ConnectionConfigM5Model() );
    //
    IValedControlFactoriesRegistry facReg = aWinContext.get( IValedControlFactoriesRegistry.class );
    facReg.registerFactory( ValedAvStringProviderIdCombo.FACTORY );
    facReg.registerFactory( ValedProviderIdCombo.FACTORY );
    facReg.registerFactory( ValedAvStringConnConfIdCombo.FACTORY );
    facReg.registerFactory( ValedConnConfIdCombo.FACTORY );
    facReg.registerFactory( ValedSkidListEditor.FACTORY );
    facReg.registerFactory( ValedAvValobjSkidListEditor.FACTORY );
    // FIXME dima, it seems to me the next two valed equal each other
    facReg.registerFactory( ValedUgwiSelectorTextAndButton.FACTORY );
    facReg.registerFactory( ValedUgwiSelectorFactory.FACTORY );

  }

  @Override
  protected void doClose() {
    if( connectionSupplier != null ) {
      connectionSupplier.close();
    }
  }

}
