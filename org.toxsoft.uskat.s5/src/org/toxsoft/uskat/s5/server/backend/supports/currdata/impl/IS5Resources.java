package org.toxsoft.uskat.s5.server.backend.supports.currdata.impl;

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
  String MSG_REJECT_CONFIGURE_BY_INTERCEPTORS = "Интерсепторы синглетона отклонили изменение набора";
  String MSG_REJECT_READER_BY_INTERCEPTORS    = "Интерсепторы синглетона отклонили регистрацию читателя данных";

  String MSG_CACHE_ALREADY_INITED                  =
      "Кэш текущих данных уже сформирован кластером. Количество данных: %d. Время инициализации: %d msec.";                  //$NON-NLS-1$
  String MSG_CACHE_INITED                          =
      "Сформирован кэш текущих данных кластера. Количество данных: %d. Время загрузки: %d msec. Время формирования: %d msec";
  String MSG_REJECT_CURRDATA_WRITE_BY_INTERCEPTORS = "Интерсепторы синглетона запись значений текущих данных";
  String MSG_WRITE_CURRDATA_VALUES_INFO            =
      "Writing current values(%d) in %d msec. Сhange LogLevel => TRACE to output values.";
  String MSG_WRITE_CURRDATA_VALUES_DEBUG           = "Writing current values(%d), time %d(msec). Values:";
  String MSG_WRITE_CURRDATA_VALUE                  = "Writing current value: %s=%s";
  String MSG_CURRDATA_VALUE                        = "   %s=%s";

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_CURRDATA_NOT_HAVE_DEFAULT_VALUE = "Текущее данное %s имеет тип %s у которого нет значения по умолчанию";
  String ERR_CANT_CHANGE_CLASS_WITH_OBJS     = "Запрет изменения класса %s имеющий объекты. Причина: %s";
  String ERR_WRONG_CACHE_SIZE                =
      "Размер кэша текущих данных не соотвествует количеству текущих данных в системе. Размер кэша = %d. Текущих данных в системе: %d.";
  String ERR_NO_DEFAULT_VALUE                =
      "Класс %s. Текущее данное %s имеет тип %s для которого требуется, но неопределено значение по умолчанию";
  String ERR_CACHE_VALUE_NOT_FOUND           =
      "В кэше currdataValuesCache не найдено данного gwid = %s для записи newValue = %s";
  String ERR_WRONG_VALUE_TYPE                =
      "Недопустимый тип значения для текущего данного. gwid=%s, dataType=%s, valueType=%s(%s)";
}
