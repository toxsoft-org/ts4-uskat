package org.toxsoft.uskat.skadmin.dev.batch;

/**
 * Локализуемые ресурсы пакета.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IAdminHardResources {

  // ------------------------------------------------------------------------------------
  // AdminCmdBatchRead
  //
  String STR_CMD_BATCH_READ        = "Пакетное чтение данных системы в контейнер {@link IDpuContainer}.";
  String STR_CMD_BATCH_READ_RESULT = "Контейнер с полученными данными системы.";
  String STR_DPU_CONTAINER_ID      = "dpuContainer";
  String STR_DPU_CONTAINER_DESCR   = "Контейнер с данными системы";

  String STR_ARG_INCLUDE_SYSTEM_ENTITIES =
      "Признак чтения также системных сужностей (например, объектов сессии). По умолчанию: false";
  String STR_ARG_INCLUDE_CLASS_INFOS     =
      "Признак включния в контейнер описании выбранных классов. По умолчанию: true";
  String STR_ARG_INCLUDE_OBJECTS         =
      "Признак включения в контейнер описании выбранных объектов. По умолчанию: true";
  String STR_ARG_INCLUDE_LINKS           =
      "Признак включения в контейнер описании выбранных связей. По умолчанию: true";
  String STR_ARG_ORPHAN_CLASSES          = "Режим включения сиротских классов в контейнер. По умолчанию: ENRICH";
  String STR_ARG_ORPHAN_LINKS            = "Режим включения сиротских классов в контейнер. По умолчанию: REMOVE";
  String STR_ARG_CLASS_FILTER            =
      "Фильтр выборки классов по идентификаторам в формате TsCombiFilterParamsKeeper. По умолчанию: ALL";
  String STR_ARG_TYPE_FILTER             =
      "Фильтр выборки типов по идентификаторам в формате TsCombiFilterParamsKeeper. По умолчанию: ALL";
  String STR_ARG_CLOB_FILTER             =
      "Фильтр выборки CLOB по идентификаторам в формате TsCombiFilterParamsKeeper. По умолчанию: DV_NONE";
  String STR_ARG_FILE                    = "Текстовый файл, в котором сохраняется контейнер.";

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
