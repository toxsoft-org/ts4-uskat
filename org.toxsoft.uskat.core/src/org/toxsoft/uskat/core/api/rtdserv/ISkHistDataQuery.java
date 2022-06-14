package org.toxsoft.uskat.core.api.rtdserv;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.temporal.*;
import org.toxsoft.core.tslib.bricks.events.change.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;

/**
 * Query to the historic (strored) data.
 * <p>
 * Usage:
 * <ol>
 * <li>create instance of the query by method {@link ISkRtdataService#createQuery(IOptionSet)};</li>
 * <li>specifiy the GWIDs of data to quety - {@link #prepare(IGwidList)};</li>
 * <li>start query execution - {@link #exec(IQueryInterval)};</li>
 * <li>wait while query ends either by polling {@link #state()} or listening to {@link #genericChangeEventer()};</li>
 * <li>opionally one may {@link #cancel()} executing query;</li>
 * <li>get and process {@link #result()} data;</li>
 * <li>{@link #close()} unneeded query to release resources.</li>
 * </ol>
 * Steps 2-6 or 3-6 may be repetead.
 * <p>
 * Every time when {@link #state()} is changed event {@link IGenericChangeListener#onGenericChangeEvent(Object)} is
 * fired.
 *
 * @author hazard157
 */
public interface ISkHistDataQuery
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
   * Prepares the query.
   * <p>
   * Query preparation includes at list GWIDs analysis and optional actions on the server side (if applicable). Only
   * concrete GWIDs of kind {@link EGwidKind#GW_RTDATA} are considered as appropriate. All other GWIDs, including but
   * not limited to unexisting GWIDs, duplicate GWIDs, non-historic RTdata, etc are ignored. Multi-GWIDs are expanded.
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
  IGwidList prepare( IGwidList aGwids );

  /**
   * Starts query execution and returns immediately.
   * <p>
   * Mathod shall not ba called when {@link #state()} is {@link EQueryState#UNPREPARED}, {@link EQueryState#EXECUTING}
   * or {@link EQueryState#CLOSED}.
   *
   * @param aQueryInterval {@link IQueryInterval} - requested time interval
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalStateRtException illegal {@link #state()}
   */
  void exec( IQueryInterval aQueryInterval );

  /**
   * Cancels the executing query.
   * <p>
   * Methor call in any state except {@link EQueryState#EXECUTING} does nothing.
   */
  void cancel();

  /**
   * Returns the query data result.
   * <p>
   * In any {@link #state()} except {@link EQueryState#READY} returns an empty map.
   * <p>
   * Query result is a map. For each GWID key of RTdata list of values are stored. Values as {@link ITimedList} are
   * sorted by the time increase.
   * <p>
   * Result is totally stored in memory so calling this method does not lead to any performance penalty.
   *
   * @return {@link IMap}&lt;{@link Gwid},{@link ITimedList}&lt;{@link ITemporalAtomicValue}&gt;&gt; - query result
   */
  IMap<Gwid, ITimedList<ITemporalAtomicValue>> result();

}
