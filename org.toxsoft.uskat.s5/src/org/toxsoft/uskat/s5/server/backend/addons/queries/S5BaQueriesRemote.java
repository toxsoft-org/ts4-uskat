package org.toxsoft.uskat.s5.server.backend.addons.queries;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.hqserv.IDtoQueryParam;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.api.IBaQueries;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendRemote;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonRemote;

/**
 * Remote {@link IBaQueries} implementation.
 *
 * @author mvk
 */
class S5BaQueriesRemote
    extends S5AbstractBackendAddonRemote<IS5BaQueriesSession>
    implements IBaQueries {

  /**
   * Данные конфигурации фронтенда для {@link IBaQueries}
   */
  private final S5BaQueriesData baData = new S5BaQueriesData();

  /**
   * Constructor.
   *
   * @param aOwner {@link IS5BackendRemote} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5BaQueriesRemote( IS5BackendRemote aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_QUERIES, IS5BaQueriesSession.class );
    // Установка конфигурации фронтенда
    frontend().frontendData().setBackendAddonData( IBaQueries.ADDON_ID, baData );
  }

  // ------------------------------------------------------------------------------------
  // BackendAddonBase
  //
  @Override
  public void onBackendMessage( GtMessage aMessage ) {
    // nop
  }

  @Override
  public void close() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // IBaQueries
  //
  @Override
  public String createQuery( IOptionSet aParams ) {
    TsNullArgumentRtException.checkNull( aParams );
    return session().createQuery( aParams );
  }

  @Override
  public void prepareQuery( String aQueryId, IStringMap<IDtoQueryParam> aParams ) {
    TsNullArgumentRtException.checkNulls( aQueryId, aParams );
    session().prepareQuery( aQueryId, aParams );
  }

  @Override
  public void execQuery( String aQueryId, IQueryInterval aTimeInterval ) {
    TsNullArgumentRtException.checkNulls( aQueryId, aTimeInterval );
    session().execQuery( aQueryId, aTimeInterval );
  }

  @Override
  public void cancel( String aQueryId ) {
    TsNullArgumentRtException.checkNull( aQueryId );
    session().cancel( aQueryId );
  }

  @Override
  public void close( String aQueryId ) {
    TsNullArgumentRtException.checkNull( aQueryId );
    session().close( aQueryId );
  }
}
