package org.toxsoft.uskat.s5.server.backend.addons.queries;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;

import java.util.concurrent.TimeUnit;

import javax.ejb.*;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.hqserv.IDtoQueryParam;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.api.IBaQueries;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonSessionControl;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonSession;
import org.toxsoft.uskat.s5.server.backend.supports.queries.IS5BackendQueriesSingleton;
import org.toxsoft.uskat.s5.server.sessions.init.IS5SessionInitData;
import org.toxsoft.uskat.s5.server.sessions.init.S5SessionInitResult;
import org.toxsoft.uskat.s5.server.sessions.pas.S5SessionCallbackWriter;

/**
 * Реализация сессии расширения бекенда {@link IS5BaQueriesSession}.
 *
 * @author mvk
 */
@Stateful
@StatefulTimeout( value = STATEFULL_TIMEOUT, unit = TimeUnit.MILLISECONDS )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
@SuppressWarnings( "unused" )
public class S5BaQueriesSession
    extends S5AbstractBackendAddonSession
    implements IS5BaQueriesSession, IS5BackendAddonSessionControl {

  private static final long serialVersionUID = 157157L;

  /**
   * Поддержка сервера запросов хранимых данных
   */
  @EJB
  private IS5BackendQueriesSingleton queriesSupport;

  /**
   * Пустой конструктор.
   */
  public S5BaQueriesSession() {
    super( ISkBackendHardConstant.BAINF_QUERIES );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendAddonSession
  //
  @Override
  protected Class<? extends IS5BaQueriesSession> doGetSessionView() {
    return IS5BaQueriesSession.class;
  }

  @Override
  protected void doAfterInit( S5SessionCallbackWriter aCallbackWriter, IS5SessionInitData aInitData,
      S5SessionInitResult aInitResult ) {
    S5BaQueriesData baData = new S5BaQueriesData();
    frontend().frontendData().setBackendAddonData( IBaQueries.ADDON_ID, baData );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5BaQueriesSession
  //
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public String createQuery( IOptionSet aParams ) {
    TsNullArgumentRtException.checkNull( aParams );
    return queriesSupport.createQuery( aParams );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void prepareQuery( String aQueryId, IStringMap<IDtoQueryParam> aParams ) {
    TsNullArgumentRtException.checkNulls( aQueryId, aParams );
    queriesSupport.prepareQuery( aQueryId, aParams );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void execQuery( String aQueryId, IQueryInterval aTimeInterval ) {
    TsNullArgumentRtException.checkNulls( aQueryId, aTimeInterval );
    queriesSupport.execQuery( aQueryId, aTimeInterval );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void cancel( String aQueryId ) {
    TsNullArgumentRtException.checkNull( aQueryId );
    queriesSupport.cancel( aQueryId );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void close( String aQueryId ) {
    TsNullArgumentRtException.checkNull( aQueryId );
    queriesSupport.close( aQueryId );
  }
}
