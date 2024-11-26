package org.toxsoft.uskat.s5.cron.skadmin.cron;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.s5.cron.skadmin.cron.IAdminHardResources.*;
import static org.toxsoft.uskat.skadmin.core.plugins.AdminPluginUtils.*;

import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;
import org.toxsoft.core.tslib.gw.ugwi.UgwiList;
import org.toxsoft.uskat.s5.cron.skadmin.AdminPluginCron;
import org.toxsoft.uskat.skadmin.core.IAdminCmdArgDef;
import org.toxsoft.uskat.skadmin.core.impl.AdminCmdArgDef;

/**
 * Константы пакета.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IAdminHardConstants {

  /**
   * Префикс идентификаторов команд и их алиасов плагина.
   */
  String CMD_PATH_PREFIX = AdminPluginCron.SCHEDULES_CMD_PATH;

  /**
   * Аргумент : требование на все вопросы отвечать "yes".
   */
  IAdminCmdArgDef ARG_YES_ID = new AdminCmdArgDef( "y", DT_BOOLEAN_FALSE, STR_ARG_YES );

  // ------------------------------------------------------------------------------------
  // AdminCmdListSchedules
  //
  String CMD_LIST_SCHEDULES_ID    = CMD_PATH_PREFIX + "listSchedules";
  String CMD_LIST_SCHEDULES_ALIAS = EMPTY_STRING;
  String CMD_LIST_SCHEDULES_NAME  = EMPTY_STRING;
  String CMD_LIST_SCHEDULES_DESCR = STR_CMD_LIST_SCHEDULES;

  // ------------------------------------------------------------------------------------
  // AdminCmdRemoveSchedule
  //
  String CMD_REMOVE_SCHEDULE_ID    = CMD_PATH_PREFIX + "removeSchedule";
  String CMD_REMOVE_SCHEDULE_ALIAS = EMPTY_STRING;
  String CMD_REMOVE_SCHEDULE_NAME  = EMPTY_STRING;
  String CMD_REMOVE_SCHEDULE_DESCR = STR_CMD_REMOVE_SCHEDULE;

  // ------------------------------------------------------------------------------------
  // AdminCmdAddSchedule
  //
  String CMD_ADD_SCHEDULE_ID    = CMD_PATH_PREFIX + "addSchedule";
  String CMD_ADD_SCHEDULE_ALIAS = EMPTY_STRING;
  String CMD_ADD_SCHEDULE_NAME  = EMPTY_STRING;
  String CMD_ADD_SCHEDULE_DESCR = STR_CMD_ADD_SCHEDULE;

  /**
   * Аргумент : Строковый идентификатор расписания.
   */
  IAdminCmdArgDef ARG_ID = new AdminCmdArgDef( "id", DT_STRING, STR_ARG_ID );

  /**
   * Аргумент : Имя расписания.
   */
  IAdminCmdArgDef ARG_NAME = new AdminCmdArgDef( "name", DT_STRING_EMPTY, STR_ARG_NAME );

  /**
   * Аргумент : Описание расписания.
   */
  IAdminCmdArgDef ARG_DESCR = new AdminCmdArgDef( "descr", DT_STRING_EMPTY, STR_ARG_DESCR );

  /**
   * Аргумент : Секунды.
   */
  IAdminCmdArgDef ARG_SECONDS = new AdminCmdArgDef( "seconds", createType( STRING, avStr( "0" ) ), STR_ARG_SECONDS );

  /**
   * Аргумент : Минуты.
   */
  IAdminCmdArgDef ARG_MINUTES = new AdminCmdArgDef( "minutes", createType( STRING, avStr( "0" ) ), STR_ARG_MINUTES );

  /**
   * Аргумент : Часы.
   */
  IAdminCmdArgDef ARG_HOURS = new AdminCmdArgDef( "hours", createType( STRING, avStr( "*" ) ), STR_ARG_HOURS );

  /**
   * Аргумент : Дни месяца.
   */
  IAdminCmdArgDef ARG_DAYS_OF_MONTH =
      new AdminCmdArgDef( "daysOfMonth", createType( STRING, avStr( "*" ) ), STR_ARG_DAYS_OF_MONTH );

  /**
   * Аргумент : Месяцы.
   */
  IAdminCmdArgDef ARG_MONTHS = new AdminCmdArgDef( "months", createType( STRING, avStr( "*" ) ), STR_ARG_MONTHS );

  /**
   * Аргумент : Дни недели.
   */
  IAdminCmdArgDef ARG_DAYS_OF_WEEK =
      new AdminCmdArgDef( "daysOfWeek", createType( STRING, avStr( "*" ) ), STR_ARG_DAYS_OF_WEEK );

  /**
   * Аргумент : Годы.
   */
  IAdminCmdArgDef ARG_YEARS = new AdminCmdArgDef( "years", createType( STRING, avStr( "*" ) ), STR_ARG_YEARS );

  /**
   * Аргумент : Часовой пояс.
   */
  IAdminCmdArgDef ARG_TIMEZONE = new AdminCmdArgDef( "timezone", createType( STRING, AV_STR_EMPTY ), STR_ARG_TIMEZONE );

  /**
   * Аргумент : Метка начала действия расписания.
   */
  IAdminCmdArgDef ARG_START = new AdminCmdArgDef( "start",
      createType( TIMESTAMP, avTimestamp( TimeUtils.readTimestamp( "2000-01-01" ) ) ), STR_ARG_START );

  /**
   * Аргумент : Метка завершения действия расписания.
   */
  IAdminCmdArgDef ARG_END = new AdminCmdArgDef( "end",
      createType( TIMESTAMP, avTimestamp( TimeUtils.readTimestamp( "2050-01-01" ) ) ), STR_ARG_END );

  /**
   * Аргумент : список идентификаторов ресурсов системы связанных с расписанием
   */
  IAdminCmdArgDef ARG_UGWIES = new AdminCmdArgDef( "ugwies",
      createType( STRING, avStr( UgwiList.KEEPER.ent2str( new UgwiList() ) ) ), STR_ARG_UGWIES );

}
