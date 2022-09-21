package org.toxsoft.uskat.core.backend.api;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;

import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.av.temporal.TemporalAtomicValueKeeper;
import org.toxsoft.core.tslib.bricks.events.msg.GenericMessage;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.gwid.EGwidKind;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsUnsupportedFeatureRtException;
import org.toxsoft.uskat.core.api.cmdserv.IDtoCompletedCommand;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
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

  private static final String ARGID_IS_FINISHED = "isFinished"; //$NON-NLS-1$
  private static final String ARGID_DATA_ID     = "dataId";     //$NON-NLS-1$
  private static final String ARGID_KIND        = "kind";       //$NON-NLS-1$
  private static final String ARGID_VALUES_LIST = "valuesList"; //$NON-NLS-1$

  BaMsgQueryNextData() {
    super( ISkHistoryQueryService.SERVICE_ID, MSG_ID );
    defineArgNonValobj( ARGID_DATA_ID, STRING, true );
    defineArgValobj( ARGID_KIND, EGwidKind.KEEPER_ID, true );
    defineArgNonValobj( ARGID_VALUES_LIST, STRING, true );
    defineArgNonValobj( ARGID_IS_FINISHED, BOOLEAN, false, TSID_DEFAULT_VALUE, AV_FALSE );
  }

  /**
   * Creates the message instance of kind {@link EGwidKind#GW_RTDATA}.
   *
   * @param aDataId String - the data ID
   * @param aValues {@link ITimedList}&lt;{@link ITemporalAtomicValue}&gt; - the values
   * @param aFinished boolean - finished flag
   * @return {@link GtMessage} - created instance of the message
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public GtMessage makeMessageRtdata( String aDataId, ITimedList<ITemporalAtomicValue> aValues, boolean aFinished ) {
    return makeMessageVarargs( //
        ARGID_DATA_ID, avStr( aDataId ), //
        ARGID_KIND, avValobj( EGwidKind.GW_RTDATA ), //
        ARGID_VALUES_LIST, TemporalAtomicValueKeeper.KEEPER.coll2str( aValues ), //
        ARGID_IS_FINISHED, avBool( aFinished ) //
    );
  }

  /**
   * Creates the message instance of kind {@link EGwidKind#GW_EVENT}.
   *
   * @param aDataId String - the data ID
   * @param aValues {@link ITimedList}&lt;{@link SkEvent}&gt; - the events
   * @param aFinished boolean - finished flag
   * @return {@link GtMessage} - created instance of the message
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public GtMessage makeMessageEvents( String aDataId, ITimedList<SkEvent> aValues, boolean aFinished ) {
    return makeMessageVarargs( //
        ARGID_DATA_ID, avStr( aDataId ), //
        ARGID_KIND, avValobj( EGwidKind.GW_EVENT ), //
        ARGID_VALUES_LIST, SkEvent.KEEPER.coll2str( aValues ), //
        ARGID_IS_FINISHED, avBool( aFinished ) //
    );
  }

  /**
   * Creates the message instance of kind {@link EGwidKind#GW_CMD}.
   *
   * @param aDataId String - the data ID
   * @param aValues {@link ITimedList}&lt;{@link IDtoCompletedCommand}&gt; - the commands
   * @param aFinished boolean - finished flag
   * @return {@link GtMessage} - created instance of the message
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public GtMessage makeMessageCmds( String aDataId, ITimedList<IDtoCompletedCommand> aValues, boolean aFinished ) {
    return makeMessageVarargs( //
        ARGID_DATA_ID, avStr( aDataId ), //
        ARGID_KIND, avValobj( EGwidKind.GW_EVENT ), //
        ARGID_VALUES_LIST, DtoCompletedCommand.KEEPER.coll2str( aValues ), //
        ARGID_IS_FINISHED, avBool( aFinished ) //
    );
  }

  /**
   * Extracts data ID argument from the message.
   *
   * @param aMsg {@link GenericMessage} - the message
   * @return String - argument extracted from the message
   */
  public String getDataId( GenericMessage aMsg ) {
    return getArg( aMsg, ARGID_DATA_ID ).asString();
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
   * Extracts RTdata values argument from the message.
   *
   * @param aMsg {@link GenericMessage} - the message
   * @return {@link IList}&lt;{@link ITemporalAtomicValue}&gt; - argument extracted from the message
   * @throws TsUnsupportedFeatureRtException message is not of kind {@link EGwidKind#GW_RTDATA}
   */
  public IList<ITemporalAtomicValue> getRtdataValues( GenericMessage aMsg ) {
    TsUnsupportedFeatureRtException.checkTrue( getGwidKind( aMsg ) != EGwidKind.GW_RTDATA );
    String s = aMsg.args().getStr( ARGID_VALUES_LIST );
    return TemporalAtomicValueKeeper.KEEPER.str2coll( s );
  }

  /**
   * Extracts events list argument from the message.
   *
   * @param aMsg {@link GenericMessage} - the message
   * @return {@link IList}&lt;{@link SkEvent}&gt; - argument extracted from the message
   * @throws TsUnsupportedFeatureRtException message is not of kind {@link EGwidKind#GW_EVENT}
   */
  public IList<SkEvent> getEventsList( GenericMessage aMsg ) {
    TsUnsupportedFeatureRtException.checkTrue( getGwidKind( aMsg ) != EGwidKind.GW_EVENT );
    String s = aMsg.args().getStr( ARGID_VALUES_LIST );
    return SkEvent.KEEPER.str2coll( s );
  }

  /**
   * Extracts commands list argument from the message.
   *
   * @param aMsg {@link GenericMessage} - the message
   * @return {@link IList}&lt;{@link IDtoCompletedCommand}&gt; - argument extracted from the message
   * @throws TsUnsupportedFeatureRtException message is not of kind {@link EGwidKind#GW_CMD}
   */
  public IList<IDtoCompletedCommand> getCmdsList( GenericMessage aMsg ) {
    TsUnsupportedFeatureRtException.checkTrue( getGwidKind( aMsg ) != EGwidKind.GW_CMD );
    String s = aMsg.args().getStr( ARGID_VALUES_LIST );
    return DtoCompletedCommand.KEEPER.str2coll( s );
  }

}
