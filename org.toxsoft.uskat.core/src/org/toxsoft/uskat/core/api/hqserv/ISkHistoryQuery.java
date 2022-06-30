package org.toxsoft.uskat.core.api.hqserv;

import org.toxsoft.core.tslib.bricks.events.change.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.gwids.*;

/**
 * The asynchronous query.
 *
 * @author hazard157
 */
public interface ISkHistoryQuery
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
   * returns GWID specified in method {@link #prepare(IGwidList)}.
   *
   * @return {@link IGwidList} - requested GWIDs
   */
  IGwidList listGwids();

  /**
   * Prepares the query.
   * <p>
   * Query preparation includes at least GWIDs analysis and optional actions on the server side (if applicable).
   * <p>
   * Query accepts GWIDs of following kind:
   * <ul>
   * <li>{@link EGwidKind#GW_RTDATA} - returns historical RTdata;</li>
   * <li>{@link EGwidKind#GW_CMD} - returns history of the commands;</li>
   * <li>{@link EGwidKind#GW_EVENT} - returns history of events.</li>
   * </ul>
   * <p>
   * Invalid GWIDs are silently ignored. Non-existing entities GWIDs, or GWIDs of non-historical RTdata are examples of
   * invalid GWIDs,
   * <p>
   * Multi GWIDs are acceped and expanded as {@link ISkGwidService#expandGwid(Gwid)}. Method returns the result of
   * analysis - the list of GWIDs to be processed. This list will be the same as the keys of the {@link #getAll()} map.
   * All multi-GWIDs will be expanded so result contains only <b>concrete single</b> GWIDs (that is
   * {@link Gwid#isMulti()} = <code>false</code>).
   * <p>
   * Mathod shall not ba called when {@link #state()} is {@link EQueryState#EXECUTING} or {@link EQueryState#CLOSED}.
   *
   * @param aGwids {@link IGwidList} - RTdata GWIDs user ask for query
   * @return {@link IGwidList} - GWIDs to be queried in fact
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalStateRtException illegal {@link #state()}
   */
  IGwidList prepare( IGwidList aGwids );

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
   * @param <T> - expected type of the temporal value
   * @param aGwid {@link Gwid} - data GWID
   * @return {@link ITimedList}&lt;T&gt; - data sequence
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException not such GWID in the result
   */
  <T extends ITemporalValue<T>> ITimedList<T> get( Gwid aGwid );

  /**
   * Returns all results of the query at once.
   * <p>
   * Map keys is the same list of GWIDs as returned by {@link #prepare(IGwidList)}.
   *
   * @param <T> - expected type of the temporal value
   * @return {@link IMap}&lt;{@link Gwid},{@link ITimedList}&gt; - map "data GWID" - "data sequence"
   */
  <T extends ITemporalValue<T>> IMap<Gwid, ITimedList<T>> getAll();

}
