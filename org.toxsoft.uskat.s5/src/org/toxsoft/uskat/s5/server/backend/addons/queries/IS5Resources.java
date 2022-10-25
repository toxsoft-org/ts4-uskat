package org.toxsoft.uskat.s5.server.backend.addons.queries;

import org.toxsoft.uskat.core.api.hqserv.ISkHistoryQueryServiceConstants;

/**
 * Localizable resources.
 *
 * @author hazard157
 */
@SuppressWarnings( "nls" )
interface IS5Resources {

  /**
   * {@link S5BaQueriesConvoy}
   */
  String FMT_ERR_QUERY_INVALID_STATE = "%s. query invalid state: %s";
  String FMT_ERR_QUERY_TIMEOUT       = "Cancel query by timeout error. Try change -"
      + ISkHistoryQueryServiceConstants.OP_SK_MAX_EXECUTION_TIME.id() + " value to up (%d))";

}
