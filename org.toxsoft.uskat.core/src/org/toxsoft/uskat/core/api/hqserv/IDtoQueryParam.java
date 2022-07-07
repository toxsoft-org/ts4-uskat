package org.toxsoft.uskat.core.api.hqserv;

import java.io.*;

import org.toxsoft.core.tslib.bricks.filter.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.gw.gwid.*;

/**
 * Single data argumetn to be queried by {@link ISkQueryProcessedData}.
 * <p>
 * Implementa {@link IStridableParameterized} where {@link #id()} is user-specified data ID (unique in query). Visal
 * {@link #nmName()} and {@link #description()} is not used by query itself but may be specified for example to be used
 * in manual query creation GUI.
 * <p>
 * FIXME as of 04/07/2022 this API is under development and has no implementation !
 *
 * @author hazard157
 */
public interface IDtoQueryParam
    extends IStridable {

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
   * FIXME the api of the data processing rules must be developed.
   * <p>
   * In ts3 uskat there were was EReportFunc enum (like MIN/MAX/COUNT/AVE etc) with IOptionSet of additional parameters
   * like aggregation time interval.
   * <p>
   * In ts4 usakt must be added the ability for user-specified rules in addition with standard MIN/MAX/COUNT/AVE/....
   * <p>
   * At least the following solutions shall be considered:
   * <ul>
   * <li>enum like EReprtFunc with USER_SPECIFIED choice (see below about how to USER-SPECIFY);</li>
   * <li>user-specified rules as filter {@link ITsCombiFilterParams};</li>
   * <li>specify reules as JavaScript code;</li>
   * <li>introduce {@link Serializable} class AbstractDataProcessor and send subclasses to the server for processing in
   * server context;</li>
   * <li>develop SkatQL (the USkat Query Language) and use it's framnets as argument processing rules.</li>
   * </ul>
   *
   * @return {@link Object} - processing rule to be developed
   */
  Object methodMustReturnProcessingRule();

}
