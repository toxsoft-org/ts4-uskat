package org.toxsoft.uskat.skadmin.dev.objects;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.skadmin.core.plugins.AdminPluginUtils.*;
import static org.toxsoft.uskat.skadmin.dev.objects.IAdminHardResources.*;

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

  String MULTI = "*";

  /**
   * Префикс идентификаторов команд и их алиасов плагина
   */
  String CMD_PATH_PREFIX = AdminPluginDev.DEV_CMD_PATH + "objects.";

  // ------------------------------------------------------------------------------------
  // AdminCmdGetAttr, AdminCmdSetAttr
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
   * Аргумент : Идентификатор атрибута
   */
  IAdminCmdArgDef ARG_ATTRID = new AdminCmdArgDef( "attrId", DT_STRING_NULLABLE, STR_ARG_ATTR );

  // ------------------------------------------------------------------------------------
  // AdminCmdGetAttr
  //
  String CMD_GET_ATTR_ID    = CMD_PATH_PREFIX + "getAttr";
  String CMD_GET_ATTR_ALIAS = EMPTY_STRING;
  String CMD_GET_ATTR_NAME  = EMPTY_STRING;
  String CMD_GET_ATTR_DESCR = STR_CMD_GET_ATTR;

  // ------------------------------------------------------------------------------------
  // AdminCmdSetAttr
  //
  String CMD_SET_ATTR_ID    = CMD_PATH_PREFIX + "setAttr";
  String CMD_SET_ATTR_ALIAS = EMPTY_STRING;
  String CMD_SET_ATTR_NAME  = EMPTY_STRING;
  String CMD_SET_ATTR_DESCR = STR_CMD_SET_ATTR;

  /**
   * Аргумент : Значение атрибута
   */
  IAdminCmdArgDef ARG_WRITE_VALUE = new AdminCmdArgDef( "value", PT_NONE, STR_ARG_SET_ATTR_VALUE );

  // ------------------------------------------------------------------------------------
  // AdminCmdRemoveObject
  //
  String CMD_REMOVE_OBJ_ID    = CMD_PATH_PREFIX + "removeObject";
  String CMD_REMOVE_OBJ_ALIAS = EMPTY_STRING;
  String CMD_REMOVE_OBJ_NAME  = EMPTY_STRING;
  String CMD_REMOVE_OBJ_DESCR = STR_CMD_REMOVE_OBJ;

}
