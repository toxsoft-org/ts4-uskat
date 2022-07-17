package org.toxsoft.uskat.core.backend;

/**
 * Localizable resources.
 *
 * @author hazard157
 */
@SuppressWarnings( "nls" )
interface ISkResources {

  String STR_N_SKBI_MAX_CLOB_LENGTH           = "Max CLOB";
  String STR_D_SKBI_MAX_CLOB_LENGTH           = "Maximal number of characters in CLOB string storage backend";
  String STR_N_SKBI_NEED_THREAD_SAFE_FRONTEND = "Thread-safety?";
  String STR_D_SKBI_NEED_THREAD_SAFE_FRONTEND = "Backend need the thread-safe implementation of the frontend read API";

  String STR_N_SKBI_BA_EVENTS_IS_HISTORY = "Events history?";
  String STR_D_SKBI_BA_EVENTS_IS_HISTORY = "The flag indicates that backend supports events history";

  String STR_N_BA_CLASSES  = "Classes";
  String STR_D_BA_CLASSES  = "Classes info managemnt addon";
  String STR_N_BA_CLOBS    = "Clobs";
  String STR_D_BA_CLOBS    = "Clobs addon";
  String STR_N_BA_EVENTS   = "Events";
  String STR_D_BA_EVENTS   = "Events addon";
  String STR_N_BA_LINKS    = "Links";
  String STR_D_BA_LINKS    = "Links addon";
  String STR_N_BA_RTDATA   = "RTdata";
  String STR_D_BA_RTDATA   = "RTdata addon";
  String STR_N_BA_COMMANDS = "Commands";
  String STR_D_BA_COMMANDS = "Commands addon";
  String STR_N_BA_OBJECTS  = "Objects";
  String STR_D_BA_OBJECTS  = "Objects addon";
  String STR_N_BA_QUERIES  = "Queries";
  String STR_D_BA_QUERIES  = "Queriesaddon";

}
