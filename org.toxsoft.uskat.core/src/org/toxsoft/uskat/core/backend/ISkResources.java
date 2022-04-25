package org.toxsoft.uskat.core.backend;

/**
 * Localizable resources.
 *
 * @author hazard157
 */
@SuppressWarnings( "nls" )
interface ISkResources {

  String STR_N_SKBI_MAX_CLOB_LENGTH        = "Max CLOB";
  String STR_D_SKBI_MAX_CLOB_LENGTH        = "Maximal number of characters in CLOB string storage backend";
  String STR_N_SKBI_NEEDS_THREAD_SEPARATOR = "Thread separator?";
  String STR_D_SKBI_NEEDS_THREAD_SEPARATOR = "Backend-fronend communication requires messaging thread separation";

  String STR_N_SKBI_BA_EVENTS_IS_REMOTE  = "Remove events?";
  String STR_D_SKBI_BA_EVENTS_IS_REMOTE  = "The flag indicates that backend supports events send/receive";
  String STR_N_SKBI_BA_EVENTS_IS_HISTORY = "Events history?";
  String STR_D_SKBI_BA_EVENTS_IS_HISTORY = "The flag indicates that backend supports events history";

}
