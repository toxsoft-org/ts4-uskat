package org.toxsoft.uskat.backend.memtext;

import static org.toxsoft.core.tslib.bricks.strio.IStrioHardConstants.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.backend.memtext.IBackendMemtextConstants.*;

import java.util.concurrent.locks.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.temporal.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.strio.impl.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;

/**
 * {@link IBaRtdata} implementation.
 * <p>
 * Notes on implementation:
 * <ul>
 * <li>this addon runs {@link InternalThread} for periodic tasks;</li>
 * <li>methods named <code>internalXxx()</code> are invoked in the from syncronized blocks of code;</li>
 * </ul>
 *
 * @author hazard157
 */
public class MtbBaRtdata
    extends MtbAbstractAddon
    implements IBaRtdata {

  /**
   * Granularity (responsiveness) of {@link InternalThread#run()} method code execution.
   * <p>
   * This value must not be changed because {@link IBackendMemtextConstants#OPDEF_CURR_DATA_10MS_TICKS} for users are
   * declared to have 10ms units.
   */
  private static final long INTERNAL_THREAD_TICK_MSECS = 10L;

  /**
   * Prefix of the {@link InternalThread} name, name ends with backend ID.
   */
  private static final String INTERNAL_THREAD_NAME_PREFIX = MtbBaRtdata.class.getSimpleName() + " internal thread "; //$NON-NLS-1$

  /**
   * Time interval between checks to update {@link #earliestTimeToStore} and start outdated data removal.
   */
  private static final long HD_WORK_CHECK_INTERVAL_MSECS = 3600_000L; // 1 hr

  /**
   * Keyword for STRIO storage of RTdata history.
   */
  private static final String KW_HISTORY_DATA = "HistoryData"; //$NON-NLS-1$

  /**
   * Internal thread.
   * <p>
   * THis thread:
   * <ul>
   * <li>few times in second checks for new currdata values and messages them to the frontend;</li>
   * <li>periodically removes outdated historical data.</li>
   * </ul>
   *
   * @author hazard157
   */
  class InternalThread
      extends Thread {

    private volatile boolean needStop = false;

    public InternalThread( String aName ) {
      super( aName );
    }

    @Override
    public void run() {
      /**
       * we'll invoke code every TICK_MSECS * INVOKE_TICKS milleconds, however for responsiveness we'll check #needStop
       * flag every TICK_MSECS milliseconds.
       */
      int ticksCounter = 0;
      // run until thread stop is requested
      while( !needStop ) {
        try {
          Thread.sleep( INTERNAL_THREAD_TICK_MSECS );
          if( ++ticksCounter >= internalThreadInvokeTicks ) {
            // following code runs every TICK_MSECS * INVOKE_TICKS milleconds
            ticksCounter = 0;
            // check for new currdata values and message them to the frontend
            cdLock.writeLock().lock();
            try {
              internalSendNewCurrDataToFrontend();
            }
            finally {
              cdLock.writeLock().unlock();
            }
            // update #earliestTimeToStore if needed and remove outdated data
            hdLock.writeLock().lock();
            try {
              internalWorkWithDataHistory();
            }
            finally {
              hdLock.writeLock().unlock();
            }
          }
        }
        catch( InterruptedException ex ) {
          LoggerUtils.errorLogger().error( ex );
        }
      }
    }

    void stopTheThread() {
      // request thread to stop
      needStop = true;
      // and wait until it does
      try {
        Thread.sleep( 2 * INTERNAL_THREAD_TICK_MSECS );
      }
      catch( InterruptedException ex ) {
        LoggerUtils.errorLogger().error( ex );
      }
    }

  }

  private final long historyDepthMsecs;
  private final int  internalThreadInvokeTicks;

  private final ReentrantReadWriteLock       cdLock       = new ReentrantReadWriteLock();
  private final GwidList                     cdReadGwids  = new GwidList();
  private final GwidList                     cdWriteGwids = new GwidList();
  private final IMapEdit<Gwid, IAtomicValue> cdMap        = new ElemMap<>();

  private final ReentrantReadWriteLock                          hdLock = new ReentrantReadWriteLock();
  private final IMapEdit<Gwid, TimedList<ITemporalAtomicValue>> hdMap  = new ElemMap<>();

  private final InternalThread theThread;

  /**
   * This reference is changed in {@link #internalSendNewCurrDataToFrontend()}.
   */
  private IMapEdit<Gwid, IAtomicValue> newValues = new ElemMap<>();

  private long earliestTimeToStore = 0L;

  /**
   * Constructor.
   *
   * @param aOwner {@link MtbAbstractBackend} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public MtbBaRtdata( MtbAbstractBackend aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_RTDATA );
    int val = OPDEF_CURR_DATA_10MS_TICKS.getValue( owner().argContext().params() ).asInt();
    internalThreadInvokeTicks = TsMiscUtils.inRange( val, MIN_CURR_DATA_10MS_TICKS, MAX_CURR_DATA_10MS_TICKS );
    val = OPDEF_HISTORY_DEPTH_HOURS.getValue( owner().argContext().params() ).asInt();
    historyDepthMsecs = TsMiscUtils.inRange( val, MIN_HISTORY_DEPTH_HOURS, MAX_HISTORY_DEPTH_HOURS ) * 3600_000L;
    earliestTimeToStore = System.currentTimeMillis() - historyDepthMsecs;
    String threadName = INTERNAL_THREAD_NAME_PREFIX + owner().getBackendInfo().id();
    theThread = new InternalThread( threadName );
    theThread.start();
  }

  // ------------------------------------------------------------------------------------
  // MtbAbstractAddon
  //

  @Override
  public void close() {
    theThread.stopTheThread();
  }

  @Override
  public void clear() {
    // clear currdata
    cdLock.writeLock().lock();
    try {
      cdReadGwids.clear();
      cdWriteGwids.clear();
      cdMap.clear();
    }
    finally {
      cdLock.writeLock().unlock();
    }
    // clear history
    hdLock.writeLock().lock();
    try {
      hdMap.clear();
    }
    finally {
      hdLock.writeLock().unlock();
    }
  }

  @Override
  protected void doWrite( IStrioWriter aSw ) {
    hdLock.readLock().lock();
    try {
      StrioUtils.writeKeywordHeader( aSw, KW_HISTORY_DATA, true );
      aSw.writeChar( CHAR_ARRAY_BEGIN );
      aSw.incNewLine();
      for( int i = 0, count = hdMap.size(); i < count; i++ ) {
        Gwid g = hdMap.keys().get( i );
        aSw.writeQuotedString( g.toString() );
        aSw.writeChars( CHAR_SPACE, CHAR_EQUAL, CHAR_SPACE );
        ITimedList<ITemporalAtomicValue> ll = hdMap.values().get( i );
        StrioUtils.writeCollection( aSw, EMPTY_STRING, ll, TemporalAtomicValueKeeper.KEEPER, false );
        if( i < count - 1 ) {
          aSw.writeSeparatorChar();
          aSw.writeEol();
        }
      }
      aSw.decNewLine();
      aSw.writeChar( CHAR_ARRAY_END );
    }
    finally {
      hdLock.readLock().unlock();
    }

  }

  @Override
  protected void doRead( IStrioReader aSr ) {
    hdLock.writeLock().lock();
    try {
      hdMap.clear();
      StrioUtils.ensureKeywordHeader( aSr, KW_HISTORY_DATA );
      if( aSr.readArrayBegin() ) {
        do {
          String strGwid = aSr.readQuotedString();
          Gwid g = Gwid.of( strGwid );
          aSr.ensureChar( CHAR_EQUAL );
          IList<ITemporalAtomicValue> tmp =
              StrioUtils.readCollection( aSr, EMPTY_STRING, TemporalAtomicValueKeeper.KEEPER );
          TimedList<ITemporalAtomicValue> ll = new TimedList<>( tmp );
          hdMap.put( g, ll );
        } while( aSr.readArrayNext() );
      }
    }
    finally {
      hdLock.writeLock().unlock();
    }
  }

  // ------------------------------------------------------------------------------------
  // Package API
  //

  @Override
  void papiRemoveEntitiesOfClassIdsBeforeSave( IStringList aClassIds ) {
    IListEdit<Gwid> toRemove = new ElemArrayList<>();
    hdLock.writeLock().lock();
    try {
      for( Gwid g : hdMap.keys() ) {
        if( aClassIds.hasElem( g.classId() ) ) {
          toRemove.add( g );
        }
      }
      for( Gwid g : toRemove ) {
        hdMap.removeByKey( g );
      }
    }
    finally {
      hdLock.writeLock().unlock();
    }
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private static boolean isRtdataGwidValid( Gwid aGwid ) {
    if( aGwid.isAbstract() ) {
      return false;
    }
    if( aGwid.isMulti() ) {
      return false;
    }
    if( aGwid.kind() != EGwidKind.GW_RTDATA ) {
      return false;
    }
    return true;
  }

  /**
   * Configures {@link #cdMap} to contain union of {@link #cdReadGwids} and {@link #cdWriteGwids}.
   */
  private void internalConfigureCurrDataReadWrite() {
    // add new GWIDs ro read
    for( Gwid g : cdReadGwids ) {
      if( isRtdataGwidValid( g ) ) {
        if( !cdMap.hasKey( g ) ) {
          cdMap.put( g, IAtomicValue.NULL );
        }
      }
    }
    // add new GWIDs ro write
    for( Gwid g : cdWriteGwids ) {
      if( isRtdataGwidValid( g ) ) {
        if( !cdMap.hasKey( g ) ) {
          cdMap.put( g, IAtomicValue.NULL );
        }
      }
    }
    // remove outdated GWIDs
    IListEdit<Gwid> toRemove = new ElemArrayList<>();
    for( Gwid g : cdMap.keys() ) {
      if( !cdReadGwids.hasElem( g ) && !cdWriteGwids.hasElem( g ) ) {
        toRemove.add( g );
      }
    }
    for( Gwid g : toRemove ) {
      cdMap.removeByKey( g );
    }
  }

  /**
   * Writes to destination list historical values.
   * <p>
   * Note on implementation:
   * <ul>
   * <li>ignores values out of interval {@link #earliestTimeToStore} .. now;</li>
   * <li>replaces values with the same timestamp by new one;</li>
   * <li>if source contains duplicate timestamps only last value will be stored.</li>
   * </ul>
   *
   * @param aDest {@link TimedList}&lt;{@link ITemporalAtomicValue}&gt; - destination list
   * @param aSource {@link ITimedList}&lt;{@link ITemporalAtomicValue}&gt; - source wof history data values
   */
  private void internalUpdateHistory( TimedList<ITemporalAtomicValue> aDest,
      ITimedList<ITemporalAtomicValue> aSource ) {
    long now = System.currentTimeMillis();
    for( ITemporalAtomicValue e : aSource ) {
      if( e.timestamp() >= earliestTimeToStore && e.timestamp() <= now ) {
        aDest.replaceByTimestamp( e );
      }
    }
  }

  private void internalSendNewCurrDataToFrontend() {
    // safely get new currdata values
    IMap<Gwid, IAtomicValue> nvMap;
    cdLock.writeLock().lock();
    try {
      if( newValues.isEmpty() ) {
        return;
      }
      nvMap = newValues;
      newValues = new ElemMap<>();
    }
    finally {
      cdLock.writeLock().unlock();
    }
    // prepare and send message to frontend (note: frontend call is thread-safe)
    GtMessage msg = BaMsgRtdataCurrData.INSTANCE.makeMessage( nvMap );
    owner().frontend().onBackendMessage( msg );
  }

  private long            hdWorkLastCheckTime = 0;
  private IListEdit<Gwid> hdWorkWithGwids     = new ElemArrayList<>();

  /**
   * This methods periodically updates {@link #earliestTimeToStore} and removed outdated history data.
   * <p>
   * Outdated data removing algorithm:
   * <ul>
   * <li>outdated data removal starts every time, when {@link #earliestTimeToStore} is updated (if at this moment
   * {@link #hdWorkWithGwids} list is empty);</li>
   * <li>algorithm at start remembers {@link #hdWorkWithGwids} list of GWIDs from {@link #hdMap} keys;</li>
   * <li>on every call of this method only one GWID from {@link #hdWorkWithGwids} are processed for backend
   * responsiveness purposes;</li>
   * <li>processed GWID is removed from {@link #hdWorkWithGwids} and when list becames empty the algirtm is finished
   * until next start.</li>
   * </ul>
   */
  private void internalWorkWithDataHistory() {
    // next step of outdated data removal algorithm
    if( !hdWorkWithGwids.isEmpty() ) {
      Gwid gwid = hdWorkWithGwids.removeByIndex( hdWorkWithGwids.size() - 1 );
      internalRemoveOutdatedHistDataOfGwid( gwid );
    }
    // check is performed every HD_WORK_CHECK_INTERVAL_MSECS milliseconds
    long currTime = System.currentTimeMillis();
    if( currTime - hdWorkLastCheckTime < HD_WORK_CHECK_INTERVAL_MSECS ) {
      // update #earliestTimeToStore
      hdWorkLastCheckTime = currTime;
      earliestTimeToStore = currTime - historyDepthMsecs;
      // start outdated data removal if needed
      if( hdWorkWithGwids.isEmpty() && !hdMap.isEmpty() ) {
        hdWorkWithGwids = new ElemArrayList<>( hdMap.keys() );
      }
    }
  }

  private void internalRemoveOutdatedHistDataOfGwid( Gwid aGwid ) {
    TimedList<ITemporalAtomicValue> ll = hdMap.findByKey( aGwid );
    if( ll == null ) {
      return; // rare but possible - historic data was removed from sysdescr
    }
    // find last element with #earliestTimeToStore time or before
    int index = ll.firstIndexOrBefore( earliestTimeToStore );
    if( index >= 0 ) {
      if( index == ll.size() - 1 ) { // whole list is outdated
        ll.clear();
      }
      else {
        ll.removeRangeByIndex( 0, index + 1 );
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // IBaRtdata
  //

  @Override
  public void configureCurrDataReader( IList<Gwid> aRtdGwids ) {
    cdLock.writeLock().lock();
    try {
      cdReadGwids.setAll( aRtdGwids );
      internalConfigureCurrDataReadWrite();
    }
    finally {
      cdLock.writeLock().lock();
    }
  }

  @Override
  public void configureCurrDataWriter( IList<Gwid> aRtdGwids ) {
    cdLock.writeLock().lock();
    try {
      cdWriteGwids.setAll( aRtdGwids );
      internalConfigureCurrDataReadWrite();
    }
    finally {
      cdLock.writeLock().lock();
    }
  }

  @Override
  public void writeCurrData( Gwid aGwid, IAtomicValue aValue ) {
    cdLock.writeLock().lock();
    try {
      if( !cdMap.hasElem( aValue ) ) {
        return;
      }
      cdMap.put( aGwid, aValue );
      newValues.put( aGwid, aValue );
    }
    finally {
      cdLock.writeLock().unlock();
    }
  }

  @Override
  public void writeHistData( Gwid aGwid, ITimeInterval aInterval, ITimedList<ITemporalAtomicValue> aValues ) {
    hdLock.writeLock().lock();
    try {
      if( !isRtdataGwidValid( aGwid ) ) {
        return;
      }
      TimedList<ITemporalAtomicValue> ll = hdMap.findByKey( aGwid );
      if( ll == null ) {
        ll = new TimedList<>();
        hdMap.put( aGwid, ll );
      }
      internalUpdateHistory( ll, aValues );
    }
    finally {
      hdLock.writeLock().unlock();
    }
  }

  @Override
  public ITimedList<ITemporalAtomicValue> queryObjRtdata( IQueryInterval aInterval, Gwid aGwid ) {
    hdLock.readLock().lock();
    try {
      ITimedList<ITemporalAtomicValue> ll = hdMap.findByKey( aGwid );
      if( ll == null ) {
        return new TimedList<>(); // TODO we need ITimedList.EMPTY !
      }
      return ll.selectInterval( aInterval );
    }
    finally {
      hdLock.readLock().unlock();
    }
  }

}
