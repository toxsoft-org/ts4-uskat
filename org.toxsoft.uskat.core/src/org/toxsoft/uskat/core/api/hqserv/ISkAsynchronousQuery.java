package org.toxsoft.uskat.core.api.hqserv;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.utils.*;
import org.toxsoft.core.tslib.bricks.events.change.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;

/**
 * The asynchronous data query base interface.
 * <p>
 * Implements {@link IParameterized} and stores in {@link #params()} options used when creating query by
 * {@link ISkHistoryQueryService#createHistoricQuery(IOptionSet)}.
 * <p>
 * Fire {@link IGenericChangeListener#onGenericChangeEvent(Object)} events every time when {@link #state()} changes and
 * when next portion of data is received while in {@link ESkQueryState#EXECUTING} state.
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
   * @return {@link ESkQueryState} - the query state
   */
  ESkQueryState state();

  /**
   * Returns current query state message.
   *
   * @return String - message. May be is empty
   */
  String stateMessage();

  /**
   * Queries the data for the specified time interval.
   * <p>
   * Method shall not be called when {@link #state()} is {@link ESkQueryState#UNPREPARED},
   * {@link ESkQueryState#EXECUTING} or {@link ESkQueryState#CLOSED}.
   * <p>
   * When querying commands or events, open intervals {@link EQueryIntervalType#isStartOpen()} = <b>true</b> and/or
   * {@link EQueryIntervalType#isEndOpen()} = <b>true</b> ) are ignored and {@link EQueryIntervalType#CSCE} is used
   * instead.
   *
   * @param aInterval {@link IQueryInterval} - asked interval of time
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalStateRtException illegal {@link #state()}
   */
  void exec( IQueryInterval aInterval );

  /**
   * Cancel the executing query.
   * <p>
   * Has no effect is query is not in state of execution.
   */
  void cancel();

}
