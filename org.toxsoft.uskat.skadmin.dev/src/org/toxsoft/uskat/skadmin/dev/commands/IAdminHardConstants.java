package org.toxsoft.uskat.skadmin.dev.commands;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.bricks.strio.IStrioHardConstants.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.skadmin.core.plugins.AdminPluginUtils.*;
import static org.toxsoft.uskat.skadmin.dev.commands.IAdminHardResources.*;

import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.legacy.plexy.*;
import org.toxsoft.uskat.skadmin.core.*;
import org.toxsoft.uskat.skadmin.core.impl.*;
import org.toxsoft.uskat.skadmin.dev.*;

/**
 * Константы пакета.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IAdminHardConstants {

  /**
   * Тип аргумента "состояние ответа команды"
   */
  IPlexyType RESPONSE_STATE_TYPE = ptSingleValue( createType( STRING, avStr( ESkCommandState.SUCCESS.id() ) ) );

  /**
   * Префикс идентификаторов команд и их алиасов плагина
   */
  String CMD_PATH_PREFIX = AdminPluginDev.CMD_PATH + "commands.";

  // ------------------------------------------------------------------------------------
  // AdminCmdSend
  //
  String CMD_SEND_ID    = CMD_PATH_PREFIX + "send";
  String CMD_SEND_ALIAS = EMPTY_STRING;
  String CMD_SEND_NAME  = EMPTY_STRING;
  String CMD_SEND_DESCR = STR_CMD_SEND;

  /**
   * Аргумент {@link AdminCmdSend}: Идентификатор класса объекта
   */
  IAdminCmdArgDef ARG_SEND_CLASSID = new AdminCmdArgDef( "classId", DT_STRING, STR_ARG_SEND_CLASSID );

  /**
   * Аргумент {@link AdminCmdSend}: Строковый идентификатор объекта класса
   */
  IAdminCmdArgDef ARG_SEND_STRID = new AdminCmdArgDef( "strid", DT_STRING, STR_ARG_SEND_STRID );

  /**
   * Аргумент {@link AdminCmdSend}: Идентификатор команды
   */
  IAdminCmdArgDef ARG_SEND_CMDID = new AdminCmdArgDef( "cmdId", DT_STRING, STR_ARG_SEND_CMDID );

  /**
   * Аргумент {@link AdminCmdSend}: Список аргументов команды
   */
  IAdminCmdArgDef ARG_SEND_ARGS = new AdminCmdArgDef( "args", PT_OPSET, STR_ARG_SEND_ARGS );

  /**
   * Аргумент {@link AdminCmdSend}: Класс автора команды
   */
  IAdminCmdArgDef ARG_SEND_AUTHOR_CLASSID =
      new AdminCmdArgDef( "authorClassId", DT_BOOLEAN_NULLABLE, STR_ARG_SEND_AUTHOR_CLASSID );

  /**
   * Аргумент {@link AdminCmdSend}: Строковый идентификатор автора команды
   */
  IAdminCmdArgDef ARG_SEND_AUTHOR_STRID =
      new AdminCmdArgDef( "authorStrid", DT_BOOLEAN_NULLABLE, STR_ARG_SEND_AUTHOR_STRID );

  // ------------------------------------------------------------------------------------
  // AdminCmdExecutor
  //
  String CMD_EXEC_ID    = CMD_PATH_PREFIX + "executor";
  String CMD_EXEC_ALIAS = EMPTY_STRING;
  String CMD_EXEC_NAME  = EMPTY_STRING;
  String CMD_EXEC_DESCR = STR_CMD_EXEC;

  /**
   * Аргумент {@link AdminCmdExecutor}: Список GWID-идентификаторов выполняемых команд
   */
  IAdminCmdArgDef ARG_EXEC_GWIDS = new AdminCmdArgDef( "gwids", PT_LIST_STRING, STR_ARG_EXEC_GWIDS );

  /**
   * Аргумент {@link AdminCmdExecutor}: Время ожидания команд(мсек)
   */
  IAdminCmdArgDef ARG_EXEC_TIMEOUT =
      new AdminCmdArgDef( "timeout", createType( INTEGER, avInt( 6000 ) ), STR_ARG_EXEC_TIMEOUT );

  /**
   * Аргумент {@link AdminCmdExecutor}: Состояние возвращаемое при получении команды
   */
  IAdminCmdArgDef ARG_EXEC_RESPONSE = new AdminCmdArgDef( "response", RESPONSE_STATE_TYPE, STR_ARG_EXEC_RESPONSE );

  /**
   * Аргумент {@link AdminCmdExecutor}: Таймаут отправки ответного состояния команды (мсек)
   */
  IAdminCmdArgDef ARG_EXEC_RESPONSE_TIMEOUT =
      new AdminCmdArgDef( "responseTimeout", createType( INTEGER, avInt( 100 ) ), STR_ARG_EXEC_RESPONSE_TIMEOUT );

  // ------------------------------------------------------------------------------------
  // Аргументы команды
  //
  String ARG_START_TIME_ID      = "startTime";
  String ARG_START_TIME_ALIAS   = "s";
  String ARG_START_TIME_NAME    = "";
  String ARG_START_TIME_DESCR   = "Время начала интервала (включительно) набора данных в формате: " + TIMESTAMP_FMT
      + " или в его сокращенной форме. ";
  String ARG_START_TIME_DEFAULT = String.valueOf( TimeUtils.MIN_TIMESTAMP );

  String ARG_END_TIME_ID      = "endTime";
  String ARG_END_TIME_ALIAS   = "e";
  String ARG_END_TIME_NAME    = "";
  String ARG_END_TIME_DESCR   = "Время завершения интервала (включительно) набора данных в формате: " + TIMESTAMP_FMT
      + " или в его сокращенной форме. ";
  String ARG_END_TIME_DEFAULT = String.valueOf( TimeUtils.MAX_TIMESTAMP );

  // ------------------------------------------------------------------------------------
  // AdminCmdCommandValidation
  //
  String VALIDATION_CMD_ID    = CMD_PATH_PREFIX + "validation";
  String VALIDATION_CMD_ALIAS = "";
  String VALIDATION_CMD_NAME  = "Проверка";
  String VALIDATION_CMD_DESCR = "Запуск задачи проверки и исправления блоков хранения команд.";

  String MSG_VALIDATION_FINISH = "Завершение задачи проверки блоков последовательностей.\n" + //
      "Всего данных        : %d\n" + //
      "Обработано блоков   : %d\n" + //
      "Предупреждений      : %d\n" + //
      "Ошибок              : %d\n" + //
      "Обновлено блоков    : %d\n" + //
      "Удалено блоков      : %d\n" + //
      "Неэффективных блоков: %d\n" + //
      "Количество значений : %d\n" + //
      "Время обработки     : %d(sec)";

  // ------------------------------------------------------------------------------------
  // AdminCmdCommandUnion
  //
  String UNION_CMD_ID    = CMD_PATH_PREFIX + "union";
  String UNION_CMD_ALIAS = "";
  String UNION_CMD_NAME  = "Дефрагментация";
  String UNION_CMD_DESCR = "Запуск процесса дефрагментации блоков истории команд.";

  String MSG_UNION_FINISH = "Завершение задачи объединения блоков последовательностей.\n" + //
      "Проанализировано   : %d\n" + //
      "Дефрагментировано  : %d\n" + //
      "Обработанных блоков: %d\n" + //
      "Удаленных блоков   : %d\n" + //
      "Количество значений: %d\n" + //
      "Количество ошибок  : %d\n" + //
      "Время обработки    : %d (сек)";

  // ------------------------------------------------------------------------------------
  // AdminCmdCommandsDbmsStatistics
  //
  String DBMS_STATISTICS_CMD_ID    = CMD_PATH_PREFIX + "dbmsStatistics";
  String DBMS_STATISTICS_CMD_ALIAS = "";
  String DBMS_STATISTICS_CMD_NAME  = "Статистика ввода-вывода dbms";
  String DBMS_STATISTICS_CMD_DESCR = "Вывод статистики ввода-вывода блоков dbms.";

}
