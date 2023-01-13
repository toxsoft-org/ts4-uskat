package org.toxsoft.uskat.s5.server.backend.addons.events;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;

import java.util.concurrent.TimeUnit;

import javax.ejb.*;

import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ELogSeverity;
import org.toxsoft.uskat.core.api.evserv.ISkEventList;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.api.IBaEvents;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonSessionControl;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonSession;
import org.toxsoft.uskat.s5.server.backend.supports.events.IS5BackendEventSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.IS5BackendSysDescrSingleton;
import org.toxsoft.uskat.s5.server.sessions.init.IS5SessionInitData;
import org.toxsoft.uskat.s5.server.sessions.init.S5SessionInitResult;
import org.toxsoft.uskat.s5.server.sessions.pas.S5SessionMessenger;

/**
 * Реализация сессии расширения бекенда {@link IS5BaEventsSession}.
 *
 * @author mvk
 */
@Stateful
@StatefulTimeout( value = STATEFULL_TIMEOUT, unit = TimeUnit.MILLISECONDS )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
@SuppressWarnings( "unused" )
public class S5BaEventsSession
    extends S5AbstractBackendAddonSession
    implements IS5BaEventsSession, IS5BackendAddonSessionControl {

  private static final long serialVersionUID = 157157L;

  /**
   * Поддержка сервера для чтения/записи системного описания
   */
  @EJB
  private IS5BackendSysDescrSingleton sysdescrSupport;

  /**
   * Поддержка сервера для формирования событий
   */
  @EJB
  private IS5BackendEventSingleton eventsSupport;

  /**
   * Пустой конструктор.
   */
  public S5BaEventsSession() {
    super( ISkBackendHardConstant.BAINF_EVENTS );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendAddonSession
  //
  @Override
  protected Class<? extends IS5BaEventsSession> doGetSessionView() {
    return IS5BaEventsSession.class;
  }

  @Override
  protected void doAfterInit( S5SessionMessenger aMessenger, IS5SessionInitData aInitData,
      S5SessionInitResult aInitResult ) {
    S5BaEventsData baData = new S5BaEventsData();
    S5BaEventsData initData = aInitData.findBackendAddonData( IBaEvents.ADDON_ID, S5BaEventsData.class );
    if( initData != null ) {
      baData.events.setNeededEventGwids( initData.events.gwids() );
    }
    frontend().frontendData().setBackendAddonData( IBaEvents.ADDON_ID, baData );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5BaEventsSession
  //
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public void fireEvents( ISkEventList aEvents ) {
    TsNullArgumentRtException.checkNull( aEvents );
    eventsSupport.fireEvents( frontend(), aEvents );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void subscribeToEvents( IGwidList aNeededGwids ) {
    TsNullArgumentRtException.checkNull( aNeededGwids );
    // Данные сессии
    S5BaEventsData baData = frontend().frontendData().findBackendAddonData( IBaEvents.ADDON_ID, S5BaEventsData.class );
    // Реконфигурация набора
    baData.events.setNeededEventGwids( aNeededGwids );
    // Сохранение измененной сессии в кластере сервера
    writeSessionData();
    // Вывод протокола
    if( logger().isSeverityOn( ELogSeverity.INFO ) || logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
      // Вывод в журнал информации о регистрации ресурсов в сессии
      StringBuilder sb = new StringBuilder();
      sb.append( String.format( "setNeededEventGwids(...): sessionID = %s, changed resources:", sessionID() ) ); //$NON-NLS-1$
      sb.append( String.format( "\n   === events (%d) === ", Integer.valueOf( baData.events.gwids().size() ) ) ); //$NON-NLS-1$
      for( Gwid gwid : baData.events.gwids() ) {
        sb.append( String.format( "\n   %s", gwid ) ); //$NON-NLS-1$
      }
      logger().info( sb.toString() );
    }

  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public ITimedList<SkEvent> queryObjEvents( ITimeInterval aInterval, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aInterval, aGwid );
    return eventsSupport.queryEvents( aInterval, new GwidList( aGwid ) );
  }
}
