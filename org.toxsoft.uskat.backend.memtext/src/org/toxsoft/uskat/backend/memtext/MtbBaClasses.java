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
    // nop
  }

  @Override
  public void clear() {
    classInfos.clear();
  }

  @Override
  protected void doWrite( IStrioWriter aSw ) {
    StrioUtils.writeKeywordHeader( aSw, KW_CLASS_INFOS, true );
    /**
     * We'll remove classes defined by the core services.
     */
    IStridablesListEdit<IDtoClassInfo> toSave = new StridablesList<>();
    for( IDtoClassInfo cinf : classInfos ) {
      boolean isSrcCode = OPDEF_SK_IS_SOURCE_CODE_DEFINED_CLASS.getValue( cinf.params() ).asBool();
      boolean isCoreClass = OPDEF_SK_IS_SOURCE_USKAT_CORE_CLASS.getValue( cinf.params() ).asBool();
      if( !isSrcCode && !isCoreClass ) {
        toSave.add( cinf );
      }
    }
    DtoClassInfo.KEEPER.writeColl( aSw, toSave, true );
  }

  @Override
  protected void doRead( IStrioReader aSr ) {
    StrioUtils.ensureKeywordHeader( aSr, KW_CLASS_INFOS );
    classInfos.clear();
    DtoClassInfo.KEEPER.readColl( aSr, classInfos );
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

  @Override
  public void writeClassInfos( IStringList aRemoveClassIds, IStridablesList<IDtoClassInfo> aUpdateClassInfos ) {
    internalCheck();
    TsIllegalArgumentRtException.checkTrue( aUpdateClassInfos.hasKey( IGwHardConstants.GW_ROOT_CLASS_ID ) );
    // prepare for frontend message
    IStringListEdit removedClassIds = new StringLinkedBundleList();
    IStringListEdit createdClassIds = new StringLinkedBundleList();
    IStringListEdit editedClassIds = new StringLinkedBundleList();
    // remove classes
    if( aRemoveClassIds != null ) {
      for( String classId : aRemoveClassIds ) {
        if( classInfos.removeById( classId ) != null ) {
          removedClassIds.add( classId );
          setChanged();
        }
      }
    }
    else {
      if( !classInfos.isEmpty() ) {
        removedClassIds.addAll( classInfos.ids() );
        classInfos.clear();
        setChanged();
      }
    }
    // add/update classes
    for( IDtoClassInfo inf : aUpdateClassInfos ) {
      IDtoClassInfo oldInf = classInfos.findByKey( inf.id() );
      if( !Objects.equals( inf, oldInf ) ) {
        if( oldInf != null ) {
          if( !oldInf.equals( inf ) ) {
            editedClassIds.add( inf.id() );
          }
        }
        else {
          createdClassIds.add( inf.id() );
        }
        classInfos.put( inf );
        setChanged();
      }
    }
    // inform frontend
    int totalCount = removedClassIds.size() + editedClassIds.size() + createdClassIds.size();
    switch( totalCount ) {
      case 0: { // no changes, nothing to inform about
        // nop
        break;
      }
      case 1: { // single change causes single class event
        ECrudOp op;
        String classId;
        if( !createdClassIds.isEmpty() ) {
          op = ECrudOp.CREATE;
          classId = createdClassIds.first();
        }
        else {
          if( !editedClassIds.isEmpty() ) {
            op = ECrudOp.EDIT;
            classId = editedClassIds.first();
          }
          else {
            op = ECrudOp.REMOVE;
            classId = removedClassIds.first();
          }
        }
        GtMessage msg = IBaClassesMessages.makeMessage( op, classId );
        owner().frontend().onBackendMessage( msg );
        break;
      }
      default: { // batch changes will fir ECrudOp.LIST event
        GtMessage msg = IBaClassesMessages.makeMessage( ECrudOp.LIST, null );
        owner().frontend().onBackendMessage( msg );
        break;
      }
    }
  }

}
