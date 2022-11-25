package org.toxsoft.uskat.alarms.lib;

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
  String STR_N_AP_HIGH     = "Высокий";
  /**
   *
   */
  String STR_D_AP_HIGH     = "Высокий уровень важности";
  /**
   *
   */
  String STR_N_AP_NORMAL   = "Обычный";
  /**
   *
   */
  String STR_D_AP_NORMAL   = "Обычный (средний) уровень важности тревоги";
  /**
   *
   */
  String STR_N_AP_LOW      = "Низкий";
  /**
   *
   */
  String STR_D_AP_LOW      = "Низкий уровень приориитета";
  /**
   *
   */
  String STR_N_AP_INFO     = "Информационный";
  /**
   *
   */
  String STR_D_AP_INFO     = "Информационный (самый низкий) уровень приоритета";

  // ------------------------------------------------------------------------------------
  // IBaAlarms
  //
  String STR_N_BA_ALARMS = "Alarms";
  String STR_D_BA_ALARMS = "Alarms addon";

  /**
   * {@link ISkAlarmServiceHardConstants}
   */
  String STR_N_BOOL_PARAM = "Boolean";
  String STR_D_BOOL_PARAM = "Boolean history parameter";
  String STR_N_INT_PARAM  = "Integer";
  String STR_D_INT_PARAM  = "Integer history parameter";
  String STR_N_MESSAGE    = "Message";
  String STR_D_MESSAGE    = "SkAlarm history message";

}
