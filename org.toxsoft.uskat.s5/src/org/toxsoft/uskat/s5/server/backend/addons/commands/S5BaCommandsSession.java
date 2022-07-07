package org.toxsoft.uskat.s5.server.backend.addons.commands;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;

import java.util.concurrent.TimeUnit;

import javax.ejb.*;

import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ELogSeverity;
import org.toxsoft.uskat.core.api.evserv.ISkEventList;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.api.IBaEvents;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonSession;
import org.toxsoft.uskat.s5.server.backend.addons.events.S5BaEventsFrontendData;
import org.toxsoft.uskat.s5.server.backend.addons.events.S5BaEventsInitData;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.IS5BackendSysDescrSingleton;
import org.toxsoft.uskat.s5.server.sessions.init.IS5SessionInitData;
import org.toxsoft.uskat.s5.server.sessions.init.S5SessionInitResult;
import org.toxsoft.uskat.s5.server.sessions.pas.S5SessionCallbackWriter;

/**
 * Реализация сессии расширения бекенда {@link IS5BaCommandsSession}.
 *
 * @author mvk
 */
@Stateful
@StatefulTimeout( value = STATEFULL_TIMEOUT, unit = TimeUnit.MILLISECONDS )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
class S5BaCommandsSession
    extends S5AbstractBackendAddonSession
    implements IS5BaCommandsSession {

  private static final long serialVersionUID = 157157L;

  /**
   * Поддержка сервера для чтения/записи системного описания
   */
  @EJB
  private IS5BackendSysDescrSingleton sysdescrSupport;

  /**
   * Поддержка сервера для формирования команд
   */
  @EJB
  private IS5BackendSingleton eventsSupport;

  /**
   * Пустой конструктор.
   */
  public S5BaCommandsSession() {
    super( ISkBackendHardConstant.BAINF_EVENTS );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendAddonSession
  //
  @Override
  protected Class<? extends IS5BaCommandsSession> doGetSessionView() {
    return IS5BaCommandsSession.class;
  }

  @Override
  protected void doAfterInit( S5SessionCallbackWriter aCallbackWriter, IS5SessionInitData aInitData,
      S5SessionInitResult aInitResult ) {
    S5BaEventsFrontendData frontdata = new S5BaEventsFrontendData();
    S5BaEventsInitData eventsInit = aInitData.findAddonData( IBaEvents.ADDON_ID, S5BaEventsInitData.class );
    if( eventsInit != null ) {
      frontdata.events.setNeededEventGwids( eventsInit.events );
    }
    frontend().frontendData().setAddonData( IBaEvents.ADDON_ID, frontdata );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5BaCommandsSession
  //
  @Override
  public void fireEvents( ISkEventList aEvents ) {
    TsNullArgumentRtException.checkNull( aEvents );
    eventsSupport.fireEvents( frontend(), aEvents );
  }

  @Override
  public void subscribeToEvents( IGwidList aNeededGwids ) {
    TsNullArgumentRtException.checkNull( aNeededGwids );
    // Данные сессии
    S5BaEventsFrontendData frontendData =
        frontend().frontendData().findAddonData( IBaEvents.ADDON_ID, S5BaEventsFrontendData.class );
    // Реконфигурация набора
    frontendData.events.setNeededEventGwids( aNeededGwids );
    // Сохранение измененной сессии в кластере сервера
    updateSessionData();
    // Вывод протокола
    if( logger().isSeverityOn( ELogSeverity.INFO ) || logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
      // Вывод в журнал информации о регистрации ресурсов в сессии
      StringBuilder sb = new StringBuilder();
      sb.append( String.format( "setNeededEventGwids(...): sessionID = %s, changed resources:", sessionID() ) ); //$NON-NLS-1$
      sb.append( String.format( "\n   === events (%d) === ", Integer.valueOf( frontendData.events.gwids().size() ) ) ); //$NON-NLS-1$
      for( Gwid gwid : frontendData.events.gwids() ) {
        sb.append( String.format( "\n   %s", gwid ) ); //$NON-NLS-1$
      }
      logger().info( sb.toString() );
    }

  }

  @Override
  public ITimedList<SkEvent> queryObjEvents( IQueryInterval aInterval, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aInterval, aGwid );
    return eventsSupport.queryEvents( aInterval, new GwidList( aGwid ) );
  }
}
