package org.toxsoft.uskat.backend.memtext;

/**
 * Локализуемые ресурсы.
 *
 * @author goga
 */
@SuppressWarnings( "nls" )
interface ISkResources {

  /**
   * {@link MtbBackendToFile}
   */
  String STR_N_OP_FILE_PATH             = "Файл";
  String STR_D_OP_FILE_PATH             = "Файл для жранения текстового содержимого БД uskat";
  String STR_N_OP_AUTO_SAVE_SECS        = "Интервал (сек)";
  String STR_D_OP_AUTO_SAVE_SECS        = "Миниманое время между авто-сохранениями файла БД";
  String STR_N_NOT_STORED_OBJ_CLASS_IDS = "Не хранимые классы";
  String STR_D_NOT_STORED_OBJ_CLASS_IDS = "Список идентификаторов классов, объекты которых не хранятся в файле";
  String FMT_ERR_NO_FILE_NAME_SPECIFIED = "Не задан параметр %s: имя текстового файла хранения данных";

}
