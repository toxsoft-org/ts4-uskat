package org.toxsoft.uskat.core.api.hqserv;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.utils.*;
import org.toxsoft.core.tslib.bricks.events.change.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;

/**
 * The historic data asynchronous query base interface.
 * <p>
 * Implements {@link IParameterized} and stores in {@link #params()} options used when creating query by
 * {@link ISkQueryService#createHistoricQuery(IOptionSet)}.
 * <p>
 * Fire {@link IGenericChangeListener#onGenericChangeEvent(Object)} events every time when {@link #state()} changes and
 * when next portion of data is received while in {@link EQueryState#EXECUTING} state.
 *
 * @author hazard157
 */
public interface ISkAsynchronousQuery
    extends IGenericChangeEventCapable, ICloseable, IParameterized {

  /**
   * Returns an unique query identifier.
   * <p>
   * This ID is unique in context of open connection.
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
   * Queries the data for the specified time interval.
   * <p>
   * Mathod shall not ba called when {@link #state()} is {@link EQueryState#UNPREPARED}, {@link EQueryState#EXECUTING}
   * or {@link EQueryState#CLOSED}.
   *
   * @param aInterval {@link IQueryInterval} - asked interval of time
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalStateRtException illegal {@link #state()}
   */
  void exec( IQueryInterval aInterval );

  /**
   * Cancel the executng query.
   * <p>
   * Has no effect is query is not in state of execution.
   */
  void cancel();

}
