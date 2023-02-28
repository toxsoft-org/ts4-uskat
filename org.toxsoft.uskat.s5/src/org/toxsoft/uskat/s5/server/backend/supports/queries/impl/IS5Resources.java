package org.toxsoft.uskat.s5.server.backend.supports.queries.impl;

/**
 * Локализуемые ресурсы реализации.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IS5Resources {

  String STR_D_BACKEND_QUERIES = "Подджержка расширения бекенда: 'запросы к хранимым данным'";

  String STR_QUERY_RAW_VALUES   = "Запрос значений хранимых данных (JDBC)."; // Request raw values (JDBC)
  String STR_QUERY_RAW_EVENTS   = "Запрос событий (JDBC).";
  String STR_QUERY_RAW_COMMANDS = "Запрос команд (JDBC).";

  String STR_PROCESSING_RAW_VALUES   = "Обработка значений хранимых данных."; // "Processing raw values";
  String STR_PROCESSING_RAW_EVENTS   = "Обработка событий";
  String STR_PROCESSING_RAW_COMMANDS = "Обработка команд";

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_FUNC_THREAD_FINISH       =
      "%s. Агрегация значений. Неожиданная ошибка запроса данных последовательности в интервале %s. Причина: %s";
  String MSG_FUNC_READ_SEQUENCE_START =
      "Запуск задачи обработки последовательностей значений данных. Количество данных (count = %d), количество функций ( functions = %d )"; //$NON-NLS-1$
  String MSG_FUNC_READ_SEQUENCE_TIME  = "Чтение обработанных последовательностей (count = %d) проведено за %d (мсек)";                      //$NON-NLS-1$
  String MSG_EXEC_QUERY               =
      "execQuery(...): Выполнение запроса данных %s (%s. params = %d, values = %d, raw = %d) за %d (%d/%d/%d) мсек.";
  String MSG_CREATE_QUERY             = "createQuery(...): Создание запроса данных %s.";
  String MSG_PREPARE_QUERY            = "prepareQuery(...): Подготовка запроса данных %s.";
  String MSG_CLOSE_QUERY              = "close(...): завершение запроса данных %s";
  String MSG_BY_TIMEOUT               = "timeout";

  String MSG_SEND_RESULT_VALUES      = "Передача результатов запроса";
  String MSG_SEND_LAST_RESULT_VALUES = "Завершение передачи результатов запроса";

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_FUNC_UNEXPECTED   =
      "%s. Обработка последовательности значений. Неожиданная ошибка запроса данных последовательности в интервале %s. Причина: %s";
  String ERR_WRONG_TYPE        = "Для выбранной обработки значений недопустимый тип значения %s";
  String ERR_OUTPUT_SIZE_LIMIT =
      "При формировании отчета по параметру %s, в интервале %s, с шагом %d (сек), было сформировано более %d значений. Повторите запрос с меньшим интервалом времени или с большим шагом агрегации";
  String ERR_CANCEL_QUERY      = "%s: Отмена выполнения запроса данных %s (%s)";
  String ERR_EXEC_QUERY        = "execQuery(...): Ошибка выполнения запроса данных %s. Причина: %s";
  String ERR_CANCEL_BY_AUTHOR  = "%s, cancel query execution by %s.";
  String ERR_UNEXPECTED_ERROR  = "%s, unexpected error: %s.";
}
