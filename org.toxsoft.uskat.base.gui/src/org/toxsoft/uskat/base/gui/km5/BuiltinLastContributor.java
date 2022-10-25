package org.toxsoft.uskat.base.gui.km5;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
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
 * This contributor is not registered in {@link KM5Utils}, it is added as last coontributor in the constructor
 * {@link KM5Support#KM5Support(ISkConnection, IM5Domain)}.
 *
 * @author hazard157
 */
class BuiltinLastContributor
    extends KM5AbstractContributor {

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
    // create default M5-models for unhandled classes
    for( String cid : classIdsForLastContribution ) {
      ISkClassInfo cinf = allClasses.getByKey( cid );
      M5Model<? extends ISkObject> model = new KM5ModelGeneric<>( cinf, skConn() );
      m5().addModel( model );
    }
    return classIdsForLastContribution;
  }

  @Override
  protected boolean papiUpdateModel( ECrudOp aOp, String aClassId ) {
    switch( aOp ) {
      case CREATE: {
        M5Model<?> model = new KM5ModelGeneric<>( skSysdescr().getClassInfo( aClassId ), skConn() );
        m5().addModel( model );
        break;
      }
      case EDIT: {
        M5Model<?> model = new KM5ModelGeneric<>( skSysdescr().getClassInfo( aClassId ), skConn() );
        m5().replaceModel( model );
        break;
      }
      case REMOVE: {
        m5().removeModel( aClassId );
        break;
      }
      case LIST:
        throw new TsInternalErrorRtException();
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
    return true;
  }

}
