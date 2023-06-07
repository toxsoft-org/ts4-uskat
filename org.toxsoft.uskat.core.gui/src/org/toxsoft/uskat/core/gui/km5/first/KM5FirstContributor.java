package org.toxsoft.uskat.core.gui.km5.first;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.km5.*;

/**
 * Contributes very basic M5-models like {@link Gwid} and {@link Skid}.
 *
 * @author hazard157
 */
public class KM5FirstContributor
    extends KM5AbstractContributor {

  /**
   * Creator singleton.
   */
  public static final IKM5ContributorCreator CREATOR = KM5FirstContributor::new;

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - the connection
   * @param aDomain {@link IM5Domain} - connection domain
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public KM5FirstContributor( ISkConnection aConn, IM5Domain aDomain ) {
    super( aConn, aDomain );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private IStridablesListEdit<M5Model<?>> internalCreateAllModels() {
    IStridablesListEdit<M5Model<?>> modelsList = new StridablesList<>();
    // FIXME
    // modelsList.add( new GwidKM5Model( skConn() ) );
    // modelsList.add( new SkidKM5Model( skConn() ) );
    return modelsList;
  }

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
    // models only Java entities, not ISkSysdescr classes
  }

}
