package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.backend.api.IBaEventsMessages.*;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.evserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.devapi.*;

/**
 * {@link ISkEventService} implementation.
 *
 * @author hazard157
 */
public class SkCoreServEvents
    extends AbstractSkCoreService
    implements ISkEventService {

  /**
   * Service creator singleton.
   */
  public static final ISkServiceCreator<AbstractSkService> CREATOR = SkCoreServEvents::new;

  private final IMapEdit<ISkEventHandler, GwidList> handlersMap = new ElemMap<>();

  /**
   * Constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  SkCoreServEvents( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkCoreService
  //

  @Override
  protected void doInit( ITsContextRo aArgs ) {
    // nop
  }

  @Override
  protected void doClose() {
    // nop
  }

  @Override
  protected boolean onBackendMessage( GenericMessage aMessage ) {
    switch( aMessage.messageId() ) {
      case MSGID_SK_EVENTS: {
        ISkEventList evList = extractSkEventsList( aMessage );
        callEventHandlers( evList );
        return true;
      }
      default:
        return false;
    }
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  /**
   * Determines if event with <code>aEventGwid</code> is accepted by the GWIDs of intereset.
   *
   * @param aEventGwid {@link Gwid} - the event GWID
   * @param aInteresetGwids {@link IGwidList} - GWIDs of intereset
   * @return boolean - event is of intereset flag
   */
  private boolean isEventOfIntereset( Gwid aEventGwid, IGwidList aInteresetGwids ) {
    // iterate over GWIDs of intereset
    for( Gwid g : aInteresetGwids ) {
      boolean b = gwidService().coversSingle( g, aEventGwid, ESkClassPropKind.EVENT );
      if( b ) {
        return true;
      }
    }
    return false;
  }

  /**
   * Invokes registered event handlers for the specified events.
   *
   * @param aEvents {@link ITimedList}&lt;{@link SkEvent}&gt; - the events to be handled
   */
  private void callEventHandlers( ITimedList<SkEvent> aEvents ) {
    // make list of events to be passed to each registered handlers
    IMapEdit<ISkEventHandler, SkEventList> eventsByHandlers = new ElemMap<>();
    // iterate all events over all handlers
    for( SkEvent event : aEvents ) {
      for( ISkEventHandler handler : handlersMap.keys() ) {
        IGwidList gwidsOfIntereset = handlersMap.getByKey( handler );
        if( isEventOfIntereset( event.eventGwid(), gwidsOfIntereset ) ) {
          SkEventList events = eventsByHandlers.findByKey( handler );
          if( events == null ) {
            events = new SkEventList();
            eventsByHandlers.put( handler, events );
          }
          events.add( event );
        }
      }
    }
    // send events
    for( ISkEventHandler handler : eventsByHandlers.keys() ) {
      handler.onEvents( eventsByHandlers.getByKey( handler ) );
    }
  }

  /**
   * Makes GWIDs list of events needed by this service to receive from backend.
   * <p>
   * List is collected from the GWIDs in {@link #handlersMap}.
   *
   * @return {@link IGwidList} - GWIDs list to subscribe to the backend
   */
  private IGwidList makeNeededGwidsList() {
    GwidList ll = new GwidList();
    // iterate over handlersMap
    for( IGwidList hl : handlersMap.values() ) {
      // update resulting list by GWID from handlers interest
      for( Gwid g : hl ) {
        gwidService().updateGwidsOfIntereset( ll, g, ESkClassPropKind.EVENT );
      }
    }
    return ll;
  }

  /**
   * Updates events subscription to the backend.
   */
  private void updateBackendSubscription() {
    IGwidList ll = makeNeededGwidsList();
    ba().baEvents().subscribeToEvents( ll );
  }

  // ------------------------------------------------------------------------------------
  // ISkEventService
  //

  @Override
  public void fireEvent( SkEvent aEvent ) {
    TsNullArgumentRtException.checkNull( aEvent );
    SkEventList events = new SkEventList();
    events.add( aEvent );
    ba().baEvents().fireEvents( events );
  }

  @Override
  public void registerHandler( IGwidList aNeededGwids, ISkEventHandler aEventHandler ) {
    TsNullArgumentRtException.checkNulls( aNeededGwids, aEventHandler );
    if( !aNeededGwids.isEmpty() ) {
      // get or create GWIDs list of intereset
      GwidList ll = handlersMap.findByKey( aEventHandler );
      if( ll == null ) {
        ll = new GwidList();
        handlersMap.put( aEventHandler, ll );
      }
      // update GWIDs list
      for( Gwid g : aNeededGwids ) {
        gwidService().updateGwidsOfIntereset( ll, g, ESkClassPropKind.EVENT );
      }
    }
    else {
      handlersMap.removeByKey( aEventHandler );
    }
    updateBackendSubscription();
  }

  @Override
  public void unregisterHandler( ISkEventHandler aEventHandler ) {
    if( handlersMap.removeByKey( aEventHandler ) != null ) {
      updateBackendSubscription();
    }
  }

  @Override
  public ITimedList<SkEvent> queryObjEvents( IQueryInterval aInterval, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aInterval, aGwid );
    TsIllegalArgumentRtException.checkTrue( aGwid.kind() != EGwidKind.GW_EVENT );
    TsIllegalArgumentRtException.checkTrue( aGwid.isAbstract() );
    TsIllegalArgumentRtException.checkTrue( aGwid.isStridMulti() );
    TsItemNotFoundRtException.checkFalse( gwidService().exists( aGwid ) );
    return ba().baEvents().queryObjEvents( aInterval, aGwid );
  }

}
