package org.toxsoft.uskat.core.backend.api;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.uskat.core.api.hqserv.*;
import org.toxsoft.uskat.core.backend.*;

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
  String ADDON_ID = ISkBackendHardConstant.BAID_QUERIES;

  /**
   * Creates data query.
   * <p>
   * This method handles both {@link ISkQueryRawHistory} and {@link ISkQueryProcessedData}.
   *
   * @param aParams {@link IOptionSet} - the query creation parameters
   * @return String - the query instance ID
   */
  String createQuery( IOptionSet aParams );

  /**
   * Prepares query to be executed.
   *
   * @param aQueryId String - the query ID
   * @param aParams {@link IStringMap}&lt;{@link IDtoQueryParam}&gt; - data request parameters
   */
  void prepareQuery( String aQueryId, IStringMap<IDtoQueryParam> aParams );

  /**
   * Starts query/statement execution.
   * <p>
   * While executing, backend sends {@link BaMsgQueryNextData} messages to the frontend until query execution is
   * finished. Last message will contain flag {@link BaMsgQueryNextData#getIsFinished(GenericMessage)} set to
   * <code>true</code>.
   *
   * @param aQueryId String - the query ID
   * @param aTimeInterval {@link IQueryInterval} - requested time interval
   */
  void execQuery( String aQueryId, IQueryInterval aTimeInterval );

  // FIXME statment execution must be developed !
  // String createStatement( IOptionSet aParams );
  // Object prepareStatement( String aStatementId, String aSkatQl, IStringMap<IAtomicValue> aArgs );
  // void execStatement( String aStatementId );

  /**
   * Cancels the executing query/statement.
   * <p>
   * Does nothing if no such query is executing.
   * <p>
   * Cancelling query will resuslt backend message FOXME ???W
   *
   * @param aQueryId String - the query ID
   */
  void cancel( String aQueryId );

  /**
   * Closes the open query/statement.
   * <p>
   * Does nothing if no such query is open.
   *
   * @param aQueryId String - the query ID
   */
  void close( String aQueryId );

}
