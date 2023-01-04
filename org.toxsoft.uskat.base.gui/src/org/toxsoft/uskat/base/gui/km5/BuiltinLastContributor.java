package org.toxsoft.uskat.base.gui.km5;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Builtin contributor processes all {@link ISkSysdescr#listClasses()} not handled by other contributors.
 * <p>
 * This contributor is not registered in {@link KM5Utils}, it is added as last contributor in the constructor
 * {@link KM5Support#KM5Support(ISkConnection, IM5Domain)}.
 *
 * @author hazard157
 */
class BuiltinLastContributor
    extends KM5AbstractContributor {

  private final IStridablesListEdit<M5Model<? extends ISkObject>> handledModels = new StridablesList<>();

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - the connection
   * @param aDomain {@link IM5Domain} - connection domain
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public BuiltinLastContributor( ISkConnection aConn, IM5Domain aDomain ) {
    super( aConn, aDomain );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private void internalCreateModelsForClasses( IStringList aClassIds ) {
    IStridablesList<ISkClassInfo> allClasses = skSysdescr().listClasses();
    for( String cid : aClassIds ) {
      ISkClassInfo cinf = allClasses.getByKey( cid );
      M5Model<? extends ISkObject> model = new KM5ModelGeneric<>( cinf, skConn() );
      m5().addModel( model );
      handledModels.add( model );
    }
  }

  // ------------------------------------------------------------------------------------
  // KM5AbstractContributor
  //

  @Override
  protected IStringList papiCreateModels() {
    // list unhandled Sk-classes without M5-models for contibution by this last contributor
    IStridablesList<ISkClassInfo> allClasses = skSysdescr().listClasses();
    IStringListEdit classIdsForLastContribution = new StringLinkedBundleList( allClasses.ids() );
    for( String mid : m5().models().ids() ) {
      classIdsForLastContribution.remove( mid );
    }
    internalCreateModelsForClasses( classIdsForLastContribution );
    return classIdsForLastContribution;
  }

  @Override
  protected void papiUpdateModel( ECrudOp aOp, String aClassId ) {
    switch( aOp ) {
      case CREATE: {
        M5Model<? extends ISkObject> model = new KM5ModelGeneric<>( skSysdescr().getClassInfo( aClassId ), skConn() );
        m5().addModel( model );
        handledModels.add( model );
        break;
      }
      case EDIT: {
        M5Model<? extends ISkObject> model = new KM5ModelGeneric<>( skSysdescr().getClassInfo( aClassId ), skConn() );
        m5().replaceModel( model );
        handledModels.put( model );
        break;
      }
      case REMOVE: {
        m5().removeModel( aClassId );
        handledModels.removeById( aClassId );
        break;
      }
      case LIST: {
        // determine list of sk-classes to be handled by this contributor
        IStridablesList<ISkClassInfo> allClasses = skSysdescr().listClasses();
        IStringListEdit classIdsForLastContribution = new StringLinkedBundleList( allClasses.ids() );
        for( String mid : m5().models().ids() ) {
          if( !handledModels.hasKey( mid ) ) {
            classIdsForLastContribution.remove( mid );
          }
        }
        // remove all models created earlier by this contributor
        while( !handledModels.isEmpty() ) {
          String mid = handledModels.ids().first();
          handledModels.removeById( mid );
          m5().removeModel( mid );
        }
        // recreate models
        internalCreateModelsForClasses( classIdsForLastContribution );
        break;
      }
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
  }

}
