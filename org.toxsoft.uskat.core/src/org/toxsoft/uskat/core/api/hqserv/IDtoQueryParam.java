package org.toxsoft.uskat.core.api.hqserv;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.temporal.*;
import org.toxsoft.core.tslib.bricks.filter.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.uskat.core.api.evserv.*;

/**
 * Single data argument to be queried by {@link ISkQueryProcessedData}.
 * <p>
 * Single data is retrieved as {@link ITimedList} of values (like {@link ITemporalAtomicValue} or {@link SkEvent}) over
 * the specified time interval and then processed. Result of processing is {@link ITimedList} usualy of the same value
 * types. However, some functions (like COUNT, see below) returns list {@link ITemporalAtomicValue}.
 * <p>
 * Data processing consist of two steps:
 * <ul>
 * <li><b>filtering</b> - some values from the initial {@link ITimedList} may be rejected by the filer. Filter is crated
 * based on specified parameters {@link #filterParams()}. For example, filter may reject/accept commands with the
 * specified argument, or atmic values if they are out of the range, etc.;</li>
 * <li>aplying an aggregating <b>function</b> - function {@link #funcId()} with arguments {@link #funcArgs()} will be
 * applyed to each filtered element. Most function are aggregating functions having fixed time periods called
 * aggregation interval. Each aggregation interval produces single element in resulting {@link ITimedList}. Aggregation
 * interval duration is specified as argument {@link ISkHistoryQueryServiceConstants#HQFUNC_ARG_AGGREGAION_INTERVAL}.
 * Specifying 0 as aggregation interval means to aggregate over whole queried time interval thus producing single value
 * in resulting list.</li>
 * </ul>
 * {@link ITsCombiFilterParams#ALL} as {@link #filterParams()} and an empty string as {@link #funcId()} produces the raw
 * data list of the specified {@link #dataGwid()}, thus implementiing {@link ISkQueryRawHistory}.
 *
 * @author hazard157
 */
public interface IDtoQueryParam {

  /**
   * The GWID of data to be queried.
   * <p>
   * Only GWIDs of kind {@link EGwidKind#GW_RTDATA}, {@link EGwidKind#GW_CMD} and {@link EGwidKind#GW_EVENT} are
   * allowed.
   *
   * @return {@link Gwid} - the concrete single (non-multi) GWID of allowed kind
   */
  Gwid dataGwid();

  /**
   * Parameters of the filter to by used for values filtering.
   * <p>
   * Cretad filter must accept elemnts of type, depending on {@link #dataGwid()}. For example, for
   * {@link EGwidKind#GW_EVENT} filter must accept elements of type {@link SkEvent}.
   * <p>
   * If no filtering is used method returns {@link ITsCombiFilterParams#ALL}.
   *
   * @return {@link ITsCombiFilterParams} - parameters for filter creation or {@link ITsCombiFilterParams#ALL}
   */
  ITsCombiFilterParams filterParams();

  /**
   * The ID of function to be used for data processing.
   * <p>
   * Function may be either aggregating or of any other kind. There are some builtin functions listed in
   * {@link ISkHistoryQueryServiceConstants}. Other functions may be application-specific.
   * <p>
   * An empty string means that no function will be used.
   *
   * @return String - function ID (an IDpath) or an empty string for no function processing
   */
  String funcId();

  /**
   * Arguments of function {@link #funcId()}.
   * <p>
   *
   * @return {@link IOptionSet} - function arguments
   */
  IOptionSet funcArgs();

}
