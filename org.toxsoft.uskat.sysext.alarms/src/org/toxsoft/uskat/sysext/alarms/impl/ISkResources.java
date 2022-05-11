package org.toxsoft.uskat.sysext.alarms.impl;

import org.toxsoft.uskat.sysext.alarms.api.ISkAlarm;

/**
 * Локализуемые ресурсы.
 *
 * @author goga
 */
@SuppressWarnings( "nls" )
interface ISkResources {

  String MSG_ADD_ALARM_DEF = "В системе зарегистрированно описание аларма %s";

  /**
   * {@link SkAlarmFilterByDefId}
   */
  String STR_N_FILTER_BY_ALARM_DEF_ID = "По типу тревоги";
  String STR_D_FILTER_BY_ALARM_DEF_ID = "Фильтр тревог по типу (идентификатору типа) тревоги";

  /**
   * {@link SkAlarmFilterByAuthorObjId}
   */
  String STR_N_FILTER_BY_AUTHOR_OBJ_ID = "По автору";
  String STR_D_FILTER_BY_AUTHOR_OBJ_ID = "Фильтр тревог по автору (идентификатору объекта автора) тревоги";

  /**
   * {@link SkAlarmService}
   */
  String STR_N_ALARM_USER = "Алармы";
  String STR_D_ALARM_USER = "Управление алармами системы";

  /**
   * Общие.
   */
  String MSG_ALARM_ON                = "Включение аларма: %s. authorId = %s";
  String MSG_ALARM_OFF               = "Выключение аларма: %s. authorId = %s";
  String ERR_NON_ALARM_INPUT_OBJ     =
      "Фильтр %s: проверяемый объект должен иметь тип " + ISkAlarm.class.getSimpleName();
  String ERR_ALARM_DEF_ALREADY_EXIST = "Аларм %s [authorId = %s] уже зарегистрирован для генерации";
  String ERR_SILENT_MODE             =
      "%s. Запрет генерации аларма после запуска сервера. Время до окончания запрета %d (мсек)";

}
