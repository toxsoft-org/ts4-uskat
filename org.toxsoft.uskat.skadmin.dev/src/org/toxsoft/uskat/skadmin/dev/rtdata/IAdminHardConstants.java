package org.toxsoft.uskat.skadmin.dev.rtdata;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.bricks.strio.IStrioHardConstants.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.s5.server.sequences.maintenance.S5SequenceUnionConfig.*;
import static org.toxsoft.uskat.s5.server.sequences.maintenance.S5SequenceValidationConfig.*;
import static org.toxsoft.uskat.skadmin.core.plugins.AdminPluginUtils.*;
import static org.toxsoft.uskat.skadmin.dev.rtdata.IAdminHardResources.*;

import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.uskat.skadmin.core.*;
import org.toxsoft.uskat.skadmin.core.impl.*;
import org.toxsoft.uskat.skadmin.dev.*;
import org.toxsoft.uskat.skadmin.dev.events.*;

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
  String CMD_PATH_PREFIX = AdminPluginDev.DEV_CMD_PATH + "rtdata.";

  // ------------------------------------------------------------------------------------
  // AdminCmdRead, AdminCmdWrite, AdminCmdClose
  //
  /**
   * Аргумент : Идентификатор класса объекта
   */
  IAdminCmdArgDef ARG_CLASSID = new AdminCmdArgDef( "classId", DT_STRING_NULLABLE, STR_ARG_CLASSID );

  /**
   * Аргумент : Строковый идентификатор объекта класса
   */
  IAdminCmdArgDef ARG_STRID = new AdminCmdArgDef( "strid", DT_STRING_NULLABLE, STR_ARG_STRID );

  /**
   * Аргумент : Идентификатор данного
   */
  IAdminCmdArgDef ARG_DATAID = new AdminCmdArgDef( "dataId", DT_STRING_NULLABLE, STR_ARG_DATA );

  // ------------------------------------------------------------------------------------
  // AdminCmdRead
  //
  String          CMD_READ_ID    = CMD_PATH_PREFIX + "read";
  String          CMD_READ_ALIAS = EMPTY_STRING;
  String          CMD_READ_NAME  = EMPTY_STRING;
  String          CMD_READ_DESCR = STR_CMD_READ;
  /**
   * Аргумент : требование закрыть указанные каналы чтения
   */
  IAdminCmdArgDef ARG_READ_CLOSE =
      new AdminCmdArgDef( "close", createType( BOOLEAN, avBool( false ) ), STR_ARG_READ_CLOSE );

  /**
   * Аргумент : метка времени начала чтения хранимых данных.
   */
  IAdminCmdArgDef ARG_READ_START_TIME =
      new AdminCmdArgDef( "startTime", DT_TIMESTAMP_NULLABLE, STR_ARG_READ_START_TIME );

  /**
   * Аргумент : метка времени завершения чтения хранимых данных.
   */
  IAdminCmdArgDef ARG_READ_END_TIME = new AdminCmdArgDef( "endTime", DT_TIMESTAMP_NULLABLE, STR_ARG_READ_END_TIME );

  /**
   * Аргумент : Тип интервала чтения данных.
   */
  IAdminCmdArgDef ARG_READ_TYPE = new AdminCmdArgDef( "type", DT_STRING_NULLABLE, STR_ARG_READ_TYPE );

  /**
   * Аргумент : метка времени завершения чтения хранимых данных.
   */
  IAdminCmdArgDef ARG_READ_TIMEOUT = new AdminCmdArgDef( "timeout", DT_TIMESTAMP_NULLABLE, STR_ARG_READ_END_TIME );

  // ------------------------------------------------------------------------------------
  // AdminCmdWrite
  //
  String CMD_WRITE_ID    = CMD_PATH_PREFIX + "write";
  String CMD_WRITE_ALIAS = EMPTY_STRING;
  String CMD_WRITE_NAME  = EMPTY_STRING;
  String CMD_WRITE_DESCR = STR_CMD_WRITE;

  /**
   * Аргумент : Значение данного
   */
  IAdminCmdArgDef ARG_WRITE_VALUE = new AdminCmdArgDef( "value", DT_NONE, STR_ARG_WRITE_VALUE );

  /**
   * Аргумент : требование сохранить значение как текущее значение
   */
  IAdminCmdArgDef ARG_WRITE_CURRDATA = new AdminCmdArgDef( "currdata", DT_BOOLEAN_NULLABLE, STR_ARG_WRITE_CURRDATA );

  /**
   * Аргумент : требование сохранить значение как хранимого данное
   */
  IAdminCmdArgDef ARG_WRITE_HISTDATA = new AdminCmdArgDef( "histdata", DT_BOOLEAN_NULLABLE, STR_ARG_WRITE_HISTDATA );

  /**
   * Аргумент : метка времени для сохранения хранимых данных.
   */
  IAdminCmdArgDef ARG_WRITE_TIMESTAMP =
      new AdminCmdArgDef( "timestamp", DT_TIMESTAMP_NULLABLE, STR_ARG_WRITE_TIMESTAMP );

  // ------------------------------------------------------------------------------------
  // AdminCmdWriteTest
  //
  String CMD_WRITE_TEST_ID    = CMD_PATH_PREFIX + "writeTest";
  String CMD_WRITE_TEST_ALIAS = EMPTY_STRING;
  String CMD_WRITE_TEST_NAME  = EMPTY_STRING;
  String CMD_WRITE_TEST_DESCR = STR_CMD_WRITE_TEST;

  /**
   * Аргумент : Таймаут между передачами значений.
   */
  IAdminCmdArgDef ARG_WRITE_TIMEOUT = new AdminCmdArgDef( "timeout", DT_INTEGER_NULLABLE, STR_ARG_WRITE_TEST_TIMEOUT );

  /**
   * Аргумент : Количество передач значений.
   */
  IAdminCmdArgDef ARG_WRITE_COUNT = new AdminCmdArgDef( "count", DT_INTEGER_NULLABLE, STR_ARG_WRITE_TEST_COUNT );

  /**
   * Аргумент : Прирост значения при каждой передачи.
   */
  IAdminCmdArgDef ARG_WRITE_INCREMENT =
      new AdminCmdArgDef( "increment", DT_INTEGER_NULLABLE, STR_ARG_WRITE_TEST_INCREMENT );

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
  String ARG_VALIDATION_REPAIR_NAME    = VALIDATION_REPAIR.nmName();
  String ARG_VALIDATION_REPAIR_DESCR   = VALIDATION_REPAIR.description();
  String ARG_VALIDATION_REPAIR_DEFAULT = String.valueOf( VALIDATION_REPAIR.defaultValue().asBool() );

  String ARG_VALIDATION_FORCE_REPAIR_ID      = "force";
  String ARG_VALIDATION_FORCE_REPAIR_ALIAS   = "f";
  String ARG_VALIDATION_FORCE_REPAIR_NAME    = VALIDATION_FORCE_REPAIR.nmName();
  String ARG_VALIDATION_FORCE_REPAIR_DESCR   = VALIDATION_FORCE_REPAIR.description();
  String ARG_VALIDATION_FORCE_REPAIR_DEFAULT = String.valueOf( VALIDATION_FORCE_REPAIR.defaultValue().asBool() );

  String ARG_VALIDATION_IDS_ID    = "ids";
  String ARG_VALIDATION_IDS_ALIAS = "";
  String ARG_VALIDATION_IDS_NAME  = VALIDATION_GWIDS.nmName();
  String ARG_VALIDATION_IDS_DESCR = VALIDATION_GWIDS.description();

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
  String ARG_UNION_IDS_NAME  = UNION_GWIDS.nmName();
  String ARG_UNION_IDS_DESCR = UNION_GWIDS.description();

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
