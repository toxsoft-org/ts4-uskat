package org.toxsoft.uskat.s5.cron.skadmin.cron;

/**
 * Локализуемые ресурсы пакета.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IAdminHardResources {

  // ------------------------------------------------------------------------------------
  // AdminCmdListSchedules
  //
  String STR_CMD_LIST_SCHEDULES  = "Вывод списка расписаний.";
  String STR_CMD_REMOVE_SCHEDULE = "Удаление расписания.";
  String STR_CMD_ADD_SCHEDULE    = "Добавление/обновление расписания.";
  String MSG_SCHEDULE            = ""                                                       //
      + "id:          %s\n"                                                                 //
      + "name:        %s\n"                                                                 //
      + "description: %s\n"                                                                 //
      + "seconds:     %s\n"                                                                 //
      + "minutes:     %s\n"                                                                 //
      + "hours:       %s\n"                                                                 //
      + "daysOfMonth: %s\n"                                                                 //
      + "months:      %s\n"                                                                 //
      + "daysOfWeek:  %s\n"                                                                 //
      + "years:       %s\n"                                                                 //
      + "timezone:    %s\n"                                                                 //
      + "start:       %s\n"                                                                 //
      + "end:         %s\n"                                                                 //
      + "ugwies:      %s\n";
  String MSG_SCHEDULE_LINE       =
      " --------------------------------------------------------------------------------\n";

  // ------------------------------------------------------------------------------------
  // AdminCmdRemoveSchedule
  //
  String STR_ARG_YES            = "Требование отвечать на все вопросы утвердительно. По умолчанию: false";
  String MSG_CMD_CONFIRM_REMOVE = "Подтвердите запрос удаления";
  String MSG_CMD_REMOVED        = "Расписание удалено.\n";
  String MSG_CMD_NOT_FOUND      = "Расписание не найдено.\n";
  String MSG_CMD_REJECT         = "Отказ пользователя от продолжения выполнения команды.";

  // ------------------------------------------------------------------------------------
  // AdminCmdAddSchedule
  //
  String STR_ARG_ID            = "Идентификатор расписания";
  String STR_ARG_NAME          = "Имя расписания";
  String STR_ARG_DESCR         = "Описание расписания";
  String STR_ARG_SECONDS       = "Cекунды. Примеры значений: *, */N где N - число, 0-59, 0,1,3-7";
  String STR_ARG_MINUTES       = "Минуты. Примеры значений: *, */N где N - число, 0-59, 0,1,3-7";
  String STR_ARG_HOURS         = "Часы. Примеры значений: *, */N где N - число, 0-23, 0,1,3-7";
  String STR_ARG_DAYS_OF_MONTH = "Дни месяца. Примеры значений: *, */N где N - число, 1-31, 1,3-7";
  String STR_ARG_MONTHS        = "Месяцы. Примеры значений: *, */N где N - число, 1-12, 1,3-7";
  String STR_ARG_DAYS_OF_WEEK  =
      "Дни недели. Примеры значений: *, */N где N - число, 0-7 где 0 и 7 это воскресенье, 0, 1,3-7";
  String STR_ARG_YEARS         = "Годы. Примеры значений: *, */N где N - число, 1900-2100, 2015,2016,2015-2030";
  String STR_ARG_TIMEZONE      = "Часовой пояс. Пустая строка - используется системный часовой пояс";
  String STR_ARG_START         = "Метка времени начала действия расписания.";
  String STR_ARG_END           = "Метка времени завершения действия расписания.";
  String STR_ARG_UGWIES        = "Идентификаторы ресурсов системы связанные с расписанием в формате UgwiList.KEEPER";
  String MSG_CMD_ADDED         = "Расписание добавлено.\n";
  String MSG_CMD_UPDATED       = "Расписание обновлено.\n";

  String ERR_UGWIES = "Неверный формат аргумента ugwies. Причина: %s\n";

  String MSG_CMD_TIME = "Время выполнения команды: %d (мсек).";
}
