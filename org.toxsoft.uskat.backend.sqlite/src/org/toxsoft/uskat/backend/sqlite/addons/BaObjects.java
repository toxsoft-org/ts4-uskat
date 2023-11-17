package org.toxsoft.uskat.backend.sqlite.addons;

import java.sql.*;

import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.uskat.backend.sqlite.*;
import org.toxsoft.uskat.backend.sqlite.helpers.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * {@link IBaObjects} implementation for {@link SkBackendSqlite}.
 *
 * @author hazard157
 */
public class BaObjects
    extends AbstractAddon
    implements IBaObjects {

  /**
   * TODO remove table when class is deleted from the sysdescr<br>
   * TODO what to do with existing objects when class changes in the sysdescr?<br>
   */

  private static final String TABLE_NAME_PREFIX = "ObjsOfClass_"; //$NON-NLS-1$

  /**
   * The map "table name" - "objects table".
   * <p>
   * The keys in the map is the table name made from the class ID with method {@link #makeTableName(String)}.
   * <p>
   * Each table is {@link StridablesKeepentTable} where key STRID is {@link ISkObject#strid()}.
   */
  private final IStringMapEdit<StridablesKeepentTable<IDtoObject>> tablesByName = new StringMap<>();

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

  @SuppressWarnings( "resource" )
  @Override
  protected void doInit() {
    // find all tables of the Sk-objects and fill #classTables
    DatabaseMetaData md;
    try {
      md = sqlConn().getMetaData();
    }
    catch( SQLException ex ) {
      throw new SkSqlRtException( ex );
    }
    try( ResultSet rs = md.getTables( null, null, TABLE_NAME_PREFIX + '%', null ) ) {
      while( rs.next() ) {
        String tableName = rs.getString( "TABLE_NAME" ); //$NON-NLS-1$
        StridablesKeepentTable<IDtoObject> table = new StridablesKeepentTable<>( tableName, stmt(), DtoObject.KEEPER );
        tablesByName.put( tableName, table );
      }
    }
    catch( SQLException ex1 ) {
      throw new SkSqlRtException( ex1 );
    }
  }

  @Override
  public void doClose() {
    // nop
  }

  @Override
  public void clear() {
    for( StridablesKeepentTable<IDtoObject> t : tablesByName ) {
      t.clearTable();
    }
    tablesByName.clear();
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private static String makeTableName( String aClassId ) {
    return TABLE_NAME_PREFIX + aClassId;
  }

  // private static String unmakeClassId( String aTableName ) {
  // return aTableName.substring( TABLE_NAME_PREFIX.length() );
  // }

  private StridablesKeepentTable<IDtoObject> findTable( String aClassId ) {
    String tableName = makeTableName( aClassId );
    return tablesByName.findByKey( tableName );
  }

  @SuppressWarnings( "resource" )
  private StridablesKeepentTable<IDtoObject> ensureTableForClass( String aClassId ) {
    String tableName = makeTableName( aClassId );
    StridablesKeepentTable<IDtoObject> table = tablesByName.findByKey( tableName );
    if( table == null ) {
      table = new StridablesKeepentTable<>( tableName, stmt(), DtoObject.KEEPER );
      table.createTable();
      tablesByName.put( tableName, table );
    }
    return table;
  }

  private void internalRemoveSingleObject( Skid aSkid ) {
    StridablesKeepentTable<IDtoObject> table = tablesByName.findByKey( aSkid.classId() );
    if( table != null ) {
      table.removeRow( aSkid.strid() );
      GtMessage msg = IBaObjectsMessages.makeMessage( ECrudOp.REMOVE, aSkid );
      owner().frontend().onBackendMessage( msg );
    }
  }

  private void internalCreateOrUpdateSingleObject( IDtoObject aObject ) {
    StridablesKeepentTable<IDtoObject> table = ensureTableForClass( aObject.classId() );
    boolean wasCreated = table.insertOrUpdateRow( aObject );
    ECrudOp op = wasCreated ? ECrudOp.CREATE : ECrudOp.EDIT;
    GtMessage msg = IBaObjectsMessages.makeMessage( op, aObject.skid() );
    owner().frontend().onBackendMessage( msg );
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

  @SuppressWarnings( "resource" )
  @Override
  public void writeObjects( ISkidList aRemoveSkids, IList<IDtoObject> aUpdateObjects ) {
    // clear all tables if requested
    if( aRemoveSkids == null ) {
      for( StridablesKeepentTable<?> table : tablesByName.values() ) {
        table.clearTable();
      }
    }
    else {
      // special case - single object removal
      if( aRemoveSkids.size() == 1 && aUpdateObjects.isEmpty() ) {
        internalRemoveSingleObject( aRemoveSkids.first() );
        return;
      }
      // special case - single object create/update
      if( aRemoveSkids.isEmpty() && aUpdateObjects.size() == 1 ) {
        internalCreateOrUpdateSingleObject( aUpdateObjects.first() );
        return;
      }
    }
    // now we have massive CRUD operation so at the end we'll generate ECrudOp.LIST message

    /**
     * Prepare map "class ID" - "what to do with objects of class".
     * <p>
     * "what to do with objects of class" is the arguments to call StridablesKeepentTable.writeTable().
     */
    IStringMapEdit<Pair<IStringList, IListEdit<IDtoObject>>> byClassMap = new StringMap<>();
    // fill left of the pairs
    if( aRemoveSkids != null ) {
      for( String classId : aRemoveSkids.classIds() ) {
        Pair<IStringList, IListEdit<IDtoObject>> p = new Pair<>( aRemoveSkids.listStridsOfClass( classId ), null );
        byClassMap.put( classId, p );
      }
    }
    // fill right of the pairs
    for( IDtoObject obj : aUpdateObjects ) {
      String classId = obj.classId();
      Pair<IStringList, IListEdit<IDtoObject>> p = byClassMap.findByKey( classId );
      if( p == null ) {
        p = new Pair<>( null, null );
        byClassMap.put( classId, p );
      }
      IListEdit<IDtoObject> ll = p.right();
      if( ll == null ) {
        ll = new ElemLinkedBundleList<>();
        p = new Pair<>( p.left(), ll );
        byClassMap.put( classId, p );
      }
      ll.add( obj );
    }
    // write changes to DB
    for( String classId : byClassMap.keys() ) {
      Pair<IStringList, IListEdit<IDtoObject>> p = byClassMap.getByKey( classId );
      StridablesKeepentTable<IDtoObject> table = ensureTableForClass( classId );
      table.writeTable( p.left(), p.right() );
    }
    GtMessage msg = IBaObjectsMessages.makeMessage( ECrudOp.LIST, null );
    owner().frontend().onBackendMessage( msg );
  }
}
