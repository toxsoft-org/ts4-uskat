package org.toxsoft.uskat.backend.memtext;

import org.toxsoft.core.txtproj.lib.*;

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
  String STR_N_HISTORY_DEPTH_HOURS      = "History hours";
  String STR_D_HISTORY_DEPTH_HOURS      = "RTdata history will be kept for specified number of hours";
  String STR_N_CURR_DATA_10MS_TICKS     = "CurrData 10ms ticks";
  String STR_D_CURR_DATA_10MS_TICKS     = "Current data will be checked and updated every specified ticks of 10 msec";

  /**
   * {@link MtbBackendToFile}
   */
  String STR_N_OP_FILE_PATH             = "Файл";
  String STR_D_OP_FILE_PATH             = "Файл для хранения текстового содержимого БД uskat";
  String STR_N_OP_AUTO_SAVE_SECS        = "Интервал (сек)";
  String STR_D_OP_AUTO_SAVE_SECS        = "Миниманое время между авто-сохранениями файла БД";
  String FMT_ERR_NO_FILE_NAME_SPECIFIED = "Не задан параметр %s: имя текстового файла хранения данных";

  /**
   * {@link MtbBackendToFileMetaInfo}
   */
  String STR_N_BACKEND_MEMTEXT_TO_FILE = "Meemtext to file";
  String STR_D_BACKEND_MEMTEXT_TO_FILE = "USkat data stored as the text file";

  /**
   * {@link MtbBackendToTsProj}
   */
  String STR_N_OP_PDU_ID    = "Раздел проекта";
  String STR_D_OP_PDU_ID    = "Идентификатор компоненты (раздела) файла проекта для хранения данных uskat";
  String STR_N_REF_PROJECT  = "Проекта";
  String STR_D_REF_PROJECT  = "Ссылка на экземпляр проекта " + ITsProject.class.getSimpleName();
  String FMT_ERR_INV_PDU_ID = "Неверный параметр %s: идентификатор раздела проекта должен быть ИД-путем";

}
