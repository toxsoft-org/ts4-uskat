package org.toxsoft.uskat.core.backend.api;

import static org.toxsoft.core.tslib.bricks.strio.IStrioHardConstants.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.av.temporal.TemporalAtomicValueKeeper;
import org.toxsoft.core.tslib.bricks.events.msg.GenericMessage;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.bricks.strio.chario.impl.CharInputStreamString;
import org.toxsoft.core.tslib.bricks.strio.chario.impl.CharOutputStreamAppendable;
import org.toxsoft.core.tslib.bricks.strio.impl.StrioReader;
import org.toxsoft.core.tslib.bricks.strio.impl.StrioWriter;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.bricks.time.impl.TimeInterval;
import org.toxsoft.core.tslib.bricks.time.impl.TimedList;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.coll.IMapEdit;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.rtdserv.ISkRtdataService;

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
        sr.ensureSeparatorChar();
        ITimedList<ITemporalAtomicValue> v = new TimedList<>( TemporalAtomicValueKeeper.KEEPER.readColl( sr ) );
        map.put( g, new Pair<>( new TimeInterval( startTime, endTime ), v ) );
      } while( sr.readArrayNext() );
    }
    return map;
  }

}
