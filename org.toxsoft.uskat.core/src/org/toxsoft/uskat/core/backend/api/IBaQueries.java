package org.toxsoft.uskat.core.backend.api;

import static org.toxsoft.uskat.core.ISkHardConstants.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.hqserv.*;

/**
 * Backend addon for queries execution.
 * <p>
 * This is the mandatory addon.
 *
 * @author hazard157
 */
public interface IBaQueries
    extends IBackendAddon {

  /**
   * ID of this backend addon.
   */
  String ADDON_ID = SK_ID + "ba.Queries"; //$NON-NLS-1$

  /**
   * Starts historic data query and returns immediately
   * <p>
   * <code>aGwids</code> may contain only concrete valid GWIDs of kind {@link EGwidKind#GW_RTDATA},
   * {@link EGwidKind#GW_CMD} and {@link EGwidKind#GW_EVENT} without duplicates.
   * <p>
   * <code>aParams</code> is query options as defined in {@link ISkQueryService#createHistoricQuery(IOptionSet)}.
   * <p>
   * After this method backend starts query execution and sends {@link BaMsgQueriesNextData} with portions of data until
   * last portio marked with option {@link BaMsgQueriesNextData#ARGID_IS_FINISHED} set to <code>true</code>. Query
   * execution also will be finished after {@link #cancelQuery(String)}.
   *
   * @param aQueryId String - query ID (an IDpath) is unique while backend is running
   * @param aQueryInterval {@link IQueryInterval} - asked interval of time
   * @param aGwids {@link IGwidList} - valid GWIDs list
   * @param aParams {@link IOptionSet} - query options
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemAlreadyExistsRtException query with specified ID is already started and still executing
   * @throws TsIllegalArgumentRtException invalid GWIDs encountered
   */
  void execHistoricQuery( String aQueryId, IQueryInterval aQueryInterval, IGwidList aGwids, IOptionSet aParams );

  /**
   * Cancels one of the executing query started with
   * {@link #execHistoricQuery(String, IQueryInterval, IGwidList, IOptionSet)}.
   * <p>
   * After this call fronted has to wait while message with set option {@link BaMsgQueriesNextData#ARGID_IS_FINISHED} is
   * received.
   * <p>
   * If no query with specified ID is executing than method does nothing.
   *
   * @param aQueryId String - the query ID
   */
  void cancelQuery( String aQueryId );

}
