package org.toxsoft.uskat.core.api.hqserv;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Queries data and returns processed results.
 * <p>
 * Data GWIDs and processing rules are specified as query arguments {@link ISkProcessedQueryDataArg}.
 * <p>
 * Processed data will be returned one-by-one. It is possible to use them even before whole set is ready. To check and
 * get single data result methods {@link #isArgDataReady(String)} and {@link #getArgData(String)} may be used.
 * <p>
 * FIXME as of 04/07/2022 this API is under development and has no implementation !
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface ISkProcessedQuery
    extends ISkAsynchronousQuery {

  // ------------------------------------------------------------------------------------
  // Query preparation
  //

  /**
   * Listes the argument as specified by {@link #prepare(IStridablesList)}.
   *
   * @return {@link IStridablesList}&lt;{@link ISkProcessedQueryDataArg}&gt; - the query arguments
   */
  IStridablesList<ISkProcessedQueryDataArg> listArgs();

  /**
   * Prepares query to be executed.
   *
   * @param aArgs {@link IStridablesList}&lt;{@link ISkProcessedQueryDataArg}&gt; - the query arguments
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException somethiong illegal found in query arguments
   */
  void prepare( IStridablesList<ISkProcessedQueryDataArg> aArgs );

  // ------------------------------------------------------------------------------------
  // Result getters
  //

  /**
   * Determines if result for the specified argumetn is ready.
   * <p>
   * This method is useful when query {@link #state()} is {@link EQueryState#EXECUTING EXECUTING}. When query becames
   * {@link EQueryState#READY READY} this method will return <code>true</code> for all arguments.
   *
   * @param aDataId String - the data argument ID as in {@link #listArgs()}
   * @return boolean - data is ready, {@link #getArgData(String)} may be called
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  boolean isArgDataReady( String aArgId );

  /**
   * Returns the result data corresponding to the single argument.
   *
   * @param <V> - expected data type
   * @param aDataId String - the data argument ID as in {@link #listArgs()}
   * @return {@link ITimedList}&lt;V&gt; - the result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no argument with the specified ID
   * @throws TsIllegalStateRtException this data is not ready yet
   */
  <V extends ITemporal<V>> ITimedList<V> getArgData( String aDataId );

}
