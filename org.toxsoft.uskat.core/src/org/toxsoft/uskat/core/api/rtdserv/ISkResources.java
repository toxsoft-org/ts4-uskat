package org.toxsoft.uskat.core.api.rtdserv;

/**
 * Localizable resources.
 *
 * @author hazard157
 */
@SuppressWarnings( "nls" )
interface ISkResources {

  /**
   * {@link EQueryState}
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

}
