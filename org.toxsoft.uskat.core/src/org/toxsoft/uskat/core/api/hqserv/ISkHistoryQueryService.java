package org.toxsoft.uskat.core.api.hqserv;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * @author hazard157
 */
public interface ISkHistoryQueryService {

  /**
   * Creates an empty query to historical data.
   * <p>
   * Options values are listed in {@link ISkHistoryQueryServiceConstants}. Also backend implementation may define
   * specific options described somwhere else.
   *
   * @param aOptions {@link IOptionSet} - optional query execution parameters
   * @return {@link ISkHistoryQuery} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ISkHistoryQuery createQuery( IOptionSet aOptions );

  /**
   * Returns currently active queries.
   *
   * @return {@link IStringMap}&lt;{@link ISkHistoryQuery}&gt; - map "query instance ID" - "the query"
   */
  IStringMap<ISkHistoryQuery> activeQueries();

}
