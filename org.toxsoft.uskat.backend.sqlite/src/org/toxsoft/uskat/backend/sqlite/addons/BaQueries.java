package org.toxsoft.uskat.backend.sqlite.addons;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.uskat.backend.sqlite.*;
import org.toxsoft.uskat.core.api.hqserv.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;

/**
 * {@link IBaQueries} implementation for {@link SkBackendSqlite}.
 *
 * @author hazard157
 */
public class BaQueries
    extends AbstractAddon
    implements IBaQueries {

  /**
   * Constructor.
   *
   * @param aOwner {@link SkBackendSqlite} - the owner backend
   */
  public BaQueries( SkBackendSqlite aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_QUERIES );
    // TODO Auto-generated constructor stub
  }

  // ------------------------------------------------------------------------------------
  // AbstractAddon
  //

  @Override
  protected void doInit() {
    // TODO doInit()
  }

  @Override
  public void doClose() {
    // nop
  }

  @Override
  public void clear() {
    // TODO clear()
  }

  // ------------------------------------------------------------------------------------
  // IBaQueries
  //

  @Override
  public String createQuery( IOptionSet aParams ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void prepareQuery( String aQueryId, IStringMap<IDtoQueryParam> aParams ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void execQuery( String aQueryId, IQueryInterval aTimeInterval ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void cancel( String aQueryId ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void close( String aQueryId ) {
    // TODO Auto-generated method stub

  }

}
