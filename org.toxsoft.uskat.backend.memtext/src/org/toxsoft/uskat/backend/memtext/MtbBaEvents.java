package org.toxsoft.uskat.backend.memtext;

import static org.toxsoft.uskat.backend.memtext.IBackendMemtextConstants.*;
import static org.toxsoft.uskat.core.backend.api.IBaEventsMessages.*;

import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.bricks.strio.impl.StrioUtils;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;
import org.toxsoft.core.tslib.bricks.time.impl.TimedList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.derivative.IRingBuffer;
import org.toxsoft.core.tslib.coll.derivative.RingBuffer;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedBundleList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.TsMiscUtils;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.evserv.ISkEventList;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.api.IBaEvents;
import org.toxsoft.uskat.core.impl.SkEventList;

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
  // Package API
  //

  @Override
  void papiRemoveEntitiesOfClassIdsBeforeSave( IStringList aClassIds ) {
    // retrieve from buffer event that shall remain in storage
    IListEdit<SkEvent> eventsToRemain = new ElemLinkedBundleList<>();
    while( !eventsHistory.isEmpty() ) {
      SkEvent e = eventsHistory.get();
      if( aClassIds.hasElem( e.eventGwid().classId() ) ) {
        eventsToRemain.add( e );
      }
    }
    // put back remained events to buffer
    for( SkEvent e : eventsToRemain ) {
      eventsHistory.put( e );
    }
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
  public ITimedList<SkEvent> queryObjEvents( ITimeInterval aInterval, Gwid aGwid ) {
    TimedList<SkEvent> result = new TimedList<>();
    for( SkEvent e : eventsHistory.getItems() ) {
      if( TimeUtils.contains( aInterval, e.timestamp() ) ) {
        if( e.eventGwid().skid().equals( aGwid.skid() ) ) {
          if( aGwid.isPropMulti() || aGwid.propId().equals( e.eventGwid().propId() ) ) {
            result.add( e );
          }
        }
      }
    }
    return result;
  }

}
