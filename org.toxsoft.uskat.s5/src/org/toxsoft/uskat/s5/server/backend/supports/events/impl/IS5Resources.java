package org.toxsoft.uskat.s5.server.backend.supports.events.impl;

/**
 * Локализуемые ресурсы реализации.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IS5Resources {

  String STR_D_BACKEND_EVENTS = Messages.getString( "IS5Resources.STR_D_BACKEND_EVENTS" );
  String STR_D_EVENTS_FACTORY = Messages.getString( "IS5Resources.STR_D_EVENTS_FACTORY" );

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_READ_EVENTS       = Messages.getString( "IS5Resources.MSG_READ_EVENTS" );
  String MSG_START_READ_EVENTS = "queryEvents(...): start query. interval = %s, gwids = %s";

  String MSG_START_FIRE_EVENTS  = "fireEvents(...): start query. events = %s";
  String MSG_FINISH_FIRE_EVENTS = "fireEvents(...): query finished. events = %s, time = %d (msec)";

  String MSG_START_WRITE_EVENTS  = "writeEventsImpl(...): start query. events = %s";
  String MSG_FINISH_WRITE_EVENTS = "writeEventsImpl(...): query finished. events = %s, time = %d (msec)";

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String MSG_ERR_ON_EVENTS                       = Messages.getString( "IS5Resources.MSG_ERR_ON_EVENTS" );
  String MSG_REJECT_EVENTS_WRITE_BY_INTERCEPTORS = "Интерсепторы синглетона отклонили запись событий";
  String MSG_FRONTDATA_EVENTS_DOESNT_EXIST       = "Frontend %s has not events data (events = null)";
}
