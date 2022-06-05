package org.toxsoft.uskat.skadmin.dev.events;

/**
 * Локализуемые ресурсы пакета.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IAdminHardResources {

  // ------------------------------------------------------------------------------------
  // AdminCmdFire, AdminCmdQuery
  //
  String STR_ARG_CLASSID = "Идентификатор класса объекта";
  String STR_ARG_STRID   = "Строковый идентификатор объекта класса classId";
  String STR_ARG_EVID    = "Идентификатор события";

  // ------------------------------------------------------------------------------------
  // AdminCmdFire
  //
  String STR_CMD_FIRE        = "Формирование событий системы";
  String STR_ARG_FIRE_PARAMS = "Список параметров события";

  // ------------------------------------------------------------------------------------
  // AdminCmdReceiver
  //
  String STR_CMD_RECV         = "Получение событий системы.";
  String STR_ARG_RECV_GWIDS   = "Список GWID-идентификаторов получаемых событий";
  String STR_ARG_RECV_TIMEOUT = "Время ожидания событий(мсек)";

  // ------------------------------------------------------------------------------------
  // AdminCmdQuery
  //
  String STR_CMD_QUERY            = "Запрос истории событий системы.";
  String STR_ARG_QUERY_START_TIME = "Метка времени начала чтения событий. По умолчанию: начало текущих суток";
  String STR_ARG_QUERY_END_TIME   = "Метка времени завершения чтения событий. По умолчанию: текущее время";
  String STR_ARG_QUERY_PARAMS     = "Выводить значения параметров события. По умолчанию: false";

  String MSG_CMD_QUERY_EVENT = "%s | %s = %s";

  // ------------------------------------------------------------------------------------

  String MSG_DBMS_STAT_LINE       =
      " --------------------------------------------------------------------------------\n";
  String MSG_DBMS_STAT_HEADER     =
      "|                    | second    | minute    | hour      | day       | all       |\n";
  String MSG_DBMS_STAT_WRITE      =
      "|- write ------------------------------------------------------------------------|\n";
  String MSG_DBMS_STAT_UNION      =
      "|- union ------------------------------------------------------------------------|\n";
  String MSG_DBMS_STAT_DETAIL_INT = "| %-19s| %-9d | %-9d | %-9d | %-9d | %-9d |\n";

  // ------------------------------------------------------------------------------------
  // Сообщения
  //

  String MSG_INFO_LINE         =
      "-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n";
  String MSG_COMMA             = ",";
  String MSG_YES               = "Да";
  String MSG_NO                = "Нет";
  String MSG_CMD_TIME          = "Время выполнения команды: %d (мсек).";
  String MSG_QUERY             = "Запрос событий (%d) в интервале startTime = %s, endTime = %s";
  String MSG_QUERY_EVENTS_SIZE = "Всего событий: %d";

  String MSG_NOT_TIME               = "нет запроса        ";
  String MSG_INFO_CMD_EXECUTED_QTTY = " Всего обработано команд: %d\n";
  String MSG_INFO_CMD_STATE_CHANGE  = " Статус обработки команды изменился.\n";

  String MSG_TIMEOUT                 = "Завершение выполнения команды по таймауту: %d мсек. ";
  String MSG_CLS_INFO_CMD            = "\n Получена командa \n";
  String MSG_INFO_CMD_TITLE          = " Информация по команде с ИД-путем %s: \n";
  String MSG_INFO_CMD_LEGEND         =
      "| Код                 | Состояние           | Время               | Получатель          | Автор\n";
  String MSG_INFO_CMD_INFO           = "| %-20.20s| %-20.20s| %-20.20s| %-20.20s|  %-20.20s\n";
  String MSG_INFO_CMD_INFO_FREE      = "| %-20s| %-20s| %-20s| %-20s|  %-20.20s\n";
  String MSG_INFO_CMD_ARGS           = "\n Аргументы команды: \n";
  String MSG_INFO_CMD_ARS_LEGEND     = "| Идентификатор       | Тип                 | Значение\n";
  String MSG_INFO_CMD_ARGS_INFO      = "| %-20.20s| %-20s| %-60.60s\n";
  String MSG_INFO_CMD_ARGS_INFO_FREE = "| %-20s| %-20s| %-60s\n";

  String MSG_CLS_INFO_CMD_RES    = "\n Команда успешно выполнена: %s\n Результаты выполнения:\n";
  String MSG_INFO_CMD_RES_LEGEND = "| Время                 | Состояние           | Сообщение\n";
  String MSG_INFO_CMD_RES_INFO   = "|%-23s| %-20s| %s\n";
  String MSG_EVENT_FIRED         = "Cобытие %s отправлено в систему";
  String MSG_RECV_EVENT          = "Получение события : %s";

  // ------------------------------------------------------------------------------------
  // Ошибки
  //
  String ERR_NOT_CONNECTION           = "Установите соединение с s5-сервером (команда login)";
  String ERR_INVALID_CLASSID          = "Не найден класс: %s";
  String ERR_INVALID_OBJECT_STRID     = "Не найден объект: %s";
  String ERR_INVALID_CMDID            = "Не найдена команда: %s";
  String ERR_BAD_CMDID_FORMAT         =
      "Нарушение формата определения cmdId %s. Формат должен быть в виде: \"cmdId1\", \"cmdId2\" ... \"cmdIdN\".";
  String ERR_BAD_ARG_FORMAT           =
      "Нарушение формата определения аргумента элементом %s. Формат должен быть в виде: \"argId:value\".";
  String ERR_BAD_INTERVAL_FORMAT      =
      "Нарушение формата определения интревала запроса элементом %s. Формат должен быть в виде: \"01.12.2003 12:00:00-02.12.2003 13:00:01\".";
  String ERR_MOVE_TIME_TOO_LOW        = "Слишком малое время подачи: %d, допускается >= %d [сек]";
  String ERR_MOVE_TIME_TOO_BIG        = "Слишком большое время подачи: %d, допускается <= %d [сек]";
  String ERR_BAD_COC_FORMAT           =
      "Нарушение формата определения Coc элементом %s. Формат должен быть в виде: \"classId:objStrid:cmdId\".";
  String ERR_AUTHOR_STRID_NOT_DEFINED = "Не определен строковый идентификатор объекта класса %s (автор команды)";
  String ERR_CANT_EXECUTE_BY_TIMEOUT  = "Команда %s не выполнена в установленное время (%d ms) и будет отменена";
}
