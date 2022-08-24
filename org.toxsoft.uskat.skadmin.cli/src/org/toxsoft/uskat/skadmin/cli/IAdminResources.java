package org.toxsoft.uskat.skadmin.cli;

import static org.toxsoft.uskat.skadmin.cli.AdminColors.*;
import static org.toxsoft.uskat.skadmin.cli.IAdminAnsiConstants.*;

/**
 * Константы, локализуемые ресурсы реализации консоли.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IAdminResources {

  /**
   * Двоеточие
   */
  String CHAR_COLON = ":";

  /**
   * Точка с запятой
   */
  String CHAR_SEMICOLON = ";";

  /**
   * Маска ввода
   */
  Character CHAR_MASK = Character.valueOf( '*' );

  /**
   * Обратный слеш
   */
  String CHAR_SLASH = "/";

  /**
   * Коммерческая at
   */
  String CHAR_AT = "@";

  /**
   * Символ y
   */
  String CHAR_YES = "y";

  /**
   * Символ n
   */
  String CHAR_NO = "n";

  /**
   * Пробел
   */
  String CHAR_SPACE = " ";

  /**
   * ?
   */
  String CHAR_QUESTION = "?";

  /**
   * Файл истории введенных команд
   */
  String CONSOLE_HISTORY_FILE = "skadmin.history";

  /**
   * Количество элементов в истории
   */
  int CONSOLE_HISTORY_SIZE = 100;

  /**
   * Рекомендованная ширина(в символах) экранного буфера консоли
   */
  int RECOMMEND_SCREEN_BUFFER_WIDHT = 200;

  /**
   * Текст запуска
   */
  String MSG_CONSOLE_CMD_INIT    = "Инициализация команд консоли";
  String MSG_CONSOLE_INIT_FINISH = "Загрузка консоли завершена";
  String MSG_LOGO1               =
      " Консоль администратора uskat. Версия: %s. Компания " + COLOR_ID + "ТоксСофт" + COLOR_RESET + ". 1993-2022.";
  String MSG_LOGO2               = " Для справки введите " + COLOR_ID + "help" + COLOR_RESET + ".";
  String MSG_RCM_WIDTH           = COLOR_WARN + "Рекомендуемая ширина буфера консоли %d символов" + COLOR_RESET + ".";
  String MSG_PROMPT_START        = "[";
  String MSG_PROMPT_FINISH       = "]$";
  String MSG_RETRY_ENTRY         = "Повторить ввод значения";
  String MSG_ANSWER_YES          = "y";
  String MSG_CMD_START_EXECUTE   = "%s%s" + COLOR_RESET;
  String MSG_CMD_FINISH_ID       = "Команда " + COLOR_ID + "%s" + COLOR_RESET + ". ";
  String MSG_CMD_FINISH_RESULT   = "Результат: %s%s" + COLOR_RESET + ". ";
  String MSG_CONSOLE_FINISHED    = "skadmin завершил работу";

  /**
   * Идентификатор аргумента "пароль"
   */
  String PASSWORD_ARG_ID = "password";

  String MSG_ERR_CTX_MULTY_INPUTS      =
      "Входной параметр контекста при указании выходных параметров может быть только один";
  String MSG_ERR                       = COLOR_ERROR + "%s" + COLOR_RESET;
  String MSG_ERR_CMD_UNEXPECTED        = COLOR_ERROR + "Ошибка выполнения команды '%s'. Причина: '%s'" + COLOR_RESET;
  String MSG_ERR_READ_CONTEXT          =
      COLOR_ERROR + "Ошибка чтения файла контекста параметров %s. Причина: '%s'" + COLOR_RESET;
  String MSG_ERR_CMD_REJECT            = "Отказ пользователя от выполнения команды '%s'.";
  String MSG_ERR_ARG_NOT_FOUND         = "Для выполнения команды '%s' не указан аргумент '%s'.";
  String MSG_ERR_CONTEXT_ARG_NOT_FOUND = "В контексте не найден параметр: '%s'";
  String MSG_ERR_CONTEXT_OVER_INPUT    =
      "Фактическое количество параметров контекста превышает требуемое для выполнения команды: %d (требуется: %d)";
}
