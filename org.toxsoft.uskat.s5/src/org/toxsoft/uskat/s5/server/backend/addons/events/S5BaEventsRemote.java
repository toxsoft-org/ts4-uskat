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
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendRemote;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonRemote;
import org.toxsoft.uskat.s5.server.backend.messages.S5BaBeforeConnectMessages;

/**
 * Remote {@link IBaEvents} implementation.
 *
 * @author mvk
 */
class S5BaEventsRemote
    extends S5AbstractBackendAddonRemote<IS5BaEventsSession>
    implements IBaEvents {

  private final GwidList needEvents = new GwidList();

  /**
   * Constructor.
   *
   * @param aOwner {@link IS5BackendRemote} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5BaEventsRemote( IS5BackendRemote aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_EVENTS, IS5BaEventsSession.class );
  }

  // ------------------------------------------------------------------------------------
  // BackendAddonBase
  //
  @Override
  public void onBackendMessage( GtMessage aMessage ) {
    if( aMessage.messageId().equals( S5BaBeforeConnectMessages.MSG_ID ) ) {
      S5BaEventsInitData initData = new S5BaEventsInitData();
      initData.events.setAll( needEvents );
      owner().sessionInitData().setAddonData( IBaEvents.ADDON_ID, initData );
    }
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
    session().fireEvents( aEvents );
  }

  @Override
  public void subscribeToEvents( IGwidList aNeededGwids ) {
    TsNullArgumentRtException.checkNull( aNeededGwids );
    needEvents.setAll( aNeededGwids );
    IS5BaEventsSession session = findSession();
    if( session != null ) {
      session.subscribeToEvents( aNeededGwids );
    }
  }

  @Override
  public ITimedList<SkEvent> queryObjEvents( IQueryInterval aInterval, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aInterval, aGwid );
    return session().queryObjEvents( aInterval, aGwid );
  }
}
