package org.toxsoft.uskat.s5.server.backend.addons.events;

import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.evserv.ISkEventList;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.api.IBaEvents;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendLocal;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonLocal;
import org.toxsoft.uskat.s5.server.backend.supports.events.IS5BackendEventSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.events.impl.S5BackendEventSingleton;

/**
 * Local {@link IBaEvents} implementation.
 *
 * @author mvk
 */
class S5BaEventsLocal
    extends S5AbstractBackendAddonLocal
    implements IBaEvents {

  /**
   * Поддержка сервера для чтения/записи системного описания
   */
  private final IS5BackendEventSingleton eventsSupport;

  /**
   * Данные конфигурации фронтенда для {@link IBaEvents}
   */
  private S5BaEventsData frontendData = new S5BaEventsData();

  /**
   * Constructor.
   *
   * @param aOwner {@link IS5BackendLocal} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5BaEventsLocal( IS5BackendLocal aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_EVENTS );
    // Синглтон поддержки чтения/записи системного описания
    eventsSupport =
        aOwner.backendSingleton().get( S5BackendEventSingleton.BACKEND_EVENTS_ID, IS5BackendEventSingleton.class );
    // Установка конфигурации фронтенда
    frontend().frontendData().setBackendAddonData( IBaEvents.ADDON_ID, frontendData );
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
  // IBaEvents
  //
  @Override
  public void fireEvents( ISkEventList aEvents ) {
    TsNullArgumentRtException.checkNull( aEvents );
    eventsSupport.fireEvents( frontend(), aEvents );
  }

  @Override
  public void subscribeToEvents( IGwidList aNeededGwids ) {
    TsNullArgumentRtException.checkNull( aNeededGwids );
    frontendData.events.setNeededEventGwids( aNeededGwids );
  }

  @Override
  public ITimedList<SkEvent> queryObjEvents( IQueryInterval aInterval, Gwid aGwid ) {
    return eventsSupport.queryEvents( aInterval, new GwidList( aGwid ) );
  }
}
