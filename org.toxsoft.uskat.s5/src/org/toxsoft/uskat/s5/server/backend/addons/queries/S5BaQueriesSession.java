package org.toxsoft.uskat.s5.server.backend.addons.queries;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.addons.queries.IS5Resources.*;

import java.util.concurrent.*;

import javax.ejb.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.hqserv.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.s5.server.backend.addons.*;
import org.toxsoft.uskat.s5.server.backend.supports.queries.*;
import org.toxsoft.uskat.s5.server.sessions.init.*;
import org.toxsoft.uskat.s5.server.sessions.pas.*;

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
  protected void doAfterInit( S5SessionMessenger aMessenger, IS5SessionInitData aInitData,
      S5SessionInitResult aInitResult ) {
    S5BaQueriesData baData = new S5BaQueriesData();
    frontend().frontendData().setBackendAddonData( IBaQueries.ADDON_ID, baData );
  }

  @Override
  protected void doBeforeClose() {
    S5BaQueriesData baData =
        frontend().frontendData().findBackendAddonData( IBaQueries.ADDON_ID, S5BaQueriesData.class );
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
  // Реализация IS5BaQueriesSession
  //
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public String createQuery( IOptionSet aParams ) {
    TsNullArgumentRtException.checkNull( aParams );
    // Создание запроса
    String retValue = queriesSupport.createQuery( frontend(), aParams );
    // Сохранение измененной сессии в кластере сервера
    writeSessionData();
    return retValue;
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void prepareQuery( String aQueryId, IStringMap<IDtoQueryParam> aParams ) {
    TsNullArgumentRtException.checkNulls( aQueryId, aParams );
    // Подготовка запроса
    queriesSupport.prepareQuery( frontend(), aQueryId, aParams );
    // Сохранение измененной сессии в кластере сервера
    writeSessionData();
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void execQuery( String aQueryId, IQueryInterval aTimeInterval ) {
    TsNullArgumentRtException.checkNulls( aQueryId, aTimeInterval );
    // Выполнение запроса
    queriesSupport.execQuery( frontend(), aQueryId, aTimeInterval );
    // Сохранение измененной сессии в кластере сервера
    writeSessionData();
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void cancel( String aQueryId ) {
    TsNullArgumentRtException.checkNull( aQueryId );
    // Отмена запроса
    queriesSupport.cancel( frontend(), aQueryId, MSG_BY_USER );
    // Сохранение измененной сессии в кластере сервера
    writeSessionData();
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void close( String aQueryId ) {
    TsNullArgumentRtException.checkNull( aQueryId );
    // Завершение запроса
    queriesSupport.close( frontend(), aQueryId );
    // Сохранение измененной сессии в кластере сервера
    writeSessionData();
  }
}
