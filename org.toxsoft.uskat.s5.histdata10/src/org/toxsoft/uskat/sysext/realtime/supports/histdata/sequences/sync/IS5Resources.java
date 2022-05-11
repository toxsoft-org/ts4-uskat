package org.toxsoft.uskat.sysext.realtime.supports.histdata.sequences.sync;

/**
 * Константы, локализуемые ресурсы
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IS5Resources {

  // ------------------------------------------------------------------------------------
  // Параметры запросов
  //
  String QPARAM_OBJID  = "objId";
  String QPARAM_DATAID = "dataId";

  String QPARAM_INFO_ID    = "infoId";
  String QPARAM_START_TIME = "startTime";
  String QPARAM_END_TIME   = "endTime";

  // ------------------------------------------------------------------------------------
  // Запросы
  //
  String QUERY_GET_ALL_INFO_IDS         = "S5HistDataInfoEntity.getAllInfoIds";
  String QUERY_GET_INFO_BY_OBJID_DATAID = "S5HistDataInfoEntity.getByObjIdDataId";

  String QUERY_GET_ENCLOSED_BLOCKS = "S5HistData.getEnclosedBlocks";
  String QUERY_GET_CROSSED_BLOCKS  = "S5HistData.getCrossedBlocks";

  String Q_WHERE_ENCLOSED = "WHERE  (block.infoId = :" + QPARAM_INFO_ID + ") AND ( block.startTime >= :"
      + QPARAM_START_TIME + ") AND ( block.endTime <= :" + QPARAM_END_TIME + ") order by block.startTime ";

  String Q_WHERE_CROSSED = "WHERE (block.infoId = :" + QPARAM_INFO_ID + ") AND ( ( block.startTime >= :"
      + QPARAM_START_TIME + ") AND ( block.startTime <= :" + QPARAM_END_TIME + ")  OR  ( block.endTime >= :"
      + QPARAM_START_TIME + ") AND ( block.endTime <= :" + QPARAM_END_TIME + ")  OR ( block.startTime <= :"
      + QPARAM_START_TIME + ") AND ( block.endTime >= :" + QPARAM_END_TIME + " ) ) order by block.startTime ";

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_AGGR_THREAD_FINISH =
      "Завершение потока агрегации хранимых данных [%d] %s. %s, blocks = %d, values = %d, aggregators = %d (values = %d ). Время: %d ms (create=%d). Сформированные последовательности:\n%s";

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_WRONG_OBJID                = "Cod-идентификатор %s не может быть использован для объекта objId = %d";
  String ERR_WRONG_TYPE                 =
      "Недопустимый тип данных для исторических значений %s. Разрешены только одиночные значения IDataValue";
  String ERR_TRY_DOUBLE_EDIT_START_TIME =
      "Попытка дважды изменить время последовательности %s. currStartTime = %s, newStartTime = %s";
  String ERR_NOT_IMPORT_DATA            = "%s. Нет данных для импорта";
  String ERR_CAST_VALUE                 = "%s. Невозможно конвертировать значение в тип %s";
  String ERR_CAST_VALUE_ACCURACY        = "%s. Невозможно конвертировать значение в тип %s (потеря точности)";
  String ERR_WRONG_IMPORT_CURSOR        =
      "Попытка установки курсора импорта за границами последовательности. startTime: %s, endTime: %s, cursorTime: %s";
  String ERR_NOT_CURSOR_IMPORT_DATA     = "%s. Нет данных для импорта из последовательности.";
  String ERR_MSG_NOT_IMPORT_DATA        =
      "%s. Невозможно установить курсор импорта значений на %s - в последовательности нет данных";

  String ERR_AGGR_READ_OUT_OF_MEMORY_TRY =
      "%s. Агрегация значений. Не хвататает памяти (JAVA HEAP) для чтения последовательности в интервале %s. Попытка %d через 100 мсек. %s";
  String ERR_AGGR_READ_OUT_OF_MEMORY     =
      "%s. Агрегация значений. Не хвататает памяти (JAVA HEAP) для чтения последовательности в интервале %s.";
  String ERR_AGGR_UNEXPECTED             =
      "%s. Агрегация значений. Неожиданная ошибка запроса данных последовательности в интервале %s. Причина: %s";
  String ERR_AGGR_TRY_READ_OVER          = "Превышено количество попыток чтения";
  String ERR_NO_HISTDATA                 = "Данное %s не является хранимым";
}
