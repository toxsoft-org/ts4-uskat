package org.toxsoft.uskat.core.api.hqserv;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * The query service.
 * <p>
 * Query creation methods <code>createXxxQuery(IOptionSet)</code> accepts creation options. Options values are listed in
 * {@link ISkQueryServiceConstants}. Also backend implementation may define specific options described somwhere else.
 * User-specific options also may be supplied and read back in {@link ISkHistoryQuery#params()}.
 *
 * @author hazard157
 */
public interface ISkQueryService {

  /**
   * Creates an empty query to historical data.
   *
   * @param aOptions {@link IOptionSet} - optional query execution parameters
   * @return {@link ISkHistoryQuery} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ISkHistoryQuery createHistoricQuery( IOptionSet aOptions );

  /**
   * Creates an empty query to processed data.
   * <p>
   * FIXME as of 04/07/2022 this API is under development and has no implementation !
   *
   * @param aOptions {@link IOptionSet} - optional query execution parameters
   * @return {@link ISkProcessedQuery} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  default ISkProcessedQuery createProcessedQuery( IOptionSet aOptions ) {
    // TODO реализовать ISkQueryService.createProcessedQuery()
    throw new TsUnderDevelopmentRtException( "ISkQueryService.createProcessedQuery()" );
  }

  /**
   * Creates an empty query to processed data.
   * <p>
   * FIXME as of 04/07/2022 this API is under development and has no implementation !
   *
   * @param aOptions {@link IOptionSet} - optional query execution parameters
   * @return {@link ISkLanguageQuery} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  default ISkLanguageQuery createLanguageQuery( IOptionSet aOptions ) {
    // TODO реализовать ISkQueryService.createLanguageQuery()
    throw new TsUnderDevelopmentRtException( "ISkQueryService.createLanguageQuery()" );
  }

  /**
   * Returns currently open queries.
   * <p>
   * As soon as {@link ISkAsynchronousQuery#close()} is called query disappears from his list.
   *
   * @return {@link IStringMap}&lt;{@link ISkAsynchronousQuery}&gt; - map "query instance ID" - "the query"
   */
  IStringMap<ISkAsynchronousQuery> openQueries();

}
