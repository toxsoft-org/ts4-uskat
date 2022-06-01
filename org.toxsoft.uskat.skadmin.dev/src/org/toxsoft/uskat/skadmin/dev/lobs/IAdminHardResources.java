package org.toxsoft.uskat.skadmin.dev.lobs;

/**
 * Локализуемые ресурсы пакета.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IAdminHardResources {

  // ------------------------------------------------------------------------------------
  // AdminCmdListIds,AdminCmdExportClobs,AdminCmdImportClobs
  //
  String STR_ARG_PATTERN           =
      "Шаблон (java.util.regex.Pattern) идентификаторов из списка всех IdPair.toString(). По умолчанию: \".*\" (все идентификаторы)";
  String STR_ARG_OUTFILE           =
      "Имя файла в который выводится список идентификаторов. По умолчанию: \"data/clobs/outfile.ext.\"";
  String STR_ARG_OUTDIR            =
      "Выходная директория, в которой сохраняются CLOBы с именем файла IdPair.toString() и с указанным расширением EXT. По умолчанию: \"data/clobs\".";
  String STR_ARG_EXTENSION         = "Расширение файлов (без точки). По умолчанию: \"txt\".";
  String STR_ARG_FORCE             = "Не запрашивать подтверждение операций. По умолчанию: false";
  String STR_FILE_ALREADY_EXIST    = "Файл %s уже существует. Переписать существующий?";
  String STR_CLOB_ALREADY_EXIST    = "clob %s уже существует в системе. Переписать существующий?";
  String STR_CMD_CANCELLED_BY_USER = "Пользователь отменил выполнение команды: %s";
  String STR_EXPORT_CLOB           = "\n%s | [%d/%d] экспорт clob (%s): %s - %d(msec)";
  String STR_IMPORT_CLOB           = "\n%s | [%d/%d] импорт clob (%s): %s - %d(msec)";

  // ------------------------------------------------------------------------------------
  // AdminCmdListIds
  //
  String STR_CMD_LISTIDS = "Вывод  списка всех имеющихся идентификаторов.";

  // ------------------------------------------------------------------------------------
  // AdminCmdExportClobs
  //
  String STR_CMD_EXPORT_CLOBS = "Экспорт CLOBов в текстовые файлы.";

  // ------------------------------------------------------------------------------------
  // AdminCmdImportClobs
  //
  String STR_CMD_IMPORT_CLOBS = "Импорт CLOBов из текстовых файлов.";

  // ------------------------------------------------------------------------------------
  // Сообщения
  //

  String MSG_INFO_LINE =
      "-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n";
  String MSG_COMMA     = ",";
  String MSG_YES       = "Да";
  String MSG_NO        = "Нет";
  String MSG_CMD_TIME  = "Время выполнения команды: %d (мсек).";

  // ------------------------------------------------------------------------------------
  // Ошибки
  //
  String ERR_NOT_CONNECTION = "Установите соединение с s5-сервером (команда login)";
}
