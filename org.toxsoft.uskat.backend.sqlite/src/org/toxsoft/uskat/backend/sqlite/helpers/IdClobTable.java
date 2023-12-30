package org.toxsoft.uskat.backend.sqlite.helpers;

import static org.toxsoft.uskat.backend.sqlite.addons.AbstractAddon.*;

import java.sql.*;

import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.backend.sqlite.*;

/**
 * Implements operations over STRID-STRING table storing large Strings (CLOBs).
 * <p>
 * The table has the following structure:
 * <ul>
 * <li>Id string PRIMARY KEY - contains the class ID;</li>
 * <li>Clob text NOT NULL - contains the text CLOB.</li>
 * </ul>
 *
 * @author hazard157
 */
public class IdClobTable {

  private final String    tableName;
  private final Statement stmt;

  /**
   * Constructor.
   * <p>
   * Note: this class does <b>not</b> holds the statement, so statement must be closed by the caller.
   *
   * @param aTableName String - the table name
   * @param aStmt {@link Statement} - the SQL-statement to use
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException table name is a blank name
   */
  public IdClobTable( String aTableName, Statement aStmt ) {
    TsNullArgumentRtException.checkNulls( aStmt );
    TsErrorUtils.checkNonBlank( aTableName );
    tableName = aTableName;
    stmt = aStmt;
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
        + "    Clob text NOT NULL\n" //$NON-NLS-1$
        + ");"; //$NON-NLS-1$
    execSql( sql );
  }

  /**
   * Determines if the table has a CLOB with the specified ID.
   *
   * @param aId String the CLOB ID
   * @return boolean - the row with the ID is in the table
   */
  public boolean hasClob( String aId ) {
    String sql = "SELECT Id FROM '" + tableName + "' WHERE Id = '" + aId + "';"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    try( ResultSet rs = execQuery( sql ) ) {
      return rs.next();
    }
    catch( SQLException ex ) {
      throw new SkSqlRtException( ex );
    }
  }

  /**
   * Finds the CLOB in the table.
   *
   * @param aId String - entity ID
   * @return &lt;T&gt; - found entity or <code>null</code>
   */
  public String find( String aId ) {
    String sql = "SELECT Clob FROM '" + tableName + "' WHERE Id = '" + aId + "';"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    try( ResultSet rs = execQuery( sql ) ) {
      if( rs.next() ) {
        String clob = cseStr( rs.getString( "Clob" ) ); //$NON-NLS-1$
        return clob;
      }
      return null;
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
   * Writes CLOB to the table.
   *
   * @param aId String - the key
   * @param aClob - the CLOB
   */
  public void writeTable( String aId, String aClob ) {
    StringBuilder sb = new StringBuilder( "INSERT OR REPLACE INTO '" ); //$NON-NLS-1$
    sb.append( tableName );
    sb.append( "' (Id,Clob) VALUES ('" ); //$NON-NLS-1$
    sb.append( aId );
    sb.append( "','" ); //$NON-NLS-1$
    sb.append( escStr( aClob ) );
    sb.append( "');" ); //$NON-NLS-1$
    String sql = sb.toString();
    execSql( sql );
  }

  /**
   * Deletes all records from the specified table.
   */
  public void clearTable() {
    execSql( "DELETE FROM '" + tableName + "';" ); //$NON-NLS-1$//$NON-NLS-2$
  }

  /**
   * Permanently deleteds the table and it's content.
   */
  public void dropTable() {
    execSql( "DROP TABLE IF EXISTS '" + tableName + "';" ); //$NON-NLS-1$//$NON-NLS-2$
  }

}
