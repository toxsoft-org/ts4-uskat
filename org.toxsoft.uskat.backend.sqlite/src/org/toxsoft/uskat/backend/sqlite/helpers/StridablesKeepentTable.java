package org.toxsoft.uskat.backend.sqlite.helpers;

import static org.toxsoft.uskat.backend.sqlite.addons.AbstractAddon.*;

import java.sql.*;

import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.backend.sqlite.*;

/**
 * Implements operations over STRID-KEEPENT table storing {@link IStridable} entities.
 * <p>
 * The table has the following structure:
 * <ul>
 * <li>Id string PRIMARY KEY - contains the class ID;</li>
 * <li>KeepEnt text NOT NULL - contains the kept textual representation with the method
 * {@link IEntityKeeper#ent2str(Object)}.</li>
 * </ul>
 *
 * @author hazard157
 * @param <T> - type of kept entities
 */
public class StridablesKeepentTable<T extends IStridable> {

  private final String           tableName;
  private final Statement        stmt;
  private final IEntityKeeper<T> keeper;

  /**
   * Constructor.
   * <p>
   * Note: this class does <b>not</b> holds the statement, so statement must be closed by the caller.
   *
   * @param aTableName String - the table name
   * @param aStmt {@link Statement} - the SQL-statement to use
   * @param aKeeper {@link IEntityKeeper}&lt;T&gt; - the keeper
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException table name is a blank name
   */
  public StridablesKeepentTable( String aTableName, Statement aStmt, IEntityKeeper<T> aKeeper ) {
    TsNullArgumentRtException.checkNulls( aStmt, aKeeper );
    TsErrorUtils.checkNonBlank( aTableName );
    tableName = aTableName;
    stmt = aStmt;
    keeper = aKeeper;
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  /**
   * Executes the specified SQL-statement text without return value.
   *
   * @param aSql String the SQL-statement text
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException aSql is a blank string
   * @throws TsIllegalStateRtException initialization was failed
   * @throws SkSqlRtException query execution failed
   */
  private void execSql( String aSql ) {
    TsErrorUtils.checkNonBlank( aSql );
    if( stmt == null ) {
      throw new TsIllegalStateRtException();
    }
    try {
      stmt.execute( aSql );
    }
    catch( SQLException ex ) {
      throw new SkSqlRtException( ex, aSql );
    }
  }

  /**
   * Executes the specified SQL-statement text returning the {@link ResultSet}.
   *
   * @param aSql String the SQL-statement text
   * @return {@link ResultSet} - the query result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException aSql is a blank string
   * @throws TsIllegalStateRtException initialization was failed
   * @throws SkSqlRtException query execution failed
   */
  private ResultSet execQuery( String aSql ) {
    TsErrorUtils.checkNonBlank( aSql );
    if( stmt == null ) {
      throw new TsIllegalStateRtException();
    }
    try {
      return stmt.executeQuery( aSql );
    }
    catch( SQLException ex ) {
      throw new SkSqlRtException( ex, aSql );
    }
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Creates table (if not exists) to store the pair STRID - KEEPER created text.
   */
  public void createTable() {
    String sql = "CREATE TABLE IF NOT EXISTS '" + tableName + "' (\n" //$NON-NLS-1$ //$NON-NLS-2$
        + "    Id string PRIMARY KEY,\n" //$NON-NLS-1$
        + "    KeepEnt text NOT NULL\n" //$NON-NLS-1$
        + ");"; //$NON-NLS-1$
    execSql( sql );
  }

  /**
   * Finds entity in the table.
   *
   * @param aId String - entity ID
   * @return &lt;T&gt; - found entity or <code>null</code>
   */
  public T find( String aId ) {
    String sql = "SELECT KeepEnt FROM '" + tableName + "' WHERE Id = '" + aId + "';"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    try( ResultSet rs = execQuery( sql ) ) {
      if( rs.next() ) {
        String keepent = cseStr( rs.getString( "KeepEnt" ) ); //$NON-NLS-1$
        T found = keeper.str2ent( keepent );
        return found;
      }
      return null;
    }
    catch( SQLException ex ) {
      throw new SkSqlRtException( ex );
    }
  }

  /**
   * Reads whole table.
   *
   * @return {@link IStridablesList}&lt;T&gt; - read entities
   */
  public IStridablesList<T> readTable() {
    String sql = "SELECT * FROM '" + tableName + "';"; //$NON-NLS-1$//$NON-NLS-2$
    try( ResultSet rs = execQuery( sql ) ) {
      IStridablesListEdit<T> ll = new StridablesList<>();
      while( rs.next() ) {
        String keepent = cseStr( rs.getString( "KeepEnt" ) ); //$NON-NLS-1$
        T entity = keeper.str2ent( keepent );
        ll.add( entity );
      }
      return ll;
    }
    catch( SQLException ex ) {
      throw new SkSqlRtException( ex );
    }
  }

  /**
   * Returns entities by the IDs.
   * <p>
   * Unexisting IDs are silently ignored.
   *
   * @param aIds {@link IStringList} - the list of IDs
   * @return {@link IStridablesList}&lt;T&gt; - read entities
   */
  public IStridablesList<T> readByIds( IStringList aIds ) {
    StringBuilder sb = new StringBuilder( "SELECT KeepEnt FROM '" ); //$NON-NLS-1$
    sb.append( tableName );
    sb.append( "' WHERE id IN ( " ); //$NON-NLS-1$
    for( String id : aIds ) {
      sb.append( "'" ); //$NON-NLS-1$
      sb.append( id );
      sb.append( "'" ); //$NON-NLS-1$
      if( id != aIds.last() ) {
        sb.append( "," ); //$NON-NLS-1$
      }
    }
    sb.append( " );" ); //$NON-NLS-1$
    String sql = sb.toString();
    try( ResultSet rs = execQuery( sql ) ) {
      IStridablesListEdit<T> ll = new StridablesList<>();
      while( rs.next() ) {
        String keepent = cseStr( rs.getString( "KeepEnt" ) ); //$NON-NLS-1$
        T entity = keeper.str2ent( keepent );
        ll.add( entity );
      }
      return ll;
    }
    catch( SQLException ex ) {
      throw new SkSqlRtException( ex );
    }
  }

  /**
   * Removes one row from the table.
   *
   * @param aId String - an ID of the entity to remove
   */
  public void removeRow( String aId ) {
    execSql( "DELETE FROM '" + tableName + "' WHERE Id = '" + aId + "';" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  /**
   * Writes (INSERT or REPLACE) single entity to the table.
   *
   * @param aEntity &lt;T&gt; - the entity to write
   * @return boolean - determines whether the entity was created rather than edited<br>
   *         <b>true</b> - the entity did not exist and it was created;<br>
   *         <b>false</b> - existing entity was updated.
   */
  public boolean insertOrUpdateRow( T aEntity ) {
    boolean wasCreated = find( aEntity.id() ) == null;
    String keepent = escStr( keeper.ent2str( aEntity ) );
    execSql( "INSERT OR REPLACE INTO '" + tableName + //$NON-NLS-1$
        "' (Id,KeepEnt) VALUES ('" + aEntity.id() + "','" + keepent + "');" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    return wasCreated;
  }

  /**
   * Writes to the table.
   * <p>
   * Warning: if argument <code>aToRemove</code> is <code>null</code>, table will be cleared and then new data inserted!
   *
   * @param aToRemove {@link IStringList} - ID of entities to remove or <code>null</code> first to remove all items
   * @param aToDefine {@link IStringList} - entities will be inserted (or replaced) in the table
   */
  public void writeTable( IStringList aToRemove, IList<T> aToDefine ) {
    TsNullArgumentRtException.checkNull( aToDefine );
    // remove
    if( aToRemove == null ) {
      execSql( "DELETE FROM '" + tableName + "';" ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    else {
      if( !aToRemove.isEmpty() ) {
        StringBuilder sb = new StringBuilder( "DELETE FROM " ); //$NON-NLS-1$
        sb.append( tableName );
        sb.append( " WHERE Id IN ( " ); //$NON-NLS-1$
        for( int i = 0, count = aToRemove.size(); i < count; i++ ) {
          String s = aToRemove.get( i );
          sb.append( "'" ); //$NON-NLS-1$
          sb.append( s );
          sb.append( "'" ); //$NON-NLS-1$
          if( i < count - 1 ) {
            sb.append( "," ); //$NON-NLS-1$
          }
        }
        sb.append( " );" ); //$NON-NLS-1$
        String sql = sb.toString();
        execSql( sql );
      }
    }
    // define
    if( aToDefine.isEmpty() ) {
      return;
    }
    StringBuilder sb = new StringBuilder( "INSERT OR REPLACE INTO '" ); //$NON-NLS-1$
    sb.append( tableName );
    sb.append( "' (Id,KeepEnt) VALUES " ); //$NON-NLS-1$
    for( int i = 0, count = aToDefine.size(); i < count; i++ ) {
      sb.append( "('" ); //$NON-NLS-1$
      T entity = aToDefine.get( i );
      String id = entity.id();
      String keepent = escStr( keeper.ent2str( entity ) );
      sb.append( id );
      sb.append( "','" ); //$NON-NLS-1$
      sb.append( keepent );
      sb.append( "')" ); //$NON-NLS-1$
      if( i < count - 1 ) {
        sb.append( "," ); //$NON-NLS-1$
      }
    }
    sb.append( ";" ); //$NON-NLS-1$
    String sql = sb.toString();
    execSql( sql );
  }

  /**
   * Deletes all records from the specified table.
   */
  public void clearTable() {
    execSql( "DELETE FROM '" + tableName + "';" ); //$NON-NLS-1$//$NON-NLS-2$
  }

}
