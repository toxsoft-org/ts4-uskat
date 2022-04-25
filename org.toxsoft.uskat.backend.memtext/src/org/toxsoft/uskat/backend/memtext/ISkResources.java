package org.toxsoft.uskat.backend.memtext;

/**
 * Локализуемые ресурсы.
 *
 * @author goga
 */
@SuppressWarnings( "nls" )
interface ISkResources {

  /**
   * {@link SkBackendMemtextFile}
   */
  String STR_N_OP_FILE_NAME             = "Файл";
  String STR_D_OP_FILE_NAME             = "Файл для жранения текстового содержимого БД uskat";
  String STR_N_OP_AUTO_SAVE_SECS        = "Интервал (сек)";
  String STR_D_OP_AUTO_SAVE_SECS        = "Миниманое время между авто-сохранениями файла БД";
  String STR_N_NOT_STORED_OBJ_CLASS_IDS = "Не хранимые классы";
  String STR_D_NOT_STORED_OBJ_CLASS_IDS = "Список идентификаторов классов, объекты которых не хранятся в файле";

}
