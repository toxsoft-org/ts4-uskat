package org.toxsoft.uskat.skadmin.dev.commands;

/**
 * Локализуемые ресурсы пакета.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IAdminHardResources {

  // ------------------------------------------------------------------------------------
  // AdminCmdSend
  //
  String STR_CMD_SEND                = "Отправка команды на исполнение";
  String STR_ARG_SEND_CLASSID        = "Идентификатор класса объекта";
  String STR_ARG_SEND_STRID          = "Строковый идентификатор объекта класса classId. * - все объекты класса";
  String STR_ARG_SEND_CMDID          = "Идентификатор команды";
  String STR_ARG_SEND_ARGS           = "Список аргументов команды";
  String STR_ARG_SEND_AUTHOR_CLASSID = "Класс автора команды";
  String STR_ARG_SEND_AUTHOR_STRID   = "Строковый идентификатор автора команды";
  String STR_ARG_SEND_TIMEOUT        = "Таймаут(мсек) ожидания завершения выполнения команды";

  // ------------------------------------------------------------------------------------
  // AdminCmdExecutor
  //
  String STR_CMD_EXEC                  = "Выполнение команды.";
  String STR_ARG_EXEC_GWIDS            = "Список GWID-идентификаторов выполняемых команд";
  String STR_ARG_EXEC_TIMEOUT          = "Время ожидания команд(мсек)";
  String STR_ARG_EXEC_RESPONSE         = "Состояние возвращаемое при получении команды";
  String STR_ARG_EXEC_RESPONSE_TIMEOUT = "Таймаут отправки ответного состояния команды (мсек)";

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

  String MSG_INFO_LINE =
      "-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n";
  String MSG_COMMA     = ",";
  String MSG_YES       = "Да";
  String MSG_NO        = "Нет";
  String MSG_CMD_TIME  = "Время выполнения команды: %d (мсек).";

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

  String MSG_CLS_INFO_CMD_RES                = "\n Команда успешно выполнена: %s\n Результаты выполнения:\n";
  String MSG_INFO_CMD_RES_LEGEND             = "| Время                 | Состояние           | Сообщение\n";
  String MSG_INFO_CMD_RES_INFO               = "|%-23s| %-20s| %s\n";
  String MSG_INFO_CMD_EXECUTED               = "Обработка команды успешно завершена";
  String MSG_COMMAND_COMPLETE                = "Команда %s успешно выполнена";
  String MSG_COMMAND_SEND                    = "Команда %s отправлена на выполнение";
  String MSG_COMMAND_ARG_NOT_FOUND           = "Не найдено значение аргумента команды: %s";
  String MSG_COMMAND_STATE_CHANGED           = "Изменение состояния команды : %s";
  String MSG_EXCUTABLE_COMMAND_GWIDS_CHANGED = "Изменение списка поддерживаемых команд. Count = %d";
  String MSG_COMMAND_EXECUTE                 = "Получение команды на выполнение : %s";
  String MSG_COMMAND_EXECUTE_TIMEOUT         = "Таймаут ответа на команду : %d msec";
  String MSG_COMMAND_EXECUTE_RESPONSNE       = "Передача ответа на команду: %s";

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
