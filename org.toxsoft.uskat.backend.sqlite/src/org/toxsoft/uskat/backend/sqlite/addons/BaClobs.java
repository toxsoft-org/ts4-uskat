package org.toxsoft.uskat.backend.sqlite.addons;

import java.sql.*;

import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.uskat.backend.sqlite.*;
import org.toxsoft.uskat.backend.sqlite.helpers.*;
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
   */

  private static final String CLOBS_TABLE = "SkClobs"; //$NON-NLS-1$

  private IdClobTable table = null;

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

  @SuppressWarnings( "resource" )
  @Override
  protected void doInit() {
    DatabaseMetaData md;
    try {
      md = sqlConn().getMetaData();
    }
    catch( SQLException ex ) {
      throw new SkSqlRtException( ex );
    }
    try( ResultSet rs = md.getTables( null, null, CLOBS_TABLE, null ) ) {
      while( rs.next() ) {
        table = new IdClobTable( CLOBS_TABLE, stmt() );
      }
    }
    catch( SQLException ex1 ) {
      throw new SkSqlRtException( ex1 );
    }
    if( table == null ) {
      table = new IdClobTable( CLOBS_TABLE, stmt() );
      table.createTable();
    }
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
    return table.find( aGwid.asString() );
  }

  @Override
  public void writeClob( Gwid aGwid, String aClob ) {
    table.writeTable( aClob, aClob );
    GtMessage msg = BaMsgClobsChanged.BUILDER.makeMessage( aGwid );
    owner().frontend().onBackendMessage( msg );
  }

}
