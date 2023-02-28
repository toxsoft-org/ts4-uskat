package org.toxsoft.uskat.s5.server.backend.addons.queries;

import org.toxsoft.uskat.core.api.hqserv.ISkHistoryQueryServiceConstants;

/**
 * Localizable resources.
 *
 * @author hazard157
 */
@SuppressWarnings( "nls" )
interface IS5Resources {

  /**
   * {@link ES5QueriesConvoyState}
   */
  String STR_N_UNPREPARED = "Не определен";
  String STR_D_UNPREPARED = "Начальное состояние";
  String STR_N_PREPARED   = "Подготовлен";
  String STR_D_PREPARED   = "Был выполнен ISkHistDataQuery#prepare(IGwidList) - запрос готов для выполнения";
  String STR_N_EXECUTING  = "Выполняется";
  String STR_D_EXECUTING  = "Запрос выполняется";
  String STR_N_READY      = "Готов";
  String STR_D_READY      = "Данные по запросу успешно получены";
  String STR_N_FAILED     = "Ошибка";
  String STR_D_FAILED     = "Данные не удалось получить (таймаут, нет связи и т.п.)";
  String STR_N_CLOSED     = "Закрыт";
  String STR_D_CLOSED     = "Уже был вызван close(), объект запроса (ISkHistDataQuery) следует выбросить";

  String STR_N_JDBC       = "Запрос значений";
  String STR_D_JDBC       = "Запрос(JDBC) значений из базы данных";
  String STR_N_PROCESSING = "Обработка значений";
  String STR_D_PROCESSING = "Обработка/агрегация значений базы данных.";

  /**
   * {@link S5BaQueriesConvoy}
   */
  String ERR_QUERY_INVALID_STATE = "%s. query invalid state: %s";
  String ERR_QUERY_TIMEOUT       = "Cancel query by timeout error. Try change -"
      + ISkHistoryQueryServiceConstants.OP_SK_MAX_EXECUTION_TIME.id() + " value to up (%d))";
  String MSG_BY_CLOSE            = "close";
  String MSG_REQUEST_COMPLETED   = "request completed";
  String ERR_CANCEL_QUERY        = "State: %s. Cancel query execution by %s";
  String ERR_UNEXPECTED_ERROR    = "State: %s. Unexpected error: %s";
  String MSG_BY_USER             = "user";

}
