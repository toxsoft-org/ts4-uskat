package org.toxsoft.uskat.s5.server.backend.addons.clobs;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;

import java.util.concurrent.*;

import javax.ejb.*;

import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.s5.server.backend.addons.*;
import org.toxsoft.uskat.s5.server.backend.supports.clobs.*;
import org.toxsoft.uskat.s5.server.sessions.init.*;
import org.toxsoft.uskat.s5.server.sessions.pas.*;

/**
 * Реализация сессии расширения бекенда {@link IS5BaClobsSession}.
 *
 * @author mvk
 */
@Stateful
@StatefulTimeout( value = STATEFULL_TIMEOUT, unit = TimeUnit.MILLISECONDS )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
public class S5BaClobsSession
    extends S5AbstractBackendAddonSession
    implements IS5BaClobsSession, IS5BackendAddonSessionControl {

  private static final long serialVersionUID = 157157L;

  /**
   * Поддержка сервера запросов обработки clobs
   */
  @EJB
  private IS5BackendClobsSingleton clobsSupport;

  /**
   * Пустой конструктор.
   */
  public S5BaClobsSession() {
    super( ISkBackendHardConstant.BAINF_CLOBS );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendAddonSession
  //
  @Override
  protected Class<? extends IS5BaClobsSession> doGetSessionView() {
    return IS5BaClobsSession.class;
  }

  @Override
  protected void doAfterInit( S5SessionMessenger aMessenger, IS5SessionInitData aInitData,
      S5SessionInitResult aInitResult ) {
    S5BaClobsData baData = new S5BaClobsData();
    frontend().frontendData().setBackendAddonData( IBaClobs.ADDON_ID, baData );
  }

  @Override
  protected void doBeforeClose() {
    // S5BaClobsData baData = frontend().frontendData().findBackendAddonData( IBaQueries.ADDON_ID, S5BaClobsData.class
    // );
    // // Список идентификаторов открытых запросов
    // IStringList queryIds;
    // synchronized (baData) {
    // queryIds = new StringArrayList( baData.openQueries.keys() );
    // }
    // // Завершение работы открытых запросов
    // for( String queryId : queryIds ) {
    // clobsSupport.close( frontend(), queryId );
    // }
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5BaClobsSession
  //
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public String readClob( Gwid aGwid ) {
    TsNullArgumentRtException.checkNull( aGwid );
    return clobsSupport.readClob( aGwid );
  }

  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public void writeClob( Gwid aGwid, String aClob ) {
    TsNullArgumentRtException.checkNulls( aGwid, aClob );
    clobsSupport.writeClob( aGwid, aClob );
  }

  // @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  // @Override
  // public String createQuery( IOptionSet aParams ) {
  // TsNullArgumentRtException.checkNull( aParams );
  // // Создание запроса
  // String retValue = clobsSupport.createQuery( frontend(), aParams );
  // // Сохранение измененной сессии в кластере сервера
  // writeSessionData();
  // return retValue;
  // }

}
