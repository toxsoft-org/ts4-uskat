package org.toxsoft.uskat.core.backend.api;

import static org.toxsoft.core.tslib.bricks.strio.IStrioHardConstants.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.temporal.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.strio.chario.impl.*;
import org.toxsoft.core.tslib.bricks.strio.impl.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.rtdserv.*;

/**
 * {@link IBaRtdata} message builder: new values of historical data received.
 *
 * @author mvk
 */
public class BaMsgRtdataHistData
    extends AbstractBackendMessageBuilder {

  /**
   * ID of the message.
   */
  public static final String MSG_ID = "HistData"; //$NON-NLS-1$

  /**
   * Singletone intance.
   */
  public static final BaMsgRtdataHistData INSTANCE = new BaMsgRtdataHistData();

  private static final String ARGID_NEW_VALUES = "NewValues"; //$NON-NLS-1$

  BaMsgRtdataHistData() {
    super( ISkRtdataService.SERVICE_ID, MSG_ID );
    defineArgNonValobj( ARGID_NEW_VALUES, EAtomicType.STRING, true );
  }

  /**
   * Creates the message instance.
   *
   * @param aNewValues {@link IMap}&lt;{@link Gwid},{@link Pair}&lt;{@link ITimeInterval},{@link ITimedList}&gt;&gt; -
   *          map "RTdata GWID" - "historical values"
   * @return {@link GtMessage} - created instance of the message
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public GtMessage makeMessage( IMap<Gwid, Pair<ITimeInterval, ITimedList<ITemporalAtomicValue>>> aNewValues ) {
    StringBuilder sb = new StringBuilder();
    IStrioWriter sw = new StrioWriter( new CharOutputStreamAppendable( sb ) );
    sw.writeChar( CHAR_ARRAY_BEGIN );
    for( int i = 0, count = aNewValues.size(); i < count; i++ ) {
      Gwid g = aNewValues.keys().get( i );
      Pair<ITimeInterval, ITimedList<ITemporalAtomicValue>> p = aNewValues.values().get( i );
      ITimeInterval interval = p.left();
      ITimedList<ITemporalAtomicValue> v = p.right();
      checkIntervals( g, interval, v );
      Gwid.KEEPER.write( sw, g );
      sw.writeSeparatorChar();
      sw.writeLong( interval.startTime() );
      sw.writeSeparatorChar();
      sw.writeLong( interval.endTime() );
      sw.writeSeparatorChar();
      TemporalAtomicValueKeeper.KEEPER.writeColl( sw, v, false );
      if( i < count - 1 ) {
        sw.writeSeparatorChar();
      }
    }
    sw.writeChar( CHAR_ARRAY_END );
    return makeMessageVarargs( ARGID_NEW_VALUES, sb.toString() );
  }

  /**
   * Extracts new current RTdata values argument from the message.
   *
   * @param aMsg {@link GenericMessage} - the message
   * @return {@link IMap}&lt;{@link Gwid},{@link Pair}&lt;{@link ITimeInterval},{@link ITimedList}&gt;&gt; - map "RTdata
   *         GWID" - "historical value"
   */
  public IMap<Gwid, Pair<ITimeInterval, ITimedList<ITemporalAtomicValue>>> getNewValues( GenericMessage aMsg ) {
    String s = getArg( aMsg, ARGID_NEW_VALUES ).asString();
    IMapEdit<Gwid, Pair<ITimeInterval, ITimedList<ITemporalAtomicValue>>> map = new ElemMap<>();
    IStrioReader sr = new StrioReader( new CharInputStreamString( s ) );
    if( sr.readArrayBegin() ) {
      do {
        Gwid g = Gwid.KEEPER.read( sr );
        sr.ensureSeparatorChar();
        long startTime = sr.readLong();
        sr.ensureSeparatorChar();
        long endTime = sr.readLong();
        ITimeInterval interval = new TimeInterval( startTime, endTime );
        sr.ensureSeparatorChar();
        ITimedList<ITemporalAtomicValue> v = new TimedList<>( TemporalAtomicValueKeeper.KEEPER.readColl( sr ) );
        checkIntervals( g, interval, v );
        map.put( g, new Pair<>( interval, v ) );
      } while( sr.readArrayNext() );
    }
    return map;
  }

  /**
   * Check interval of histdata values
   *
   * @param aGwid {@link Gwid} id of data
   * @param aInterval {@link ITimeInterval} interval for check
   * @param aValues {@link ITimedList} timed values list
   * @throws TsNullArgumentRtException any arg = null
   */
  @SuppressWarnings( "nls" )
  public static void checkIntervals( Gwid aGwid, ITimeInterval aInterval, ITimedList<ITemporalAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNulls( aGwid, aInterval, aValues );
    ITimeInterval vInterval = aValues.getInterval();
    if( aInterval.startTime() > vInterval.startTime() ) {
      throw new TsIllegalArgumentRtException( "%s: wrong values interval (left). aInterval = %s, vInterval = %s",
          aInterval, vInterval );
    }
    if( aInterval.endTime() < vInterval.endTime() ) {
      throw new TsIllegalArgumentRtException( "%s: wrong values interval (right). aInterval = %s, vInterval = %s",
          aInterval, vInterval );
    }
  }
}
