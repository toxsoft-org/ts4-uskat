package org.toxsoft.uskat.sysext.alarms.api;

/**
 * Локализуемые ресурсы.
 *
 * @author goga
 */
@SuppressWarnings( "nls" )
interface ISkResources {

  /**
   * {@link EAlarmPriority} название критического уровня
   */
  String STR_N_AP_CRITICAL = "Критический";
  /**
   * {@link EAlarmPriority} описание критического уровня
   */
  String STR_D_AP_CRITICAL = "Критический (наивысший) уровень важности";
  /**
   *
   */
  String STR_N_AP_HIGH = "Высокий";
  /**
   *
   */
  String STR_D_AP_HIGH = "Высокий уровень важности";
  /**
   *
   */
  String STR_N_AP_NORMAL = "Обычный";
  /**
   *
   */
  String STR_D_AP_NORMAL = "Обычный (средний) уровень важности тревоги";
  /**
   *
   */
  String STR_N_AP_LOW = "Низкий";
  /**
   *
   */
  String STR_D_AP_LOW = "Низкий уровень приориитета";
  /**
   *
   */
  String STR_N_AP_INFO = "Информационный";
  /**
   *
   */
  String STR_D_AP_INFO = "Информационный (самый низкий) уровень приоритета";

}
