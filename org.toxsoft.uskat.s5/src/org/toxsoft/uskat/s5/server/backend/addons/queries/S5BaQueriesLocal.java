package org.toxsoft.uskat.s5.server.backend.addons.queries;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
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
   * Данные конфигурации фронтенда для {@link IBaQueries}
   */
  private final S5BaQueriesData baData = new S5BaQueriesData();

  /**
   * Constructor.
   *
   * @param aOwner {@link IS5BackendLocal} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5BaQueriesLocal( IS5BackendLocal aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_QUERIES );
    queriesSupport =
        aOwner.backendSingleton().get( S5BackendQueriesSingleton.BACKEND_QUERIES_ID, IS5BackendQueriesSingleton.class );
    // Установка конфигурации фронтенда
    frontend().frontendData().setBackendAddonData( IBaQueries.ADDON_ID, baData );
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
    // Список идентификаторов открытых запросов
    IStringList queryIds;
    synchronized (baData) {
      queryIds = new StringArrayList( baData.openQueries.keys() );
    }
    // Завершение работы открытых запросов
    for( String queryId : queryIds ) {
      queriesSupport.close( frontend(), queryId );
    }
  }

  // ------------------------------------------------------------------------------------
  // IBaQueries
  //
  @Override
  public String createQuery( IOptionSet aParams ) {
    TsNullArgumentRtException.checkNull( aParams );
    return queriesSupport.createQuery( frontend(), aParams );
  }

  @Override
  public void prepareQuery( String aQueryId, IStringMap<IDtoQueryParam> aParams ) {
    TsNullArgumentRtException.checkNulls( aQueryId, aParams );
    queriesSupport.prepareQuery( frontend(), aQueryId, aParams );
  }

  @Override
  public void execQuery( String aQueryId, IQueryInterval aTimeInterval ) {
    TsNullArgumentRtException.checkNulls( aQueryId, aTimeInterval );
    queriesSupport.execQuery( frontend(), aQueryId, aTimeInterval );
  }

  @Override
  public void cancel( String aQueryId ) {
    TsNullArgumentRtException.checkNull( aQueryId );
    queriesSupport.cancel( frontend(), aQueryId );
  }

  @Override
  public void close( String aQueryId ) {
    TsNullArgumentRtException.checkNull( aQueryId );
    queriesSupport.close( frontend(), aQueryId );
  }
}
