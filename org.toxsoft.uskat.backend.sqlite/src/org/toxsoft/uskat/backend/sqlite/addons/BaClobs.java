package org.toxsoft.uskat.backend.sqlite.addons;

import java.sql.*;

import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.backend.sqlite.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;

/**
 * {@link IBaClobs} implementation for {@link SkBackendSqlite}.
 * <p>
 * Uses the table {@link #CLOBS_TABLE} with the structure:
 * <ul>
 * <li>ClobGwid string PRIMARY KEY - contains the CLOB GWID string returned by {@link Gwid#asString()};</li>
 * <li>ClobString text NOT NULL - contains the CLOB itself.</li>
 * </ul>
 *
 * @author hazard157
 */
public class BaClobs
    extends AbstractAddon
    implements IBaClobs {

  /**
   * TODO remove records for the removed Sk-Objects
   * <p>
   * FIXME generate events
   */

  private static final String CLOBS_TABLE = "SkClobs"; //$NON-NLS-1$

  /**
   * Constructor.
   *
   * @param aOwner {@link SkBackendSqlite} - the owner backend
   */
  public BaClobs( SkBackendSqlite aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_CLOBS );
  }

  // ------------------------------------------------------------------------------------
  // AbstractAddon
  //

  @Override
  protected void doInit() {
    // create table if not exists
    String sql = "CREATE TABLE IF NOT EXISTS " + CLOBS_TABLE + " (\n" //$NON-NLS-1$ //$NON-NLS-2$
        + "    ClobGwid string PRIMARY KEY,\n" //$NON-NLS-1$
        + "    ClobString text NOT NULL\n" //$NON-NLS-1$
        + ");"; //$NON-NLS-1$
    execSql( sql );
  }

  @Override
  public void doClose() {
    // nop
  }

  @Override
  public void clear() {
    execSql( "DELETE FROM " + CLOBS_TABLE + ";" ); //$NON-NLS-1$//$NON-NLS-2$
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  // private boolean hasClob( Gwid aGwid ) {
  // String sql = "SELECT ClobGwid FROM " + CLOBS_TABLE //$NON-NLS-1$
  // + " WHERE ClobGwid = '" + aGwid.asString() //$NON-NLS-1$
  // + "';"; //$NON-NLS-1$
  // try( ResultSet rs = execQuery( sql ) ) {
  // return rs.next();
  // }
  // catch( SQLException ex ) {
  // throw new SkSqlRtException( ex );
  // }
  // }

  // ------------------------------------------------------------------------------------
  // IBaClobs
  //

  @Override
  public String readClob( Gwid aGwid ) {
    String sql = "SELECT ClobString FROM " + CLOBS_TABLE //$NON-NLS-1$
        + " WHERE ClobGwid = '" + aGwid.asString() //$NON-NLS-1$
        + "';"; //$NON-NLS-1$
    try( ResultSet rs = execQuery( sql ) ) {
      while( rs.next() ) {
        return cseStr( rs.getString( 1 ) );
      }
      return null;
    }
    catch( SQLException ex ) {
      throw new SkSqlRtException( ex );
    }
  }

  @Override
  public void writeClob( Gwid aGwid, String aClob ) {
    TsNullArgumentRtException.checkNull( aClob );
    String gwidStr = aGwid.asString();
    StringBuilder sb = new StringBuilder( "INSERT OR REPLACE INTO " ); //$NON-NLS-1$
    sb.append( CLOBS_TABLE );
    sb.append( " (ClobGwid,ClobString) VALUES ('" ); //$NON-NLS-1$
    sb.append( gwidStr );
    sb.append( "','" ); //$NON-NLS-1$
    sb.append( escStr( aClob ) );
    sb.append( "');" ); //$NON-NLS-1$
    String sql = sb.toString();
    execSql( sql );
    GtMessage msg = IBaClobsMessages.makeMessage( aGwid );
    owner().frontend().onBackendMessage( msg );
  }

}
