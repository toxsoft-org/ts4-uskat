package org.toxsoft.uskat.backend.memtext;

import java.util.*;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.strio.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
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
    DtoClassInfo.KEEPER.writeColl( aSw, classInfos, true );
  }

  @Override
  protected void doRead( IStrioReader aSr ) {
    StrioUtils.ensureKeywordHeader( aSr, KW_CLASS_INFOS );
    DtoClassInfo.KEEPER.readColl( aSr, classInfos );
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
    // remove classes
    if( aRemoveClassIds != null ) {
      for( String classId : aRemoveClassIds ) {
        if( classInfos.removeById( classId ) != null ) {
          setChanged();
        }
      }
    }
    else {
      if( !classInfos.isEmpty() ) {
        classInfos.clear();
        setChanged();
      }
    }
    // add/update classes
    for( IDtoClassInfo inf : aUpdateClassInfos ) {
      IDtoClassInfo oldInf = classInfos.findByKey( inf.id() );
      if( !Objects.equals( inf, oldInf ) ) {
        classInfos.put( inf );
        setChanged();
      }
    }
  }

}
