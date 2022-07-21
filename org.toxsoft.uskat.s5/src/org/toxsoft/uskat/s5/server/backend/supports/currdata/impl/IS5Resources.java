package org.toxsoft.uskat.s5.server.backend.supports.currdata.impl;

/**
 * Локализуемые ресурсы реализации.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IS5Resources {

  String STR_D_BACKEND_CURRDATA = "Подджержка расширения бекенда: 'доступ к текущим данным'";
  String STR_D_BACKEND_HISTDATA = "Подджержка расширения бекенда: 'доступ к хранимым данным'";

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_REJECT_CONFIGURE_BY_INTERCEPTORS = "Интерсепторы синглетона отклонили изменение набора";
  String MSG_REJECT_READER_BY_INTERCEPTORS    = "Интерсепторы синглетона отклонили регистрацию читателя данных";

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_CURRDATA_NOT_HAVE_DEFAULT_VALUE = "Текущее данное %s имеет тип %s у которого нет значения по умолчанию";
  String ERR_CANT_CHANGE_CLASS_WITH_OBJS     = "Запрет изменения класса %s имеющий объекты. Причина: %s";
}
