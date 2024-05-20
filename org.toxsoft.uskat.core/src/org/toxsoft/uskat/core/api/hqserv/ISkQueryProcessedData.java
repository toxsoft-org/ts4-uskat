package org.toxsoft.uskat.core.api.hqserv;

import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Queries data and returns processed results.
 * <p>
 * Data GWIDs and processing rules are specified as query arguments {@link IDtoQueryParam}.
 * <p>
 * Processed data will be returned one-by-one. It is possible to use them even before whole set is ready. To check and
 * get single data result methods {@link #isArgDataReady(String)} and {@link #getArgData(String)} may be used.
 *
 * @author hazard157
 */
public interface ISkQueryProcessedData
    extends ISkAsynchronousQuery {

  // ------------------------------------------------------------------------------------
  // Query preparation
  //

  /**
   * Lists the argument as specified by {@link #prepare(IStringMap)}.
   *
   * @return {@link IStringMap}&lt;{@link IDtoQueryParam}&gt; - map "arg data ID" - "data parameter"
   */
  IStringMap<IDtoQueryParam> listArgs();

  /**
   * Prepares query to be executed.
   * <p>
   * Data ID is not necessarily to be an IDpath, any unique non-blank string is sufficient. For example,
   * {@link Gwid#toString()} may be used as argument data ID.
   *
   * @param aArgs {@link IStringMap}&lt;{@link IDtoQueryParam}&gt; - map "arg dtadata ID" - "data parameter"
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException something illegal found in query arguments
   */
  void prepare( IStringMap<IDtoQueryParam> aArgs );

  // ------------------------------------------------------------------------------------
  // Result getters
  //

  /**
   * Determines if result for the specified argument is ready.
   * <p>
   * This method is useful when query {@link #state()} is {@link ESkQueryState#EXECUTING EXECUTING}. When query becames
   * {@link ESkQueryState#READY READY} this method will return <code>true</code> for all arguments.
   *
   * @param aArgDataId String - the data argument ID as in {@link #listArgs()}
   * @return boolean - data is ready, {@link #getArgData(String)} may be called
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  boolean isArgDataReady( String aArgDataId );

  /**
   * Returns the result data corresponding to the single argument.
   *
   * @param <V> - expected data type
   * @param aArgDataId String - the data argument ID as in {@link #listArgs()}
   * @return {@link ITimedList}&lt;V&gt; - the result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no argument with the specified ID
   * @throws TsIllegalStateRtException this data is not ready yet
   */
  <V extends ITemporal<V>> ITimedList<V> getArgData( String aArgDataId );

}
