package org.toxsoft.uskat.core.api.hqserv;

import org.toxsoft.core.tslib.coll.primtypes.*;

/**
 * Queries data useing SkatQL query language.
 * <p>
 * FIXME as of 04/07/2022 this API is under very early step of development !
 *
 * @author hazard157
 */
public interface ISkLanguageQuery
    extends ISkHistoryQuery {

  /**
   * Prepares the query.
   *
   * @param aSkatQl String - the statement in SkatQL query language
   * @param aArgs {@link IStringMap}&lt;Object&gt; - statement arguments FIXME arg definition API must be developed
   */
  void prepare( String aSkatQl, IStringMap<Object> aArgs );

  /**
   * Returns the query result.
   *
   * @return Object - the query result FIXME it is most diffuclt part to develop result data representation model
   */
  Object getResult();

}
