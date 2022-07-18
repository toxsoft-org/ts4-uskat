package org.toxsoft.uskat.s5.server.backend.addons.queries;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.hqserv.IDtoQueryParam;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.api.IBaQueries;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendLocal;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonLocal;
import org.toxsoft.uskat.s5.server.backend.supports.queries.IS5BackendQueriesSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.queries.impl.S5BackendQueriesSingleton;

/**
 * Local {@link IBaQueries} implementation.
 *
 * @author mvk
 */
class S5BaQueriesLocal
    extends S5AbstractBackendAddonLocal
    implements IBaQueries {

  /**
   * Поддержка сервера обработки запросов хранимых данных
   */
  private final IS5BackendQueriesSingleton queriesSupport;

  /**
   * Constructor.
   *
   * @param aOwner {@link IS5BackendLocal} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5BaQueriesLocal( IS5BackendLocal aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_QUERIES );
    // Синглтон поддержки чтения/записи системного описания
    queriesSupport =
        aOwner.backendSingleton().get( S5BackendQueriesSingleton.BACKEND_QUERIES_ID, IS5BackendQueriesSingleton.class );
  }

  // ------------------------------------------------------------------------------------
  // BackendAddonBase
  //
  @Override
  public void onBackendMessage( GtMessage aMessage ) {
    // if( aMessage.messageId().equals( S5BaAfterInitMessages.MSG_ID ) ) {
    // }
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
    return queriesSupport.createQuery( aParams );
  }

  @Override
  public void prepareQuery( String aQueryId, IStringMap<IDtoQueryParam> aParams ) {
    TsNullArgumentRtException.checkNulls( aQueryId, aParams );
    queriesSupport.prepareQuery( aQueryId, aParams );
  }

  @Override
  public void execQuery( String aQueryId, IQueryInterval aTimeInterval ) {
    TsNullArgumentRtException.checkNulls( aQueryId, aTimeInterval );
    queriesSupport.execQuery( aQueryId, aTimeInterval );
  }

  @Override
  public void cancel( String aQueryId ) {
    TsNullArgumentRtException.checkNull( aQueryId );
    queriesSupport.cancel( aQueryId );
  }

  @Override
  public void close( String aQueryId ) {
    TsNullArgumentRtException.checkNull( aQueryId );
    queriesSupport.close( aQueryId );
  }
}
