package org.toxsoft.uskat.backend.sqlite.addons;

import java.sql.*;

import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.backend.sqlite.*;
import org.toxsoft.uskat.backend.sqlite.helpers.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;

/**
 * {@link IBaGwidDb} implementation for {@link SkBackendSqlite}.
 *
 * @author hazard157
 */
public class BaGwidDb
    extends AbstractAddon
    implements IBaGwidDb {

  private static final String SECTION_FILE_NAME_PREFIX = "GwidDbSection_"; //$NON-NLS-1$

  /**
   * The map "table name" - "CLOBs table".
   * <p>
   * The keys in the map is the table name made from the section ID with method {@link #makeTableName(IdChain)}.
   * <p>
   * Each table is {@link IdClobTable} where key is {@link IdChain#canonicalString()}.
   */
  private final IStringMapEdit<IdClobTable> tablesByName = new StringMap<>();

  /**
   * Constructor.
   *
   * @param aOwner {@link SkBackendSqlite} - the owner backend
   */
  public BaGwidDb( SkBackendSqlite aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_GWID_DB );
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
    try( ResultSet rs = md.getTables( null, null, SECTION_FILE_NAME_PREFIX + '%', null ) ) {
      while( rs.next() ) {
        String tableName = rs.getString( "TABLE_NAME" ); //$NON-NLS-1$
        IdClobTable table = new IdClobTable( tableName, stmt() );
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
    for( IdClobTable table : tablesByName ) {
      table.clearTable();
    }
    tablesByName.clear();
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private static IdChain unmakeTableName( String aId ) {
    TsInternalErrorRtException.checkFalse( aId.startsWith( SECTION_FILE_NAME_PREFIX ) );
    String canonicalString = aId.substring( SECTION_FILE_NAME_PREFIX.length() );
    return IdChain.of( canonicalString );
  }

  private static String makeTableName( IdChain aSectionId ) {
    return SECTION_FILE_NAME_PREFIX + aSectionId.canonicalString();
  }

  @SuppressWarnings( "resource" )
  private IdClobTable ensureTableForSection( IdChain aSeciotnId ) {
    String tableName = makeTableName( aSeciotnId );
    IdClobTable table = tablesByName.findByKey( tableName );
    if( table == null ) {
      table = new IdClobTable( tableName, stmt() );
      table.createTable();
      tablesByName.put( tableName, table );
    }
    return table;
  }

  // ------------------------------------------------------------------------------------
  // IBaGwidDb
  //

  @Override
  public IList<IdChain> listSectionIds() {
    IListEdit<IdChain> ll = new ElemArrayList<>();
    for( String s : tablesByName.keys() ) {
      IdChain idChain = unmakeTableName( s );
      ll.add( idChain );
    }
    return ll;
  }

  @Override
  public IList<Gwid> listKeys( IdChain aSectionId ) {
    String tableName = makeTableName( aSectionId );
    if( !tablesByName.hasKey( tableName ) ) {
      return IList.EMPTY;
    }
    String sql = "SELECT Id FROM '" + tableName + "';"; //$NON-NLS-1$//$NON-NLS-2$
    IListEdit<Gwid> ll = new ElemLinkedBundleList<>();
    try( ResultSet rs = execQuery( sql ) ) {
      while( rs.next() ) {
        String s = rs.getString( "Id" ); //$NON-NLS-1$
        Gwid gwid = Gwid.of( s );
        ll.add( gwid );
      }
      return ll;
    }
    catch( @SuppressWarnings( "unused" ) SQLException ex ) {
      throw new SkSqlRtException( sql );
    }
  }

  @Override
  public String readValue( IdChain aSectionId, Gwid aKey ) {
    String tableName = makeTableName( aSectionId );
    IdClobTable table = tablesByName.findByKey( tableName );
    if( table == null ) {
      return null;
    }
    return table.find( aKey.asString() );
  }

  @Override
  public void writeValue( IdChain aSectionId, Gwid aKey, String aValue ) {
    IdClobTable table = ensureTableForSection( aSectionId );
    table.writeTable( aKey.asString(), aValue );
  }

  @Override
  public void removeValue( IdChain aSectionId, Gwid aKey ) {
    String tableName = makeTableName( aSectionId );
    IdClobTable table = tablesByName.findByKey( tableName );
    if( table != null ) {
      table.removeRow( aKey.asString() );
    }
  }

  @Override
  public void removeSection( IdChain aSectionId ) {
    String tableName = makeTableName( aSectionId );
    IdClobTable table = tablesByName.findByKey( tableName );
    if( table != null ) {
      table.dropTable();
      tablesByName.removeByKey( tableName );
    }
  }

}
