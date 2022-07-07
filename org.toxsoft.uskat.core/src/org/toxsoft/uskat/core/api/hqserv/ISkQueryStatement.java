package org.toxsoft.uskat.core.api.hqserv;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.coll.primtypes.*;

/**
 * Queries data useing SkatQL query language.
 * <p>
 * FIXME as of 04/07/2022 this API is under early step of development !
 *
 * @author hazard157
 */
public interface ISkQueryStatement
    extends ISkAsynchronousQuery {

  /**
   * Prepares the query.
   *
   * @param aSkatQl String - the statement in SkatQL query language
   * @param aArgs {@link IStringMap}&lt;{@link IAtomicValue}&gt; - statement arguments
   */
  void prepare( String aSkatQl, IStringMap<IAtomicValue> aArgs );

  /**
   * Returns the query result.
   *
   * @return Object - the query result FIXME it's most difficult part to develop data representation model of the result
   */
  Object getResult();

}
