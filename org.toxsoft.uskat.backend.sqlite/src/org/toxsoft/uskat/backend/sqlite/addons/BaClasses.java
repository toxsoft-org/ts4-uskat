package org.toxsoft.uskat.backend.sqlite.addons;

import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.backend.sqlite.*;
import org.toxsoft.uskat.backend.sqlite.helpers.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * {@link IBaClasses} implementation for {@link SkBackendSqlite}.
 *
 * @author hazard157
 */
public class BaClasses
    extends AbstractAddon
    implements IBaClasses {

  private static final String CLASSES_TABLE = "SkClasses"; //$NON-NLS-1$

  private StridablesKeepentTable<IDtoClassInfo> table = null;

  /**
   * Constructor.
   *
   * @param aOwner {@link SkBackendSqlite} - the owner backend
   */
  public BaClasses( SkBackendSqlite aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_CLASSES );
  }

  // ------------------------------------------------------------------------------------
  // AbstractAddon
  //

  @SuppressWarnings( "resource" )
  @Override
  protected void doInit() {
    table = new StridablesKeepentTable<>( CLASSES_TABLE, stmt(), DtoClassInfo.KEEPER );
    table.createTable();
  }

  @Override
  public void doClose() {
    // nop
  }

  @Override
  public void clear() {
    table.clearTable();
  }

  // ------------------------------------------------------------------------------------
  // IBaClasses
  //

  @Override
  public IStridablesList<IDtoClassInfo> readClassInfos() {
    return table.readTable();
  }

  @Override
  public void writeClassInfos( IStringList aRemoveClassIds, IStridablesList<IDtoClassInfo> aUpdateClassInfos ) {
    TsIllegalArgumentRtException.checkTrue( aUpdateClassInfos.hasKey( IGwHardConstants.GW_ROOT_CLASS_ID ) );
    // prepare for change event generation, here is some hack...
    ECrudOp op = ECrudOp.LIST;
    String classId = null;
    if( aRemoveClassIds != null ) {
      if( aRemoveClassIds.size() == 1 && aUpdateClassInfos.isEmpty() ) {
        op = ECrudOp.REMOVE;
        classId = aRemoveClassIds.first();
      }
      if( aUpdateClassInfos.size() == 1 && aRemoveClassIds.isEmpty() ) {
        op = table.find( aUpdateClassInfos.first().id() ) != null ? ECrudOp.EDIT : ECrudOp.CREATE;
        classId = aUpdateClassInfos.first().id();
      }
    }
    table.writeTable( aRemoveClassIds, aUpdateClassInfos );
    GtMessage msg = IBaClassesMessages.makeMessage( op, classId );
    owner().frontend().onBackendMessage( msg );
  }

}
