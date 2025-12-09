package org.toxsoft.uskat.s5.server.backend.supports.events.impl;

/**
 * Локализуемые ресурсы реализации.
 *
 * @author mvk
 */
interface IS5Resources {

  String STR_D_BACKEND_EVENTS = Messages.getString( "IS5Resources.STR_D_BACKEND_EVENTS" ); //$NON-NLS-1$
  String STR_D_EVENTS_FACTORY = Messages.getString( "IS5Resources.STR_D_EVENTS_FACTORY" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_READ_EVENTS = Messages.getString( "IS5Resources.MSG_READ_EVENTS" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String MSG_ERR_ON_EVENTS                       = Messages.getString( "IS5Resources.MSG_ERR_ON_EVENTS" ); //$NON-NLS-1$
  String MSG_REJECT_EVENTS_WRITE_BY_INTERCEPTORS = "Интерсепторы синглетона отклонили запись событий";     //$NON-NLS-1$
  String MSG_FRONTDATA_EVENTS_DOESNT_EXIST       = "Frontend %s has not events data (events = null)";      //$NON-NLS-1$
}
