package org.toxsoft.uskat.backend.memtext;

import static org.toxsoft.uskat.core.ISkHardConstants.*;

import java.util.*;

import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.strio.impl.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * {@link IBaClasses} implementation.
 *
 * @author hazard157
 */
class MtbBaClasses
    extends MtbAbstractAddon
    implements IBaClasses {

  private static final String KW_CLASS_INFOS = "ClassInfos"; //$NON-NLS-1$

  private final IStridablesListEdit<IDtoClassInfo> classInfos = new StridablesList<>();

  /**
   * Constructor.
   *
   * @param aOwner {@link MtbAbstractBackend} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public MtbBaClasses( MtbAbstractBackend aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_CLASSES );
  }

  // ------------------------------------------------------------------------------------
  // BackendAddonBase
  //

  @Override
  public void close() {
    // GOGA 2024-02-02 --- do NOT save classes defined by the Java code
    IStringListEdit cidsToRemove = new StringArrayList();
    for( IDtoClassInfo cinf : classInfos ) {
      // try to remove
      if( OPDEF_SK_IS_SOURCE_CODE_DEFINED_CLASS.getValue( cinf.params() ).asBool() ) {
        if( !hasNonSourceCodeSubclass( cinf ) ) {
          cidsToRemove.add( cinf.id() );
        }
      }
    }
    for( String cid : cidsToRemove ) {
      classInfos.removeById( cid );
    }
    // ---
  }

  @Override
  public void clear() {
    classInfos.clear();
  }

  @Override
  protected void doWrite( IStrioWriter aSw ) {
    StrioUtils.writeKeywordHeader( aSw, KW_CLASS_INFOS, true );

    DtoClassInfo.KEEPER.writeColl( aSw, classInfos, true );

    // /**
    // * We'll remove classes defined by the core services.
    // */
    // IStridablesListEdit<IDtoClassInfo> toSave = new StridablesList<>();
    // for( IDtoClassInfo cinf : classInfos ) {
    // boolean isSrcCode = OPDEF_SK_IS_SOURCE_CODE_DEFINED_CLASS.getValue( cinf.params() ).asBool();
    // boolean isCoreClass = OPDEF_SK_IS_SOURCE_USKAT_CORE_CLASS.getValue( cinf.params() ).asBool();
    // if( !isSrcCode && !isCoreClass ) {
    // toSave.add( cinf );
    // }
    // }
    // DtoClassInfo.KEEPER.writeColl( aSw, toSave, true );
  }

  @Override
  protected void doRead( IStrioReader aSr ) {
    StrioUtils.ensureKeywordHeader( aSr, KW_CLASS_INFOS );
    classInfos.clear();
    DtoClassInfo.KEEPER.readColl( aSr, classInfos );
  }

  private boolean hasNonSourceCodeSubclass( IDtoClassInfo aClassInfo ) {
    for( IDtoClassInfo cinfSub : classInfos ) {
      if( cinfSub.parentId().equals( aClassInfo.id() ) ) {
        if( !OPDEF_SK_IS_SOURCE_CODE_DEFINED_CLASS.getValue( cinfSub.params() ).asBool() ) {
          return true;
        }
        boolean hasNonJavaSubSub = hasNonSourceCodeSubclass( aClassInfo );
        if( hasNonJavaSubSub ) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  void papiRemoveEntitiesOfClassIdsBeforeSave( IStringList aClassIds ) {
    // nop - class definitions shall not be removed!
  }

  // ------------------------------------------------------------------------------------
  // IBaClasses
  //

  @Override
  public IStridablesList<IDtoClassInfo> readClassInfos() {
    return classInfos;
  }

  /**
   * FIXME in #writeClassInfos setChanged() must be called only once! <br>
   * FIXME in #writeClassInfos setChanged() must NOT be called if there were no changes
   */

  @Override
  public void writeClassInfos( IStringList aRemoveClassIds, IStridablesList<IDtoClassInfo> aUpdateClassInfos ) {
    internalCheck();
    TsIllegalArgumentRtException.checkTrue( aUpdateClassInfos.hasKey( IGwHardConstants.GW_ROOT_CLASS_ID ) );

    // prepare for frontend message
    int changesCount = 0;
    ECrudOp op = null;
    String changedId = null;

    // remove classes
    if( aRemoveClassIds != null ) {
      for( String classId : aRemoveClassIds ) {
        if( classInfos.removeById( classId ) != null ) {
          ++changesCount;
          op = ECrudOp.REMOVE;
          changedId = classId;
        }
      }
    }
    else {
      switch( classInfos.size() ) {
        case 0: {
          break;
        }
        case 1: {
          ++changesCount;
          op = ECrudOp.REMOVE;
          changedId = classInfos.ids().first();
          classInfos.clear();
          break;
        }
        default: {
          changesCount = classInfos.size();
          op = ECrudOp.LIST;
          // changedId = null;
          classInfos.clear();
          break;
        }
      }
    }
    // add/update classes
    for( IDtoClassInfo inf : aUpdateClassInfos ) {
      IDtoClassInfo oldInf = classInfos.findByKey( inf.id() );
      if( !Objects.equals( inf, oldInf ) ) {
        if( oldInf != null ) {
          op = ECrudOp.EDIT;
        }
        else {
          op = ECrudOp.CREATE;
        }
        ++changesCount;
        changedId = inf.id();
        classInfos.put( inf );
      }
    }
    // inform frontend
    switch( changesCount ) {
      case 0: { // no changes, nothing to inform about
        // nop
        break;
      }
      case 1: { // single change causes single class event
        setChanged();
        GtMessage msg = IBaClassesMessages.makeMessage( op, changedId );
        owner().frontend().onBackendMessage( msg );
        break;
      }
      default: { // batch changes will fire ECrudOp.LIST event
        setChanged();
        GtMessage msg = IBaClassesMessages.makeMessage( ECrudOp.LIST, null );
        owner().frontend().onBackendMessage( msg );
        break;
      }
    }
  }

}
