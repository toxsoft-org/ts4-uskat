package org.toxsoft.uskat.core.gui.km5.sded.objed;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.km5.*;

/**
 * Contributes M5-models for templates entities.
 *
 * @author dima
 */

// FIXME GOGA Do we really need this class? It does nothing!

public class KM5ObjedContributor
    extends KM5AbstractContributor {

  /**
   * Creator singleton.
   */
  public static final IKM5ContributorCreator CREATOR = KM5ObjedContributor::new;

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - the connection
   * @param aDomain {@link IM5Domain} - connection domain
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  KM5ObjedContributor( ISkConnection aConn, IM5Domain aDomain ) {
    super( aConn, aDomain );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private IStridablesListEdit<M5Model<?>> internalCreateAllModels() {
    IStridablesListEdit<M5Model<?>> modelsList = new StridablesList<>();
    // TODO add all models of this KM5 unit
    return modelsList;
  }

  // private boolean isObjedClassId( String aClassId ) {
  // // TODO реализовать KM5ObjedContributor.isObjedClassId()
  // throw new TsUnderDevelopmentRtException( "KM5ObjedContributor.isObjedClassId()" );
  // }

  // ------------------------------------------------------------------------------------
  // KM5AbstractContributor
  //

  @Override
  protected IStringList papiCreateModels() {
    IStridablesListEdit<M5Model<?>> modelsList = internalCreateAllModels();
    for( M5Model<?> model : modelsList ) {
      m5().addModel( model );
    }
    return modelsList.ids();
  }

  @Override
  protected void papiUpdateModel( ECrudOp aOp, String aClassId ) {
    if( aOp == ECrudOp.LIST || aClassId == null ) {
      internalCreateAllModels();

    }
    // FIXME test if this is OBJED class and recreate model
  }

}
