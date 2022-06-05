package org.toxsoft.uskat.skadmin.dev.lobs;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.skadmin.core.plugins.AdminPluginUtils.*;
import static org.toxsoft.uskat.skadmin.dev.lobs.IAdminHardResources.*;

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
   * Кодировка текстовых файлов по умолчанию
   */
  String CHARSET_DEFAULT = "UTF-8";

  /**
   * '*'
   */
  String MULTI = "*";

  /**
   * Префикс идентификаторов команд и их алиасов плагина
   */
  String CMD_PATH_PREFIX = AdminPluginDev.DEV_CMD_PATH + "lobs.";

  /**
   * Значение по умолчанию для аргумента: {@link #ARG_PATTERN}.
   */
  String ARG_PATTERN_DEFAULT = ".*";

  /**
   * Значение по умолчанию для аргумента: {@link #ARG_OUTFILE}.
   */
  String ARG_OUTFILE_DEFAULT = "data/lobs/outfile.ext";

  /**
   * Значение по умолчанию для аргумента: {@link #ARG_OUTDIR}.
   */
  String ARG_OUTDIR_DEFAULT = "data/lobs";

  /**
   * Значение по умолчанию для аргумента: {@link #ARG_EXTENSION}.
   */
  String ARG_EXTENSION_DEFAULT = "txt";

  // ------------------------------------------------------------------------------------
  // AdminCmdListIds,AdminCmdExportClobs,AdminCmdImportClobs
  //
  /**
   * Аргумент: Шаблон ({@link java.util.regex.Pattern}) идентификаторов из списка всех IdPair.toString(). Если параметр
   * не указан, то выводятся все идентификаторы.
   */
  IAdminCmdArgDef ARG_PATTERN = new AdminCmdArgDef( "pattern", DT_STRING_NULLABLE, STR_ARG_PATTERN );

  /**
   * Аргумент: Имя файла в который выводится список идентификаторов. По умолчанию: {@link #ARG_OUTFILE_DEFAULT}.
   */
  IAdminCmdArgDef ARG_OUTFILE = new AdminCmdArgDef( "outfile", DT_STRING_NULLABLE, STR_ARG_OUTFILE );

  /**
   * Аргумент: Выходная директория, в которой сохраняются CLOBы с именем файла IdPair.toString() и с указанным
   * расширением EXT. По умолчанию: {@link #ARG_OUTDIR_DEFAULT}.
   */
  IAdminCmdArgDef ARG_OUTDIR = new AdminCmdArgDef( "outdir", DT_STRING_NULLABLE, STR_ARG_OUTDIR );

  /**
   * Аргумент: Расширение файлов (без точки). По умолчанию: {@link #ARG_EXTENSION_DEFAULT}.
   */
  IAdminCmdArgDef ARG_EXTENSION = new AdminCmdArgDef( "extension", DT_STRING_NULLABLE, STR_ARG_EXTENSION );

  /**
   * Аргумент: Не запрашивать подтверждение операций. По умолчанию: false.
   */
  IAdminCmdArgDef ARG_FORCE = new AdminCmdArgDef( "force", DT_BOOLEAN_NULLABLE, STR_ARG_FORCE );

  // ------------------------------------------------------------------------------------
  // AdminCmdListIds
  //
  String CMD_LISTIDS_ID    = CMD_PATH_PREFIX + "listIds";
  String CMD_LISTIDS_ALIAS = EMPTY_STRING;
  String CMD_LISTIDS_NAME  = EMPTY_STRING;
  String CMD_LISTIDS_DESCR = STR_CMD_LISTIDS;

  // ------------------------------------------------------------------------------------
  // AdminCmdExportClobs
  //
  String CMD_EXPORT_CLOBS_ID    = CMD_PATH_PREFIX + "exportClobs";
  String CMD_EXPORT_CLOBS_ALIAS = EMPTY_STRING;
  String CMD_EXPORT_CLOBS_NAME  = EMPTY_STRING;
  String CMD_EXPORT_CLOBS_DESCR = STR_CMD_EXPORT_CLOBS;

  // ------------------------------------------------------------------------------------
  // AdminCmdImportClobs
  //
  String CMD_IMPORT_CLOBS_ID    = CMD_PATH_PREFIX + "importClobs";
  String CMD_IMPORT_CLOBS_ALIAS = EMPTY_STRING;
  String CMD_IMPORT_CLOBS_NAME  = EMPTY_STRING;
  String CMD_IMPORT_CLOBS_DESCR = STR_CMD_IMPORT_CLOBS;

}
