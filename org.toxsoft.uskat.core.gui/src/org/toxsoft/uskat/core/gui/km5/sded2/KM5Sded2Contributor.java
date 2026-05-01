package org.toxsoft.uskat.core.gui.km5.sded2;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.km5.*;
import org.toxsoft.uskat.core.gui.km5.sded2.skobj.*;
import org.toxsoft.uskat.core.gui.km5.sded2.sysdecsr.*;

/**
 * Contributes M5-models for templates entities.
 *
 * @author dima
 */
public class KM5Sded2Contributor
    extends KM5AbstractContributor {

  /**
   * Creator singleton.
   */
  public static final IKM5ContributorCreator CREATOR = KM5Sded2Contributor::new;

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - the connection
   * @param aDomain {@link IM5Domain} - connection domain
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public KM5Sded2Contributor( ISkConnection aConn, IM5Domain aDomain ) {
    super( aConn, aDomain );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private IStridablesListEdit<M5Model<?>> internalCreateAllModels() {
    IStridablesListEdit<M5Model<?>> modelsList = new StridablesList<>();
    modelsList.add( new Sded2DtoPropInfoM5Model( skConn() ) );
    modelsList.add( new Sded2DtoAttrInfoM5Model( skConn() ) );
    modelsList.add( new Sded2DtoLinkInfoM5Model( skConn() ) );
    modelsList.add( new Sded2DtoRivetInfoM5Model( skConn() ) );
    modelsList.add( new Sded2DtoRtdataInfoM5Model( skConn() ) );
    modelsList.add( new Sded2DtoClobInfoM5Model( skConn() ) );
    modelsList.add( new Sded2DtoCmdInfoM5Model( skConn() ) );
    modelsList.add( new Sded2DtoEventInfoM5Model( skConn() ) );
    modelsList.add( new Sded2SkClassInfoM5Model( skConn() ) );
    modelsList.add( new Sded2SkObjectM5Model( skConn() ) );
    // modelsList.add( new MappedSkidsM5Model( skConn() ) );
    // modelsList.add( new StringMapStringM5Model( skConn() ) );
    // modelsList.add( new LinkIdSkidListM5Model() );
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
    // nop - models only Java entities, not ISkSysdescr classes
  }

}
