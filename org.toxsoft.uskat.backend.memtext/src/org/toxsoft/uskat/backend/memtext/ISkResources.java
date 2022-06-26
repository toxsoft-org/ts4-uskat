package org.toxsoft.uskat.backend.memtext;

/**
 * Локализуемые ресурсы.
 *
 * @author hazard157
 */
@SuppressWarnings( "nls" )
interface ISkResources {

  /**
   * {@link IBackendMemtextConstants}
   */
  String STR_N_NOT_STORED_OBJ_CLASS_IDS = "Не хранимые классы";
  String STR_D_NOT_STORED_OBJ_CLASS_IDS = "Список идентификаторов классов, объекты которых не хранятся в файле";
  String STR_N_IS_EVENTS_STORED         = "Хранить события?";
  String STR_D_IS_EVENTS_STORED         = "Specifies if events history  will be stored permanently";
  String STR_N_MAX_EVENTS_COUNT         = "Max events";
  String STR_D_MAX_EVENTS_COUNT         = "Maximum number of events in history";
  String STR_N_IS_CMDS_STORED           = "Хранить Команды?";
  String STR_D_IS_CMDS_STORED           = "Specifies if events history  will be stored permanently";
  String STR_N_MAX_CMDS_COUNT           = "Max Commands";
  String STR_D_MAX_CMDS_COUNT           = "Maximum number of commands in history";

  /**
   * {@link MtbBackendToFile}
   */
  String STR_N_OP_FILE_PATH             = "Файл";
  String STR_D_OP_FILE_PATH             = "Файл для хранения текстового содержимого БД uskat";
  String STR_N_OP_AUTO_SAVE_SECS        = "Интервал (сек)";
  String STR_D_OP_AUTO_SAVE_SECS        = "Миниманое время между авто-сохранениями файла БД";
  String FMT_ERR_NO_FILE_NAME_SPECIFIED = "Не задан параметр %s: имя текстового файла хранения данных";

}
