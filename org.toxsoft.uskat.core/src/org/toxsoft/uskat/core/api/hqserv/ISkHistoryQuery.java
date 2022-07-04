package org.toxsoft.uskat.core.api.hqserv;

import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.gwids.*;

/**
 * Queries the "raw" history of data.
 * <p>
 * Query simply returns contents of the storage for every requested GWID for the specified time interval.
 *
 * @author hazard157
 */
public interface ISkHistoryQuery
    extends ISkAsynchronousQuery {

  // ------------------------------------------------------------------------------------
  // Query preparation
  //

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

  // ------------------------------------------------------------------------------------
  // Result getters
  //

  /**
   * Returns result of query for specified RTdata.
   * <p>
   * Note: while open, only query with {@link ISkHistoryQuery#state()} = {@link EQueryState#READY} contains data. All
   * other states leads to an empty result of this method. After {@link #close()} data (if there were any) will remain
   * in query instance and may be used.
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
   * <p>
   * Note: while open, only query with {@link ISkHistoryQuery#state()} = {@link EQueryState#READY} contains data. All
   * other states leads to an empty result of this method. After {@link #close()} data (if there were any) will remain
   * in query instance and may be used.
   *
   * @param <T> - expected type of the temporal value
   * @return {@link IMap}&lt;{@link Gwid},{@link ITimedList}&gt; - map "data GWID" - "data sequence"
   */
  <T extends ITemporalValue<T>> IMap<Gwid, ITimedList<T>> getAll();

}
