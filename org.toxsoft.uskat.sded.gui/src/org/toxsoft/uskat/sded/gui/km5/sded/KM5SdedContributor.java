package org.toxsoft.uskat.sded.gui.km5.sded;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.base.gui.km5.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Contributes M5-models for templates entities.
 *
 * @author dima
 */
public class KM5SdedContributor
    extends KM5AbstractContributor {

  /**
   * Creator singleton.
   */
  public static final IKM5ContributorCreator CREATOR = KM5SdedContributor::new;

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - the connection
   * @param aDomain {@link IM5Domain} - connection domain
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public KM5SdedContributor( ISkConnection aConn, IM5Domain aDomain ) {
    super( aConn, aDomain );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private IStridablesListEdit<M5Model<?>> internalCreateAllModels() {
    IStridablesListEdit<M5Model<?>> modelsList = new StridablesList<>();
    modelsList.add( new SdedDtoAttrInfoM5Model( skConn() ) );
    modelsList.add( new SdedDtoRtdataInfoM5Model( skConn() ) );
    modelsList.add( new SdedDtoClobInfoM5Model( skConn() ) );
    modelsList.add( new SdedDtoRivetInfoM5Model( skConn() ) );
    modelsList.add( new SdedDtoLinkInfoM5Model( skConn() ) );
    modelsList.add( new SdedDtoCmdInfoM5Model( skConn() ) );
    modelsList.add( new SdedDtoEvInfoM5Model( skConn() ) );
    modelsList.add( new SdedDtoClassInfoM5Model( skConn() ) );
    modelsList.add( new SdedSkClassInfoM5Model( skConn() ) );
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
    // SDED models only Java entities, not ISkSysdescr classes
  }

}
