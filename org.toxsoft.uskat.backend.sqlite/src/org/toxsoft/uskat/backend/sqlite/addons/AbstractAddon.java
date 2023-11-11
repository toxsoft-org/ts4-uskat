package org.toxsoft.uskat.backend.sqlite.addons;

import java.sql.*;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.coll.basis.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.backend.sqlite.*;
import org.toxsoft.uskat.core.backend.api.*;

/**
 * This backend addons base implementation.
 *
 * @author hazard157
 */
public abstract class AbstractAddon
    extends BackendAddonBase<SkBackendSqlite>
    implements IInitializable, ITsClearable, ICloseable {

  private Statement stmt = null;

  protected AbstractAddon( SkBackendSqlite aOwner, IStridable aInfo ) {
    super( aOwner, aInfo );
  }

  // ------------------------------------------------------------------------------------
  // IInitializable
  //

  @SuppressWarnings( "resource" )
  @Override
  final public void initialize() {
    // init statement
    try {
      stmt = sqlConn().createStatement();
    }
    catch( SQLException ex ) {
      LoggerUtils.errorLogger().error( ex );
      throw new SkSqlRtException( ex );
    }
    //
    doInit();
  }

  // ------------------------------------------------------------------------------------
  // ICloseable
  //

  @Override
  final public void close() {
    if( stmt != null ) {
      try {
        stmt.close();
      }
      catch( SQLException ex ) {
        LoggerUtils.errorLogger().error( ex );
      }
    }
    //
    doClose();
  }

  // ------------------------------------------------------------------------------------
  // to implement
  //

  @Override
  public abstract void clear();

  protected abstract void doInit();

  protected abstract void doClose();

  // ------------------------------------------------------------------------------------
  // API for subclass
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
  public void execSql( String aSql ) {
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
  public ResultSet execQuery( String aSql ) {
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

  /**
   * Returns the addon private built-in statement.
   *
   * @return {@link Statement} - the statement
   * @throws TsIllegalStateRtException initialization was failed
   */
  public Statement stmt() {
    if( stmt == null ) {
      throw new TsIllegalStateRtException();
    }
    return stmt;
  }

  /**
   * Prepares string to be written using SQL-statement text.
   * <p>
   * Methods {@link #escStr(String)} and {@link #cseStr(String)} do opposite things.
   *
   * @param aJavaStr aStr - the text string
   * @return String - escaped text string
   */
  public static String escStr( String aJavaStr ) {
    return aJavaStr.replace( "'", "''" ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Makes Java String from read SQL-string.
   * <p>
   * Methods {@link #escStr(String)} and {@link #cseStr(String)} do opposite things.
   *
   * @param aSqlStr String SQL-text read from database
   * @return String - common Java string
   */
  public static String cseStr( String aSqlStr ) {
    return aSqlStr.replace( "''", "'" ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Returns the SQL-connection to the database.
   *
   * @return {@link Connection} - the SQL-connection
   * @throws TsIllegalStateRtException connection was not initialized
   */
  public Connection sqlConn() {
    return owner().sqlConn();
  }

}
