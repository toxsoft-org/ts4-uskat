package org.toxsoft.uskat.s5.cron.supports;

import org.toxsoft.uskat.s5.common.IS5CommonResources;

/**
 * Локализуемые ресурсы.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IS5Resources
    extends IS5CommonResources {

  // ------------------------------------------------------------------------------------
  // Константы, идентификаторы, имена, описания
  //
  String STR_SCHEDULE_TIMER = "schedule timer[%s]";

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_DOJOB                      = "Schedules backend doJob";
  String MSG_CREATE_S5_CLASS            = "Создание s5-класса: %s [%s].";
  String MSG_CREATE_S5_OBJ              = "Создание s5-объекта: %s [%s].";
  String MSG_SCHEDULE_TIMER_EVENT_START = "Появление запланированного события. Расписание: %s";
  String MSG_FINISH_TIMER               = "Остановлен таймер для расписания %s";
  String MSG_START_TIMER                = "Запущен таймер для расписания %s\n" +               //
      "   second = %s\n" +                                                                     //
      "   minute = %s\n" +                                                                     //
      "   hour = %s\n" +                                                                       //
      "   dayOfWeek = %s\n" +                                                                  //
      "   dayOfMonth = %s\n" +                                                                 //
      "   month = %s\n" +                                                                      //
      "   year = %s\n";

  String MSG_TIMER_EVENT_START  = "Запускается обработка события таймера: %s";
  String MSG_TIMER_EVENT_FINISH = "Завершена обработка события таймера: %s";

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_TRY_LOCK                   = "doJob(...): ошибка получения блокировки %s.";
  String ERR_CANT_UPDATE_SCHEDULE_CLASS = "Запрещено изменять класс расписания используемый службой ISkCronService";
  String ERR_CANT_REMOVE_SCHEDULE_CLASS = "Запрещено удалять класс расписания используемый службой ISkCronService";
  String ERR_UNKNOWN_TIMER_EVENT        = "Событие неизвестного таймера %s. Таймер будет остановлен";
  String ERR_START_TIMER                = "Ошибка инициализации таймера для календаря %s. Причина: %s";
  String ERR_REMOVING_TIMER_NOT_FOUND   = "Попытка удаления таймера расписания %s. Таймер не найден.";

}
