package org.toxsoft.uskat.alarms.lib.impl;

/**
 * Localizable resources.
 *
 * @author hazard157
 */
@SuppressWarnings( "nls" )
interface ISkResources {

  /**
   * {@link SkAlarmService}
   */
  String STR_N_ALARM_USER = "Алармы";
  String STR_D_ALARM_USER = "Управление алармами системы";

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_ALARM_ON  = "Включение аларма: %s. authorId = %s";
  String MSG_ALARM_OFF = "Выключение аларма: %s. authorId = %s";

  String MSG_ADD_ALARM_DEF = "В системе зарегистрированно описание аларма %s";

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_ALARM_DEF_ALREADY_EXIST = "Аларм %s [authorId = %s] уже зарегистрирован для генерации";

}
