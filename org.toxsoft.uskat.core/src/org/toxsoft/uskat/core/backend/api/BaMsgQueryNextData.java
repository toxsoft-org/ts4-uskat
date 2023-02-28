package org.toxsoft.uskat.core.backend.api;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
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
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedBundleList;
import org.toxsoft.core.tslib.coll.impl.TsCollectionsUtils;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.gwid.EGwidKind;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsUnsupportedFeatureRtException;
import org.toxsoft.uskat.core.api.cmdserv.IDtoCompletedCommand;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.core.api.hqserv.ESkQueryState;
import org.toxsoft.uskat.core.api.hqserv.ISkHistoryQueryService;
import org.toxsoft.uskat.core.impl.dto.DtoCompletedCommand;

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

  private static final String ARGID_STATE         = "state";    //$NON-NLS-1$
  private static final String ARGID_STATE_MESSAGE = "stateMsg"; //$NON-NLS-1$
  private static final String ARGID_QUERY_ID      = "queryId";  //$NON-NLS-1$
  private static final String ARGID_KIND          = "kind";     //$NON-NLS-1$
  private static final String ARGID_VALUES        = "values";   //$NON-NLS-1$

  BaMsgQueryNextData() {
    super( ISkHistoryQueryService.SERVICE_ID, MSG_ID );
    defineArgNonValobj( ARGID_QUERY_ID, STRING, true );
    defineArgValobj( ARGID_KIND, EGwidKind.KEEPER_ID, true );
    defineArgNonValobj( ARGID_VALUES, STRING, true );
    defineArgNonValobj( ARGID_STATE, STRING, true );
    defineArgNonValobj( ARGID_STATE_MESSAGE, STRING, true );
  }

  /**
   * Creates the message instance of kind {@link EGwidKind#GW_RTDATA}.
   *
   * @param aQueryId String - the query ID
   * @param aValues {@link IStringMap}&lt;{@link IList}&lt;{@link ITemporalAtomicValue}&gt;&gt; - the values map
   * @param aState {@link ESkQueryState} - current query state
   * @param aStateMessage String - current query state message
   * @return {@link GtMessage} - created instance of the message
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public GtMessage makeMessageAtomicValues( String aQueryId, IStringMap<IList<ITemporalAtomicValue>> aValues,
      ESkQueryState aState, String aStateMessage ) {
    StringBuilder sb = new StringBuilder();
    IStrioWriter sw = new StrioWriter( new CharOutputStreamAppendable( sb ) );
    sw.writeChar( CHAR_ARRAY_BEGIN );
    int i = 0, n = aValues.size();
    for( String k : aValues.keys() ) {
      IList<ITemporalAtomicValue> v = aValues.values().get( i );
      sw.writeQuotedString( k );
      sw.writeSeparatorChar();
      sw.writeInt( v.size() );
      sw.writeSeparatorChar();
      TemporalAtomicValueKeeper.KEEPER.writeColl( sw, v, false ); // aIndented = false
      if( i < n - 1 ) {
        sw.writeSeparatorChar();
      }
      ++i;
    }
    sw.writeChar( CHAR_ARRAY_END );
    return makeMessageVarargs( //
        ARGID_QUERY_ID, avStr( aQueryId ), //
        ARGID_KIND, avValobj( EGwidKind.GW_RTDATA ), //
        ARGID_VALUES, sb.toString(), //
        ARGID_STATE, avValobj( aState ), //
        ARGID_STATE_MESSAGE, avStr( aStateMessage ) //
    );
  }

  /**
   * Creates the message instance of kind {@link EGwidKind#GW_EVENT}.
   *
   * @param aQueryId String - the query ID
   * @param aValues {@link IStringMap}&lt;{@link IList}&lt;{@link SkEvent}&gt;&gt; - the events map
   * @param aState {@link ESkQueryState} - current query state
   * @param aStateMessage String - current query state message
   * @return {@link GtMessage} - created instance of the message
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public GtMessage makeMessageEvents( String aQueryId, IStringMap<IList<SkEvent>> aValues, ESkQueryState aState,
      String aStateMessage ) {
    StringBuilder sb = new StringBuilder();
    IStrioWriter sw = new StrioWriter( new CharOutputStreamAppendable( sb ) );
    sw.writeChar( CHAR_ARRAY_BEGIN );
    int i = 0, n = aValues.size();
    for( String k : aValues.keys() ) {
      IList<SkEvent> v = aValues.values().get( i );
      sw.writeQuotedString( k );
      sw.writeSeparatorChar();
      sw.writeInt( v.size() );
      sw.writeSeparatorChar();
      SkEvent.KEEPER.writeColl( sw, v, false ); // aIndented = false
      if( i < n - 1 ) {
        sw.writeSeparatorChar();
      }
      ++i;
    }
    sw.writeChar( CHAR_ARRAY_END );
    return makeMessageVarargs( //
        ARGID_QUERY_ID, avStr( aQueryId ), //
        ARGID_KIND, avValobj( EGwidKind.GW_EVENT ), //
        ARGID_VALUES, sb.toString(), //
        ARGID_STATE, avValobj( aState ), //
        ARGID_STATE_MESSAGE, avStr( aStateMessage ) //
    );
  }

  /**
   * Creates the message instance of kind {@link EGwidKind#GW_CMD}.
   *
   * @param aQueryId String - the query ID
   * @param aValues {@link IStringMap}&lt;{@link IList}&lt;{@link IDtoCompletedCommand}&gt;&gt; - the commands map
   * @param aState {@link ESkQueryState} - current query state
   * @param aStateMessage String - current query state message
   * @return {@link GtMessage} - created instance of the message
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public GtMessage makeMessageCommands( String aQueryId, IStringMap<IList<IDtoCompletedCommand>> aValues,
      ESkQueryState aState, String aStateMessage ) {
    StringBuilder sb = new StringBuilder();
    IStrioWriter sw = new StrioWriter( new CharOutputStreamAppendable( sb ) );
    sw.writeChar( CHAR_ARRAY_BEGIN );
    int i = 0, n = aValues.size();
    for( String k : aValues.keys() ) {
      IList<IDtoCompletedCommand> v = aValues.values().get( i );
      sw.writeQuotedString( k );
      sw.writeSeparatorChar();
      sw.writeInt( v.size() );
      sw.writeSeparatorChar();
      DtoCompletedCommand.KEEPER.writeColl( sw, v, false ); // aIndented = false
      if( i < n - 1 ) {
        sw.writeSeparatorChar();
      }
      ++i;
    }
    sw.writeChar( CHAR_ARRAY_END );
    return makeMessageVarargs( //
        ARGID_QUERY_ID, avStr( aQueryId ), //
        ARGID_KIND, avValobj( EGwidKind.GW_CMD ), //
        ARGID_VALUES, sb.toString(), //
        ARGID_STATE, avValobj( aState ), //
        ARGID_STATE_MESSAGE, avStr( aStateMessage ) //
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
   * Extracts is state argument from the message.
   *
   * @param aMsg {@link GenericMessage} - the message
   * @return {@link ESkQueryState} - argument extracted from the message
   */
  public ESkQueryState getState( GenericMessage aMsg ) {
    return getArg( aMsg, ARGID_STATE ).asValobj();
  }

  /**
   * Extracts is state message argument from the message.
   *
   * @param aMsg {@link GenericMessage} - the message
   * @return String - argument extracted from the message
   */
  public String getStateMessage( GenericMessage aMsg ) {
    return getArg( aMsg, ARGID_STATE_MESSAGE ).asString();
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
        int bundleCapacity = getBundleCapacity( sr.readInt() );
        sr.ensureSeparatorChar();
        map.put( k, TemporalAtomicValueKeeper.KEEPER.readColl( sr, new TimedList<>( bundleCapacity ) ) );
      } while( sr.readArrayNext() );
    }
    return map;
  }

  /**
   * Extracts events map argument from the message.
   *
   * @param aMsg {@link GenericMessage} - the message
   * @return {@link IStringMap}&lt;{@link ITimedList}&lt;{@link SkEvent}&gt;&gt; - the events map - argument extracted
   *         from the message
   * @throws TsUnsupportedFeatureRtException message is not of kind {@link EGwidKind#GW_EVENT}
   */
  public IStringMap<ITimedList<SkEvent>> getEvents( GenericMessage aMsg ) {
    TsUnsupportedFeatureRtException.checkTrue( getGwidKind( aMsg ) != EGwidKind.GW_EVENT );
    String s = aMsg.args().getStr( ARGID_VALUES );
    IStringMapEdit<ITimedList<SkEvent>> map = new StringMap<>();
    IStrioReader sr = new StrioReader( new CharInputStreamString( s ) );
    if( sr.readArrayBegin() ) {
      do {
        String k = sr.readQuotedString();
        sr.ensureSeparatorChar();
        int bundleCapacity = getBundleCapacity( sr.readInt() );
        sr.ensureSeparatorChar();
        map.put( k, SkEvent.KEEPER.readColl( sr, new TimedList<>( bundleCapacity ) ) );
      } while( sr.readArrayNext() );
    }
    return map;
  }

  /**
   * Extracts commands map argument from the message.
   *
   * @param aMsg {@link GenericMessage} - the message
   * @return {@link IStringMap}&lt;{@link ITimedList}&lt;{@link IDtoCompletedCommand}&gt;&gt; - the events map -
   *         argument extracted from the message
   * @throws TsUnsupportedFeatureRtException message is not of kind {@link EGwidKind#GW_CMD}
   */
  public IStringMap<ITimedList<IDtoCompletedCommand>> getCommands( GenericMessage aMsg ) {
    TsUnsupportedFeatureRtException.checkTrue( getGwidKind( aMsg ) != EGwidKind.GW_CMD );
    String s = aMsg.args().getStr( ARGID_VALUES );
    IStringMapEdit<ITimedList<IDtoCompletedCommand>> map = new StringMap<>();
    IStrioReader sr = new StrioReader( new CharInputStreamString( s ) );
    if( sr.readArrayBegin() ) {
      do {
        String k = sr.readQuotedString();
        sr.ensureSeparatorChar();
        int bundleCapacity = getBundleCapacity( sr.readInt() );
        sr.ensureSeparatorChar();
        map.put( k, DtoCompletedCommand.KEEPER.readColl( sr, new TimedList<>( bundleCapacity ) ) );
      } while( sr.readArrayNext() );
    }
    return map;
  }

  /**
   * Проводит расчет максимального размера фрагмента (bundle capacity) для коллекций {@link ElemLinkedBundleList} для
   * эффективного доступа по индексу.
   *
   * @param aCollectionSize int размер коллекции
   * @return int размер фрагмента
   */
  public static int getBundleCapacity( int aCollectionSize ) {
    return Math.max( TsCollectionsUtils.MIN_BUNDLE_CAPACITY,
        Math.min( TsCollectionsUtils.MAX_BUNDLE_CAPACITY, aCollectionSize ) );
  }

}
