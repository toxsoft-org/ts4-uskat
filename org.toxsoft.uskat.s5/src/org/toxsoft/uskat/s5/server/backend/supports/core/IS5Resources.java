package org.toxsoft.uskat.s5.server.backend.supports.core;

/**
 * Константы, локализуемые ресурсы .
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IS5Resources {

  /**
   * {@link S5BackendCoreConfig}
   */
  String STR_START_TIME_MIN   = "startTimeMin";
  String STR_START_TIME_MIN_D =
      "Минимальное время (секунды) в котором сервер находится в режиме {@link ES5ServerMode#STARTING}";

  String STR_START_TIME_MAX   = "startTimeMax";
  String STR_START_TIME_MAX_D =
      "Максимальное время (секунды) в котором сервер находится в режиме {@link ES5ServerMode#STARTING}. "                          //
          + "Сервер может быть досрочно переведен в режим {@link ES5ServerMode#WORKING} если после запуска сервера прошло больше\n"
          + "   * времени чем startTimeMin и текущий уровень загрузки меньше {@link #CLI_BOOSTED_AVERAGE}.\n"
          + "   * <p>\n"
          + "   * Если с момента запуска прошло больше времени чем startTimeMax, то сервер переводится в режим\n"
          + "   * {@link ES5ServerMode#BOOSTED} или {@link ES5ServerMode#OVERLOADED} в завимости от текущего уровня загрузки.";

  String STR_BOOSTED_AVERAGE   = "boostedAverage";
  String STR_BOOSTED_AVERAGE_D =
      "Уровень загрузки при котором s5-сервер может быть автоматически переключен в усиленный(форсаж) режим ({@link ES5ServerMode#BOOSTED})";

  String STR_OVERLOADED_AVERAGE   = "overloadedAverage";
  String STR_OVERLOADED_AVERAGE_D =
      "Уровень загрузки при котором s5-сервер может быть автоматически переключен в режим перегрузки ({@link ES5ServerMode#OVERLOADED})";

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
}
