package org.toxsoft.uskat.core.api.hqserv;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.*;

/**
 * The query service.
 * <p>
 * Query creation methods <code>createXxxQuery(IOptionSet)</code> accepts creation options. Options values are listed in
 * {@link ISkHistoryQueryServiceConstants}. Also backend implementation may define specific options described somwhere
 * else. User-specific options also may be supplied and read back in {@link ISkQueryRawHistory#params()}.
 *
 * @author hazard157
 */
public interface ISkHistoryQueryService
    extends ISkService {

  /**
   * Service identifier.
   */
  String SERVICE_ID = ISkHardConstants.SK_CORE_SERVICE_ID_PREFIX + ".HistQuery"; //$NON-NLS-1$

  /**
   * Creates an empty query to historical data.
   *
   * @param aOptions {@link IOptionSet} - optional query execution parameters
   * @return {@link ISkQueryRawHistory} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ISkQueryRawHistory createHistoricQuery( IOptionSet aOptions );

  /**
   * Creates an empty query to processed data.
   *
   * @param aOptions {@link IOptionSet} - optional query execution parameters
   * @return {@link ISkQueryProcessedData} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ISkQueryProcessedData createProcessedQuery( IOptionSet aOptions );

  /**
   * Creates an empty query to processed data.
   * <p>
   * FIXME as of 04/07/2022 this API is under development and has no implementation !
   *
   * @param aOptions {@link IOptionSet} - optional query execution parameters
   * @return {@link ISkQueryStatement} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  // TODO ISkQueryStatement createLanguageQuery( IOptionSet aOptions );

  /**
   * Returns currently open queries.
   * <p>
   * As soon as {@link ISkAsynchronousQuery#close()} is called query disappears from his list.
   *
   * @return {@link IStringMap}&lt;{@link ISkAsynchronousQuery}&gt; - map "query instance ID" - "the query"
   */
  IStringMap<ISkAsynchronousQuery> listOpenQueries();

}
