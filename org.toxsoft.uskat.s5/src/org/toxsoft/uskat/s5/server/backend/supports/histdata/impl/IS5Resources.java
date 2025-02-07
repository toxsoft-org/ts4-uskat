package org.toxsoft.uskat.s5.server.backend.supports.histdata.impl;

/**
 * Локализуемые ресурсы реализации.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IS5Resources {

  String STR_D_BACKEND_CURRDATA = "Поддержка расширения бекенда: 'доступ к текущим данным'";
  String STR_D_BACKEND_HISTDATA = "Поддержка расширения бекенда: 'доступ к хранимым данным'";

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_REJECT_HISTDATA_WRITE_BY_INTERCEPTORS =
      "Интерсепторы синглетона отклонили запись значений хранимых данных";
  String MSG_WRITE_HISTDATA_VALUES_INFO            =
      "Writing historical values(%d). Duration %d(msec). Сhange LogLevel => TRACE to output values.";

  String MSG_WRITE_HISTDATA_VALUES_DEBUG = "Writing historical values(%d). Duration %d(msec). Values:";
  String MSG_HISTDATA_VALUE              = "   %s=%s, count=%d";
  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_WRITE_UNEXPECTED = "Неожиданная ошибка записи хранимых данных. Причина: %s";
}
