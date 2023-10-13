package org.toxsoft.uskat.skadmin.dev.events;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.bricks.strio.IStrioHardConstants.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.skadmin.core.plugins.AdminPluginUtils.*;
import static org.toxsoft.uskat.skadmin.dev.events.IAdminHardResources.*;

import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;
import org.toxsoft.uskat.s5.server.sequences.maintenance.IS5SequenceUnionOptions;
import org.toxsoft.uskat.s5.server.sequences.maintenance.IS5SequenceValidationOptions;
import org.toxsoft.uskat.skadmin.core.IAdminCmdArgDef;
import org.toxsoft.uskat.skadmin.core.impl.AdminCmdArgDef;
import org.toxsoft.uskat.skadmin.dev.AdminPluginDev;

/**
 * Константы пакета.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IAdminHardConstants {

  String MULTI = "*";

  /**
   * Префикс идентификаторов команд и их алиасов плагина
   */
  String CMD_PATH_PREFIX = AdminPluginDev.DEV_CMD_PATH + "events.";

  // ------------------------------------------------------------------------------------
  // AdminCmdFire, AdminCmdQuery
  //
  /**
   * Аргумент {@link AdminCmdFire}: Идентификатор класса объекта
   */
  IAdminCmdArgDef ARG_CLASSID = new AdminCmdArgDef( "classId", DT_STRING_NULLABLE, STR_ARG_CLASSID );

  /**
   * Аргумент {@link AdminCmdFire}: Строковый идентификатор объекта класса
   */
  IAdminCmdArgDef ARG_STRID = new AdminCmdArgDef( "strid", DT_STRING_NULLABLE, STR_ARG_STRID );

  /**
   * Аргумент {@link AdminCmdFire}: Идентификатор события
   */
  IAdminCmdArgDef ARG_EVID = new AdminCmdArgDef( "evId", DT_STRING_NULLABLE, STR_ARG_EVID );

  // ------------------------------------------------------------------------------------
  // AdminCmdFire
  //
  String CMD_FIRE_ID    = CMD_PATH_PREFIX + "fire";
  String CMD_FIRE_ALIAS = EMPTY_STRING;
  String CMD_FIRE_NAME  = EMPTY_STRING;
  String CMD_FIRE_DESCR = STR_CMD_FIRE;

  /**
   * Аргумент {@link AdminCmdFire}: Список параметров события
   */
  IAdminCmdArgDef ARG_FIRE_PARAMS = new AdminCmdArgDef( "params", PT_OPSET, STR_ARG_FIRE_PARAMS );

  // ------------------------------------------------------------------------------------
  // AdminCmdReceiver
  //
  String CMD_RECV_ID    = CMD_PATH_PREFIX + "receiver";
  String CMD_RECV_ALIAS = EMPTY_STRING;
  String CMD_RECV_NAME  = EMPTY_STRING;
  String CMD_RECV_DESCR = STR_CMD_RECV;

  /**
   * Аргумент {@link AdminCmdReceiver}: Список GWID-идентификаторов выполняемых событий
   */
  IAdminCmdArgDef ARG_RECV_GWIDS = new AdminCmdArgDef( "gwids", PT_LIST_STRING, STR_ARG_RECV_GWIDS );

  /**
   * Аргумент {@link AdminCmdReceiver}: Время ожидания событий(мсек)
   */
  IAdminCmdArgDef ARG_RECV_TIMEOUT =
      new AdminCmdArgDef( "timeout", createType( INTEGER, "6000" ), STR_ARG_RECV_TIMEOUT );

  // ------------------------------------------------------------------------------------
  // AdminCmdQuery
  //
  String CMD_QUERY_ID    = CMD_PATH_PREFIX + "query";
  String CMD_QUERY_ALIAS = EMPTY_STRING;
  String CMD_QUERY_NAME  = EMPTY_STRING;
  String CMD_QUERY_DESCR = STR_CMD_QUERY;

  /**
   * Аргумент : метка времени начала чтения событий.
   */
  IAdminCmdArgDef ARG_QUERY_START_TIME =
      new AdminCmdArgDef( "startTime", DT_TIMESTAMP_NULLABLE, STR_ARG_QUERY_START_TIME );

  /**
   * Аргумент : метка времени завершения чтения событий.
   */
  IAdminCmdArgDef ARG_QUERY_END_TIME = new AdminCmdArgDef( "endTime", DT_TIMESTAMP_NULLABLE, STR_ARG_QUERY_END_TIME );

  /**
   * Аргумент : Вывод значений параметров события.
   */
  IAdminCmdArgDef ARG_QUERY_PARAMS = new AdminCmdArgDef( "params", DT_BOOLEAN_NULLABLE, STR_ARG_QUERY_PARAMS );

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

  String ARG_VALIDATION_REPAIR_ID      = "repair";
  String ARG_VALIDATION_REPAIR_ALIAS   = "r";
  String ARG_VALIDATION_REPAIR_NAME    = IS5SequenceValidationOptions.REPAIR.nmName();
  String ARG_VALIDATION_REPAIR_DESCR   = IS5SequenceValidationOptions.REPAIR.description();
  String ARG_VALIDATION_REPAIR_DEFAULT = String.valueOf( IS5SequenceValidationOptions.REPAIR.defaultValue().asBool() );

  String ARG_VALIDATION_FORCE_REPAIR_ID      = "force";
  String ARG_VALIDATION_FORCE_REPAIR_ALIAS   = "f";
  String ARG_VALIDATION_FORCE_REPAIR_NAME    = IS5SequenceValidationOptions.FORCE_REPAIR.nmName();
  String ARG_VALIDATION_FORCE_REPAIR_DESCR   = IS5SequenceValidationOptions.FORCE_REPAIR.description();
  String ARG_VALIDATION_FORCE_REPAIR_DEFAULT =
      String.valueOf( IS5SequenceValidationOptions.FORCE_REPAIR.defaultValue().asBool() );

  String ARG_VALIDATION_IDS_ID    = "ids";
  String ARG_VALIDATION_IDS_ALIAS = "";
  String ARG_VALIDATION_IDS_NAME  = IS5SequenceValidationOptions.GWIDS.nmName();
  String ARG_VALIDATION_IDS_DESCR = IS5SequenceValidationOptions.GWIDS.description();

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

  String ARG_UNION_IDS_ID    = "ids";
  String ARG_UNION_IDS_ALIAS = "";
  String ARG_UNION_IDS_NAME  = IS5SequenceUnionOptions.UNION_GWIDS.nmName();
  String ARG_UNION_IDS_DESCR = IS5SequenceUnionOptions.UNION_GWIDS.description();

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
