package org.toxsoft.uskat.backend.memtext;

import static org.toxsoft.uskat.backend.memtext.IBackendMemtextConstants.*;
import static org.toxsoft.uskat.core.backend.api.IBaEventsMessages.*;

import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.strio.impl.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.derivative.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.evserv.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * {@link IBaEvents} implementation.
 *
 * @author hazard157
 */
class MtbBaEvents
    extends MtbAbstractAddon
    implements IBaEvents {

  private static final String KW_HISTORY = "EventsHistory"; //$NON-NLS-1$

  private final GwidList             gwidsOfIntereset = new GwidList();
  private final IRingBuffer<SkEvent> eventsHistory;

  /**
   * Constructor.
   *
   * @param aOwner {@link MtbAbstractBackend} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public MtbBaEvents( MtbAbstractBackend aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_EVENTS );
    int count = OPDEF_MAX_EVENTS_COUNT.getValue( aOwner.argContext().params() ).asInt();
    count = TsMiscUtils.inRange( count, MIN_MAX_EVENTS_COUNT, MIN_MAX_EVENTS_COUNT );
    eventsHistory = new RingBuffer<>( count );
  }

  // ------------------------------------------------------------------------------------
  // MtbAbstractAddon
  //

  @Override
  public void close() {
    // nop
  }

  @Override
  public void clear() {
    // nop
  }

  @Override
  protected void doWrite( IStrioWriter aSw ) {
    IList<SkEvent> ll = eventsHistory.getItems();
    StrioUtils.writeCollection( aSw, KW_HISTORY, ll, SkEvent.KEEPER, true );
  }

  @Override
  protected void doRead( IStrioReader aSr ) {
    IList<SkEvent> ll = StrioUtils.readCollection( aSr, KW_HISTORY, SkEvent.KEEPER );
    eventsHistory.clear();
    for( SkEvent e : ll ) {
      eventsHistory.put( e );
    }
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private static boolean isNeededEvent( SkEvent aEvent, IGwidList aNeededGwids ) {
    for( Gwid g : aNeededGwids ) {
      if( GwidUtils.covers( g, aEvent.eventGwid() ) ) {
        return true;
      }
    }
    return false;
  }

  // ------------------------------------------------------------------------------------
  // IBaLinks
  //

  @Override
  public void fireEvents( ISkEventList aEvents ) {
    TsNullArgumentRtException.checkNull( aEvents );
    // remember history
    for( SkEvent e : aEvents ) {
      eventsHistory.put( e );
    }
    // make list of events of interest
    SkEventList eventList = new SkEventList();
    for( SkEvent e : aEvents ) {
      if( isNeededEvent( e, gwidsOfIntereset ) ) {
        if( isNeededEvent( e, gwidsOfIntereset ) ) {
          eventList.add( e );
        }
      }
    }
    // put messages to the frontends back
    if( !eventList.isEmpty() ) {
      GtMessage msg = makeMessage( aEvents );
      owner().frontend().onBackendMessage( msg );
    }
  }

  @Override
  public void subscribeToEvents( IGwidList aNeededGwids ) {
    TsNullArgumentRtException.checkNulls( aNeededGwids );
    gwidsOfIntereset.setAll( aNeededGwids );
  }

  @Override
  public ITimedList<SkEvent> queryEvents( IQueryInterval aInterval, IGwidList aNeededGwids ) {
    TsNullArgumentRtException.checkNulls( aInterval, aNeededGwids );
    TimedList<SkEvent> result = new TimedList<>();
    for( SkEvent e : eventsHistory.getItems() ) {
      if( TimeUtils.contains( aInterval, e.timestamp() ) ) {
        if( isNeededEvent( e, aNeededGwids ) ) {
          result.add( e );
        }
      }
    }
    return result;
  }

}
