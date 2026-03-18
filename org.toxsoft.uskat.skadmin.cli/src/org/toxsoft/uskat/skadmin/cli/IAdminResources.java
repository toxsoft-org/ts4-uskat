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
  String CHAR_COLON = ":";//$NON-NLS-1$

  /**
   * Точка с запятой
   */
  String CHAR_SEMICOLON = ";";//$NON-NLS-1$

  /**
   * Маска ввода
   */
  Character CHAR_MASK = Character.valueOf( '*' );

  /**
   * Обратный слеш
   */
  String CHAR_SLASH = "/";//$NON-NLS-1$

  /**
   * Коммерческая at
   */
  String CHAR_AT = "@";//$NON-NLS-1$

  /**
   * Символ y
   */
  String CHAR_YES = "y";//$NON-NLS-1$

  /**
   * Символ n
   */
  String CHAR_NO = "n";//$NON-NLS-1$

  /**
   * Пробел
   */
  String CHAR_SPACE = " ";//$NON-NLS-1$

  /**
   * ?
   */
  String CHAR_QUESTION = "?";//$NON-NLS-1$

  /**
   * Файл истории введенных команд
   */
  String CONSOLE_HISTORY_FILE = "skadmin.history";//$NON-NLS-1$

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
  // FIXME l10n
  String MSG_CONSOLE_CMD_INIT    = Messages.getString( "MSG_CONSOLE_CMD_INIT" );                                      //$NON-NLS-1$
  String MSG_CONSOLE_INIT_FINISH = Messages.getString( "MSG_CONSOLE_INIT_FINISH" );                                   //$NON-NLS-1$
  String MSG_LOGO1               =
      " Uskat admin console. Version: %s. " + COLOR_ID + "ToxSoft" + COLOR_RESET + " Ltd. www.toxsoft.ru. 1993-2025.";
  String MSG_LOGO2               = " Use the " + COLOR_ID + "help" + COLOR_RESET + " command for more information.";
  String MSG_RCM_WIDTH           = COLOR_WARN + "Рекомендуемая ширина буфера консоли %d символов" + COLOR_RESET + ".";
  String MSG_PROMPT_START        = "[";                                                                               //$NON-NLS-1$
  String MSG_PROMPT_FINISH       = "]$";                                                                              //$NON-NLS-1$
  String MSG_RETRY_ENTRY         = Messages.getString( "MSG_RETRY_ENTRY" );                                           //$NON-NLS-1$
  String MSG_ANSWER_YES          = Messages.getString( "MSG_ANSWER_YES" );                                            //$NON-NLS-1$
  String MSG_CMD_START_EXECUTE   = "%s%s" + COLOR_RESET;
  String MSG_CMD_FINISH_ID       = "Команда " + COLOR_ID + "%s" + COLOR_RESET + ". ";
  String MSG_CMD_FINISH_RESULT   = "Результат: %s%s" + COLOR_RESET + ". ";
  String MSG_CONSOLE_FINISHED    = Messages.getString( "MSG_CONSOLE_FINISHED" );                                      //$NON-NLS-1$

  /**
   * Идентификатор аргумента "пароль"
   */
  String PASSWORD_ARG_ID = Messages.getString( "PASSWORD_ARG_ID" ); //$NON-NLS-1$

  String MSG_ERR_CTX_MULTY_INPUTS      = Messages.getString( "MSG_ERR_CTX_MULTY_INPUTS" );                           //$NON-NLS-1$
  String MSG_ERR                       = COLOR_ERROR + "%s" + COLOR_RESET;
  String MSG_ERR_CMD_UNEXPECTED        = COLOR_ERROR + "Ошибка выполнения команды '%s'. Причина: '%s'" + COLOR_RESET;
  String MSG_ERR_READ_CONTEXT          =
      COLOR_ERROR + "Ошибка чтения файла контекста параметров %s. Причина: '%s'" + COLOR_RESET;
  String MSG_ERR_CMD_REJECT            = Messages.getString( "MSG_ERR_CMD_REJECT" );                                 //$NON-NLS-1$
  String MSG_ERR_ARG_NOT_FOUND         = Messages.getString( "MSG_ERR_ARG_NOT_FOUND" );                              //$NON-NLS-1$
  String MSG_ERR_CONTEXT_ARG_NOT_FOUND = Messages.getString( "MSG_ERR_CONTEXT_ARG_NOT_FOUND" );                      //$NON-NLS-1$
  String MSG_ERR_CONTEXT_OVER_INPUT    = Messages.getString( "MSG_ERR_CONTEXT_OVER_INPUT" );                         //$NON-NLS-1$
}
