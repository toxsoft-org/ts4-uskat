package org.toxsoft.uskat.skadmin.cli;

/**
 * Константы для управления ansi-терминалом.
 * <p>
 * Источник: http://en.wikipedia.org/wiki/ANSI_escape_code
 *
 * @author mvk
 */
@SuppressWarnings( { "nls", "javadoc" } )
public interface IAdminAnsiConstants {

  /**
   * Символ начала escape-последовательности
   */
  int CHAR_ANSI_START = 27;

  /**
   * Символ завершение escape-последовательности
   */
  int CHAR_ANSI_FINISH = 109;

  /**
   * Еscape-последовательность: перевести каретку в начало
   */
  String ESCAPE_CURSOR_TO_START = "\r";
  /**
   * Еscape-последовательность: перевести каретку в указанную позицию
   */
  String ESCAPE_CURSOR_TO_AT = "G";

  /**
   * Еscape-последовательность: Очистить экран
   */
  String ESCAPE_CLEAR_SCREAN = "2J";

  // ------------------------------------------------------------------------------------
  // Control Sequence Introducer or Control Sequence Initiator (CSI)
  //
  String CSI = "\u001B[";

  // ------------------------------------------------------------------------------------
  // Установить все цвета в значения по умолчанию
  //
  String COLOR_RESET = CSI + "0m";

  // ------------------------------------------------------------------------------------
  // Цвета
  //
  int BLACK = 0;
  int RED = 1;
  int GREEN = 2;
  int YELLOW = 3;
  int BLUE = 4;
  int MAGENTA = 5;
  int CYAN = 6;
  int WHITE = 7;

  // ------------------------------------------------------------------------------------
  // Цвет текста (Normal)
  //
  int FORE = 30;
  String NORMAL = "m";
  String CLR_FORE_BLACK = CSI + (FORE + BLACK) + NORMAL;
  String CLR_FORE_RED = CSI + (FORE + RED) + NORMAL;
  String CLR_FORE_GREEN = CSI + (FORE + GREEN) + NORMAL;
  String CLR_FORE_YELLOW = CSI + (FORE + YELLOW) + NORMAL;
  String CLR_FORE_BLUE = CSI + (FORE + BLUE) + NORMAL;
  String CLR_FORE_MAGENTA = CSI + (FORE + MAGENTA) + NORMAL;
  String CLR_FORE_CYAN = CSI + (FORE + CYAN) + NORMAL;
  String CLR_FORE_WHITE = CSI + (FORE + WHITE) + NORMAL;

  // ------------------------------------------------------------------------------------
  // Цвет текста (Bright)
  //
  String BRIGHT = ";1m";
  String CLR_FORE_BRIGHT_BLACK = CSI + (FORE + BLACK) + BRIGHT;
  String CLR_FORE_BRIGHT_RED = CSI + (FORE + RED) + BRIGHT;
  String CLR_FORE_BRIGHT_GREEN = CSI + (FORE + GREEN) + BRIGHT;
  String CLR_FORE_BRIGHT_YELLOW = CSI + (FORE + YELLOW) + BRIGHT;
  String CLR_FORE_BRIGHT_BLUE = CSI + (FORE + BLUE) + BRIGHT;
  String CLR_FORE_BRIGHT_MAGENTA = CSI + (FORE + MAGENTA) + BRIGHT;
  String CLR_FORE_BRIGHT_CYAN = CSI + (FORE + CYAN) + BRIGHT;
  String CLR_FORE_BRIGHT_WHITE = CSI + (FORE + WHITE) + BRIGHT;

  // ------------------------------------------------------------------------------------
  // Цвета фона (Normal)
  //
  int BACK = 40;
  String CLR_BACK_BLACK = CSI + (BACK + BLACK) + NORMAL;
  String CLR_BACK_RED = CSI + (BACK + RED) + NORMAL;
  String CLR_BACK_GREEN = CSI + (BACK + GREEN) + NORMAL;
  String CLR_BACK_YELLOW = CSI + (BACK + YELLOW) + NORMAL;
  String CLR_BACK_BLUE = CSI + (BACK + BLUE) + NORMAL;
  String CLR_BACK_MAGENTA = CSI + (BACK + MAGENTA) + NORMAL;
  String CLR_BACK_CYAN = CSI + (BACK + CYAN) + NORMAL;
  String CLR_BACK_WHITE = CSI + (BACK + WHITE) + NORMAL;

  // ------------------------------------------------------------------------------------
  // Цвет фона (Bright)
  //
  String CLR_BACK_BRIGHT_BLACK = CSI + (BACK + BLACK) + BRIGHT;
  String CLR_BACK_BRIGHT_RED = CSI + (BACK + RED) + BRIGHT;
  String CLR_BACK_BRIGHT_GREEN = CSI + (BACK + GREEN) + BRIGHT;
  String CLR_BACK_BRIGHT_YELLOW = CSI + (BACK + YELLOW) + BRIGHT;
  String CLR_BACK_BRIGHT_BLUE = CSI + (BACK + BLUE) + BRIGHT;
  String CLR_BACK_BRIGHT_MAGENTA = CSI + (BACK + MAGENTA) + BRIGHT;
  String CLR_BACK_BRIGHT_CYAN = CSI + (BACK + CYAN) + BRIGHT;
  String CLR_BACK_BRIGHT_WHITE = CSI + (BACK + WHITE) + BRIGHT;

  // ------------------------------------------------------------------------------------
  // Цвет сообщения INFO
  //
  // String COLOR_INFO = CLR_FORE_GREEN;

  // ------------------------------------------------------------------------------------
  // Цвет сообщения WARNING
  //
  // String COLOR_WARN = CLR_FORE_YELLOW;

  // ------------------------------------------------------------------------------------
  // Цвет сообщения ERROR
  //
  // String COLOR_ERROR = CLR_FORE_BRIGHT_RED;

  // ------------------------------------------------------------------------------------
  // Цвет единичного параметра контекста
  //
  // String COLOR_SINGLE_REF = CLR_FORE_BRIGHT_YELLOW;

  // ------------------------------------------------------------------------------------
  // Цвет значения элемента списка параметров контекста
  //
  // String COLOR_REF_LIST = CLR_FORE_BRIGHT_YELLOW;

  // ------------------------------------------------------------------------------------
  // Цвет идентификатора
  //
  // String COLOR_ID = CLR_FORE_BRIGHT_WHITE;

  // ------------------------------------------------------------------------------------
  // Цвет единичного значения
  //
  // String COLOR_SINGLE_VALUE = CLR_FORE_BRIGHT_CYAN;

  // ------------------------------------------------------------------------------------
  // Цвет значения элемента списка
  //
  // String COLOR_VALUE_LIST = CLR_FORE_CYAN;

}
