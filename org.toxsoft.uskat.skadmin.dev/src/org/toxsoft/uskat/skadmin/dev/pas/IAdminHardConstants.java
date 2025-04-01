package org.toxsoft.uskat.skadmin.dev.pas;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.skadmin.core.plugins.AdminPluginUtils.*;
import static org.toxsoft.uskat.skadmin.dev.pas.IAdminHardResources.*;

import org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants;
import org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils;
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

  /**
   * Префикс идентификаторов команд и их алиасов плагина
   */
  String CMD_PATH_PREFIX = AdminPluginDev.CMD_PATH + "pas.";

  /**
   * Значение по умолчанию для аргумента: {@link #ARG_PAS_HOST}.
   */
  String ARG_PAS_HOST_DEFAULT = "localhost";

  /**
   * Значение по умолчанию для аргумента: {@link #ARG_PAS_PORT}.
   */
  int ARG_PAS_PORT_DEFAULT = 2194;

  /**
   * Значение по умолчанию для аргумента: {@link #ARG_PAS_CREATE_TIMEOUT}.
   */
  int ARG_PAS_CREATE_TIMEOUT_DEFAULT = 10000;

  /**
   * Значение по умолчанию для аргумента: {@link #ARG_PAS_FAILURE_TIMEOUT}.
   */
  int ARG_PAS_FAILURE_TIMEOUT_DEFAULT = 3000;

  /**
   * Значение по умолчанию для аргумента: {@link #ARG_PAS_WRITE_TIMEOUT}.
   */
  int ARG_PAS_WRITE_TIMEOUT_DEFAULT = 100;

  /**
   * Значение по умолчанию для аргумента: {@link #ARG_PAS_REQUEST_TIMEOUT}.
   */
  int ARG_PAS_REQUEST_TIMEOUT_DEFAULT = 10000;

  // ------------------------------------------------------------------------------------
  // AdminCmdPasRequest,AdminCmdPasNotify
  //
  /**
   * Аргумент: Имя вызываемого метода.
   */
  IAdminCmdArgDef ARG_PAS_METHOD = new AdminCmdArgDef( "method", IAvMetaConstants.DDEF_STRING, STR_ARG_METHOD );

  /**
   * Аргумент: Параметры вызываемого метода.
   */
  IAdminCmdArgDef ARG_PAS_PARAMS = new AdminCmdArgDef( "params", PlexyValueUtils.PT_OPSET, STR_ARG_PARAMS );

  // ------------------------------------------------------------------------------------
  // AdminCmdPasConnect
  //
  String CMD_PAS_CONNECT_ID    = CMD_PATH_PREFIX + "pasConnect";
  String CMD_PAS_CONNECT_ALIAS = EMPTY_STRING;
  String CMD_PAS_CONNECT_NAME  = EMPTY_STRING;
  String CMD_PAS_CONNECT_DESCR = STR_CMD_PAS_CONNECT;

  /**
   * Аргумент: Имя хоста или IP адрес PAS-сервера.
   */
  IAdminCmdArgDef ARG_PAS_HOST = new AdminCmdArgDef( "host", DT_STRING_NULLABLE, STR_ARG_HOST );

  /**
   * Аргумент: Порт PAS-сервера.
   */
  IAdminCmdArgDef ARG_PAS_PORT = new AdminCmdArgDef( "port", DT_INTEGER_NULLABLE, STR_ARG_HOST );

  /**
   * Аргумент: Таймаут (мсек) подключения к PAS.
   */
  IAdminCmdArgDef ARG_PAS_CREATE_TIMEOUT =
      new AdminCmdArgDef( "createTimeout", DT_INTEGER_NULLABLE, STR_ARG_FAILURE_TIMEOUT );

  /**
   * Аргумент: Таймаут (мсек) обрыва связи с PAS.
   */
  IAdminCmdArgDef ARG_PAS_FAILURE_TIMEOUT =
      new AdminCmdArgDef( "failureTimeout", DT_INTEGER_NULLABLE, STR_ARG_FAILURE_TIMEOUT );

  /**
   * Аргумент: Таймаут (мсек) записи в PAS.
   */
  IAdminCmdArgDef ARG_PAS_WRITE_TIMEOUT =
      new AdminCmdArgDef( "writeTimeout", DT_INTEGER_NULLABLE, STR_ARG_WRITE_TIMEOUT );

  // ------------------------------------------------------------------------------------
  // AdminCmdPasRequest
  //
  String CMD_PAS_REQUEST_ID    = CMD_PATH_PREFIX + "request";
  String CMD_PAS_REQUEST_ALIAS = EMPTY_STRING;
  String CMD_PAS_REQUEST_NAME  = EMPTY_STRING;
  String CMD_PAS_REQUEST_DESCR = STR_CMD_PAS_REQUEST;

  /**
   * Аргумент: Имя файла в который сохраняется ответ.
   */
  IAdminCmdArgDef ARG_PAS_REQUEST_FILENAME =
      new AdminCmdArgDef( "filename", DT_STRING_NULLABLE, STR_ARG_REQUEST_FILENAME );

  /**
   * Аргумент: Таймаут (мсек) выполнения метода.
   */
  IAdminCmdArgDef ARG_PAS_REQUEST_TIMEOUT =
      new AdminCmdArgDef( "timeout", DT_INTEGER_NULLABLE, STR_ARG_REQUEST_TIMEOUT );

  /**
   * Аргумент: Требование выводить на экран принятые данные.
   */
  IAdminCmdArgDef ARG_PAS_REQUEST_IO_LOGGER =
      new AdminCmdArgDef( "ioLogger", DT_BOOLEAN_NULLABLE, STR_ARG_REQUEST_IO_LOGGER );

  // ------------------------------------------------------------------------------------
  // AdminCmdPasNotify
  //
  String CMD_PAS_NOTIFY_ID    = CMD_PATH_PREFIX + "notify";
  String CMD_PAS_NOTIFY_ALIAS = EMPTY_STRING;
  String CMD_PAS_NOTIFY_NAME  = EMPTY_STRING;
  String CMD_PAS_NOTIFY_DESCR = STR_CMD_PAS_NOTIFY;

  // ------------------------------------------------------------------------------------
  // AdminCmdPasClose
  //
  String CMD_PAS_CLOSE_ID    = CMD_PATH_PREFIX + "pasClose";
  String CMD_PAS_CLOSE_ALIAS = EMPTY_STRING;
  String CMD_PAS_CLOSE_NAME  = EMPTY_STRING;
  String CMD_PAS_CLOSE_DESCR = STR_CMD_PAS_CLOSE;
}
