package org.toxsoft.uskat.core.backend.api;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.core.tslib.bricks.strio.IStrioHardConstants.*;

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
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.bricks.time.impl.TimedList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.gwid.EGwidKind;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsUnsupportedFeatureRtException;
import org.toxsoft.uskat.core.api.hqserv.ISkHistoryQueryService;

/**
 * {@link IBaQueries} message builder: next portion of data delivered from backend to frontend.
 *
 * @author hazard157
 */
public class BaMsgQueryNextData
    extends AbstractBackendMessageBuilder {

  /**
   * ID of the message.
   */
  public static final String MSG_ID = "NextData"; //$NON-NLS-1$

  /**
   * Singletone intance.
   */
  public static final BaMsgQueryNextData INSTANCE = new BaMsgQueryNextData();

  private static final String ARGID_IS_FINISHED = "isFinished"; //$NON-NLS-1$
  private static final String ARGID_QUERY_ID    = "queryId";    //$NON-NLS-1$
  private static final String ARGID_KIND        = "kind";       //$NON-NLS-1$
  private static final String ARGID_VALUES      = "values";     //$NON-NLS-1$

  BaMsgQueryNextData() {
    super( ISkHistoryQueryService.SERVICE_ID, MSG_ID );
    defineArgNonValobj( ARGID_QUERY_ID, STRING, true );
    defineArgValobj( ARGID_KIND, EGwidKind.KEEPER_ID, true );
    defineArgNonValobj( ARGID_VALUES, STRING, true );
    defineArgNonValobj( ARGID_IS_FINISHED, BOOLEAN, false, TSID_DEFAULT_VALUE, AV_FALSE );
  }

  /**
   * Creates the message instance of kind {@link EGwidKind#GW_RTDATA}.
   *
   * @param aQueryId String - the query ID
   * @param aValues {@link IStringMap}&lt;{@link ITimedList}&lt;{@link ITemporalAtomicValue}&gt;&gt; - the values map
   * @param aFinished boolean - finished flag
   * @return {@link GtMessage} - created instance of the message
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public GtMessage makeMessageAtomicValues( String aQueryId, IStringMap<ITimedList<ITemporalAtomicValue>> aValues,
      boolean aFinished ) {
    StringBuilder sb = new StringBuilder();
    IStrioWriter sw = new StrioWriter( new CharOutputStreamAppendable( sb ) );
    sw.writeChar( CHAR_ARRAY_BEGIN );
    for( int i = 0, count = aValues.size(); i < count; i++ ) {
      String k = aValues.keys().get( i );
      ITimedList<ITemporalAtomicValue> v = aValues.values().get( i );
      sw.writeQuotedString( k );
      sw.writeSeparatorChar();
      TemporalAtomicValueKeeper.KEEPER.coll2str( v );
      if( i < count - 1 ) {
        sw.writeSeparatorChar();
      }
    }
    sw.writeChar( CHAR_ARRAY_END );
    return makeMessageVarargs( //
        ARGID_QUERY_ID, avStr( aQueryId ), //
        ARGID_KIND, avValobj( EGwidKind.GW_RTDATA ), //
        ARGID_VALUES, sb.toString(), //
        ARGID_IS_FINISHED, avBool( aFinished ) //
    );
  }

  /**
   * Extracts query ID argument from the message.
   *
   * @param aMsg {@link GenericMessage} - the message
   * @return String - argument extracted from the message
   */
  public String getQueryId( GenericMessage aMsg ) {
    return getArg( aMsg, ARGID_QUERY_ID ).asString();
  }

  /**
   * Extracts is finshed argument from the message.
   *
   * @param aMsg {@link GenericMessage} - the message
   * @return boolean - argument extracted from the message
   */
  public boolean getIsFinished( GenericMessage aMsg ) {
    return getArg( aMsg, ARGID_IS_FINISHED ).asBool();
  }

  /**
   * Extracts GWID kind argument from the message.
   *
   * @param aMsg {@link GenericMessage} - the message
   * @return EGw - argument extracted from the message
   */
  public EGwidKind getGwidKind( GenericMessage aMsg ) {
    return getArg( aMsg, ARGID_KIND ).asValobj();
  }

  /**
   * Extracts atomic values map argument from the message.
   *
   * @param aMsg {@link GenericMessage} - the message
   * @return {@link IStringMap}&lt;{@link ITimedList}&lt;{@link ITemporalAtomicValue}&gt;&gt; - the values map -
   *         argument extracted from the message
   * @throws TsUnsupportedFeatureRtException message is not of kind {@link EGwidKind#GW_RTDATA}
   */
  public IStringMap<ITimedList<ITemporalAtomicValue>> getAtomicValues( GenericMessage aMsg ) {
    TsUnsupportedFeatureRtException.checkTrue( getGwidKind( aMsg ) != EGwidKind.GW_RTDATA );
    String s = aMsg.args().getStr( ARGID_VALUES );
    IStringMapEdit<ITimedList<ITemporalAtomicValue>> map = new StringMap<>();
    IStrioReader sr = new StrioReader( new CharInputStreamString( s ) );
    if( sr.readArrayBegin() ) {
      do {
        String k = sr.readQuotedString();
        sr.ensureSeparatorChar();
        ITimedList<ITemporalAtomicValue> v = new TimedList<>( TemporalAtomicValueKeeper.KEEPER.read( sr ) );
        map.put( k, v );
      } while( sr.readArrayNext() );
    }
    return map;
  }

}
