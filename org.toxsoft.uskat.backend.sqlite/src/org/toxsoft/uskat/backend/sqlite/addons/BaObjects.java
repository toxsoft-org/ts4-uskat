package org.toxsoft.uskat.backend.sqlite.addons;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.uskat.backend.sqlite.*;
import org.toxsoft.uskat.backend.sqlite.helpers.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;

/**
 * {@link IBaObjects} implementation for {@link SkBackendSqlite}.
 *
 * @author hazard157
 */
public class BaObjects
    extends AbstractAddon
    implements IBaObjects {

  private static final String TABLE_NAME_PREFIX = "ObjsOfClass_"; //$NON-NLS-1$

  /**
   * The map "table name" - "objects table".
   * <p>
   * The keys in the map is the table name made from the class ID with method {@link #makeTableName(String)}.
   * <p>
   * Each table is {@link StridablesKeepentTable} where key STRID is {@link ISkObject#strid()}.
   */
  private final IStringMapEdit<StridablesKeepentTable<IDtoObject>> classTables = new StringMap<>();

  /**
   * Constructor.
   *
   * @param aOwner {@link SkBackendSqlite} - the owner backend
   */
  public BaObjects( SkBackendSqlite aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_OBJECTS );
  }

  // ------------------------------------------------------------------------------------
  // AbstractAddon
  //

  @Override
  protected void doInit() {
    // nop
  }

  @Override
  public void doClose() {
    // nop
  }

  @Override
  public void clear() {
    for( StridablesKeepentTable<IDtoObject> t : classTables ) {
      t.clearTable();
    }
    classTables.clear();
  }

  private static String makeTableName( String aClassId ) {
    return TABLE_NAME_PREFIX + aClassId;
  }

  private static String unmakeClassId( String aTableName ) {
    return aTableName.substring( TABLE_NAME_PREFIX.length() );
  }

  private StridablesKeepentTable<IDtoObject> findTable( String aClassId ) {
    String tableName = makeTableName( aClassId );
    return classTables.findByKey( tableName );
  }

  // ------------------------------------------------------------------------------------
  // IBaObjects
  //

  @Override
  public IDtoObject findObject( Skid aSkid ) {
    StridablesKeepentTable<IDtoObject> table = findTable( aSkid.classId() );
    if( table != null ) {
      return table.find( aSkid.strid() );
    }
    return null;
  }

  @Override
  public IList<IDtoObject> readObjects( IStringList aClassIds ) {
    IListEdit<IDtoObject> ll = new ElemLinkedBundleList<>();
    for( String classId : aClassIds ) {
      StridablesKeepentTable<IDtoObject> table = findTable( classId );
      if( table != null ) {
        ll.addAll( table.readTable() );
      }
    }
    return ll;
  }

  @Override
  public IList<IDtoObject> readObjectsByIds( ISkidList aSkids ) {
    IListEdit<IDtoObject> ll = new ElemLinkedBundleList<>();
    for( String classId : aSkids.classIds() ) {
      StridablesKeepentTable<IDtoObject> table = findTable( classId );
      if( table != null ) {
        IStringList strids = aSkids.listStridsOfClass( classId );
        if( !strids.isEmpty() ) {
          ll.addAll( table.readByIds( strids ) );
        }
      }
    }
    return ll;
  }

  @Override
  public void writeObjects( ISkidList aRemoveSkids, IList<IDtoObject> aUpdateObjects ) {
    // clear all tables if requested
    if( aRemoveSkids == null ) {
      for( StridablesKeepentTable<?> table : classTables.values() ) {
        table.clearTable();
      }
    }
    // union of class IDs from both argument
    IStringListEdit affectedClassIds = new StringLinkedBundleList();
    // affectedClassIds.a

    for( StridablesKeepentTable<?> table : classTables.values() ) {

    }

    if( aRemoveSkids != null ) {
      for( String classId : aRemoveSkids.classIds() ) {
        StridablesKeepentTable<IDtoObject> table = findTable( classId );
        if( table != null ) {
          // TODO remove only specified objects
        }
      }
    }

    // FIXME

  }

}
