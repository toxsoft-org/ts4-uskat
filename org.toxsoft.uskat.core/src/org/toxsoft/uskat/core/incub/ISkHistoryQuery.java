package org.toxsoft.uskat.core.incub;

import org.toxsoft.core.tslib.bricks.events.change.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.rtdserv.*;

/**
 * The asynchronous query.
 *
 * @author hazard157
 * @param <T> - queries temporal value type
 */
public interface ISkHistoryQuery<T extends ITemporalValue<T>>
    extends IGenericChangeEventCapable, ICloseable {

  /**
   * Returns an unique query instance identifier.
   * <p>
   * This ID is unique among all queries of all time in the particular system.
   * <p>
   * Query ID is not used by {@link ISkCoreApi} directly however it is useful for some other maintenance service.
   *
   * @return String - identifier (an IDpath) of the query instance
   */
  String queryId();

  /**
   * Returns current query state.
   *
   * @return {@link EQueryState} - the query state
   */
  EQueryState state();

  /**
   * returns GWID specified in method {@link #setGwids(IGwidList)}.
   *
   * @return {@link IGwidList} - requested GWIDs
   */
  IGwidList listGwids();

  /**
   * Prepares the query.
   * <p>
   * Query preparation includes at list GWIDs analysis and optional actions on the server side (if applicable). Only
   * concrete GWIDs of kind {@link EGwidKind#GW_RTDATA}
   * <p>
   * Commands? Events?
   * <p>
   * are considered as appropriate. All other GWIDs, including but not limited to unexisting GWIDs, duplicate GWIDs,
   * non-historic RTdata, etc are ignored. Multi-GWIDs are expanded.
   * <p>
   * Method returns the result of analysis - the list of GWIDs to be processed. This list will be the same as the keys
   * of the {@link #result()} map. All multi-GWIDs will be expanded so result sontains only <b>concrete single</b> GWIDs
   * (that is {@link Gwid#isMulti()} = <code>false</code>) of kind {@link EGwidKind#GW_RTDATA}.
   * <p>
   * Mathod shall not ba called when {@link #state()} is {@link EQueryState#EXECUTING} or {@link EQueryState#CLOSED}.
   *
   * @param aGwids {@link IGwidList} - RTdata GWIDs user ask for query
   * @return {@link IGwidList} - GWIDs to be queried in fact
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalStateRtException illegal {@link #state()}
   */
  void setGwids( IGwidList aList );

  /**
   * Queries the data for the specified time interval.
   *
   * @param aInterval {@link IQueryInterval} - asked interval of time
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  void exec( IQueryInterval aInterval );

  /**
   * Cancel the executng query.
   * <p>
   * Has no effect is query is not in state of execution.
   */
  void cancel();

  /**
   * Returns result of query for specified RTdata.
   *
   * @param aGwid {@link Gwid} - data GWID
   * @return {@link ITimedList}&lt;T&gt; - data sequence
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException not such GWID in the result
   */
  ITimedList<T> get( Gwid aGwid );

  /**
   * Returns all results of the quesry at once.
   *
   * @return {@link IMap}&lt;{@link Gwid},{@link ITimedList}&gt; - map "data GWID" - "data sequence"
   */
  IMap<Gwid, ITimedList<T>> getAll();

}
