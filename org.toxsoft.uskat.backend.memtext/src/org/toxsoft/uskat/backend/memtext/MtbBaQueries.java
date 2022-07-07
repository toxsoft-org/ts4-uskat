package org.toxsoft.uskat.backend.memtext;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.hqserv.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;

/**
 * {@link IBaQueries} implementation.
 *
 * @author hazard157
 */
public class MtbBaQueries
    extends MtbAbstractAddon
    implements IBaQueries {

  /**
   * Constructor.
   *
   * @param aOwner {@link MtbAbstractBackend} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public MtbBaQueries( MtbAbstractBackend aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_LINKS );
  }

  // ------------------------------------------------------------------------------------
  // MtbAbstractAddon
  //

  @Override
  public void close() {
    clear();
  }

  @Override
  public void clear() {
    // TODO close all queries
  }

  // ------------------------------------------------------------------------------------
  // IBaQueries
  //

  @Override
  public String createQuery( IOptionSet aParams ) {
    // TODO реализовать MtbBaQueries.createQuery()
    throw new TsUnderDevelopmentRtException( "MtbBaQueries.createQuery()" );
  }

  @Override
  public void prepareQuery( String aQueryId, IStringMap<IDtoQueryParam> aParams ) {
    // TODO реализовать MtbBaQueries.prepareQuery()
    throw new TsUnderDevelopmentRtException( "MtbBaQueries.prepareQuery()" );
  }

  @Override
  public void execQuery( String aQueryId, IQueryInterval aTimeInterval ) {
    // TODO реализовать MtbBaQueries.execQuery()
    throw new TsUnderDevelopmentRtException( "MtbBaQueries.execQuery()" );
  }

  @Override
  public void cancel( String aQueryId ) {
    // TODO реализовать MtbBaQueries.cancel()
    throw new TsUnderDevelopmentRtException( "MtbBaQueries.cancel()" );
  }

  @Override
  public void close( String aQueryId ) {
    // TODO реализовать MtbBaQueries.close()
    throw new TsUnderDevelopmentRtException( "MtbBaQueries.close()" );
  }

}
