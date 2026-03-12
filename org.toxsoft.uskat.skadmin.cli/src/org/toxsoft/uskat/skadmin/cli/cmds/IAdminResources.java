package org.toxsoft.uskat.skadmin.cli.cmds;

import static org.toxsoft.uskat.skadmin.cli.AdminColors.*;
import static org.toxsoft.uskat.skadmin.cli.IAdminAnsiConstants.*;

/**
 * Константы, локализуемые ресурсы реализации команд консоли.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IAdminResources {

  // FIXME l10n

  /**
   * Обратный слеш
   */
  String CHAR_SLASH = "/";

  /**
   * Двоеточие
   */
  String CHAR_COLON = ":";

  /**
   * Двоеточие
   */
  char CHAR_CHAR_COLON = ':';

  /**
   * Коммерческая 'at'
   */
  char CHAR_AT = '@';

  /**
   * Доллар
   */
  String CHAR_DOLLAR = "$";

  /**
   * Префикс аргумента
   */
  String CHAR_ARG_PREFIX = "-";
  /**
   * Идентификатор корневого раздела
   */
  String ROOT_SECTION    = ".";
  /**
   * Идентификатор родительского раздела
   */
  String PARENT_SECTION  = "..";

  /**
   * Комментарий
   */
  String CHAR_LINE_COMMENT = "#";

  /**
   * Символ продолжения команды на следующую строку
   */
  String CHAR_MULTI_LINE = "\\";

  /**
   * Регулярное выражение для поиска файловых разделителей в формате windows
   */
  char CHAR_WIN_FILE_SEPARATOR = '\\';

  /**
   * Регулярное выражение для поиска формальных аргментов
   */
  String ARG_REGEX = "\\$";

  /**
   * Начало шаблона
   */
  String PATTERN_START  = "%-";
  /**
   * Завершение шаблона
   */
  String PATTERN_FINISH = "s";

  /**
   * Идентификатор цветового переключателя текста
   */
  String FORE_COLOR_ID = "FORE_";

  /**
   * Идентификатор цветового переключателя фона текста
   */
  String BACK_COLOR_ID = "BACK_";

  /**
   * Формат имени аргумента скрипта
   */
  String BATCH_ARG_NAME_FORMAT = "%d";

  // ------------------------------------------------------------------------------------
  // Аргументы общие для команд
  //

  // ------------------------------------------------------------------------------------
  // ConsoleCmdHelp
  //
  String HELP_CMD_ID    = Messages.getString( "HELP_CMD_ID" );    //$NON-NLS-1$
  String HELP_CMD_ALIAS = Messages.getString( "HELP_CMD_ALIAS" ); //$NON-NLS-1$
  String HELP_CMD_NAME  = Messages.getString( "HELP_CMD_NAME" );  //$NON-NLS-1$
  String HELP_CMD_DESCR = Messages.getString( "HELP_CMD_DESCR" ); //$NON-NLS-1$

  String HELP_ARG_CMD_ID    = Messages.getString( "HELP_ARG_CMD_ID" );    //$NON-NLS-1$
  String HELP_ARG_CMD_ALIAS = Messages.getString( "HELP_ARG_CMD_ALIAS" ); //$NON-NLS-1$
  String HELP_ARG_CMD_NAME  = Messages.getString( "HELP_ARG_CMD_NAME" );  //$NON-NLS-1$
  String HELP_ARG_CMD_DESCR = Messages.getString( "HELP_ARG_CMD_DESCR" ); //$NON-NLS-1$

  String HELP_ARG_ALL_ID      = Messages.getString( "HELP_ARG_ALL_ID" );      //$NON-NLS-1$
  String HELP_ARG_ALL_ALIAS   = Messages.getString( "HELP_ARG_ALL_ALIAS" );   //$NON-NLS-1$
  String HELP_ARG_ALL_NAME    = Messages.getString( "HELP_ARG_ALL_NAME" );    //$NON-NLS-1$
  String HELP_ARG_ALL_DESCR   = Messages.getString( "HELP_ARG_ALL_DESCR" );   //$NON-NLS-1$
  String HELP_ARG_ALL_DEFAULT = Messages.getString( "HELP_ARG_ALL_DEFAULT" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // ConsoleCmdClear
  //
  String CLEAR_CMD_ID    = Messages.getString( "CLEAR_CMD_ID" );    //$NON-NLS-1$
  String CLEAR_CMD_ALIAS = Messages.getString( "CLEAR_CMD_ALIAS" ); //$NON-NLS-1$
  String CLEAR_CMD_NAME  = Messages.getString( "CLEAR_CMD_NAME" );  //$NON-NLS-1$
  String CLEAR_CMD_DESCR = Messages.getString( "CLEAR_CMD_DESCR" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // ConsoleCmdCd
  //
  String CD_CMD_ID    = Messages.getString( "CD_CMD_ID" );    //$NON-NLS-1$
  String CD_CMD_ALIAS = Messages.getString( "CD_CMD_ALIAS" ); //$NON-NLS-1$
  String CD_CMD_NAME  = Messages.getString( "CD_CMD_NAME" );  //$NON-NLS-1$
  String CD_CMD_DESCR = Messages.getString( "CD_CMD_DESCR" ); //$NON-NLS-1$

  String CD_ARG_SECTION_ID    = Messages.getString( "CD_ARG_SECTION_ID" );    //$NON-NLS-1$
  String CD_ARG_SECTION_ALIAS = Messages.getString( "CD_ARG_SECTION_ALIAS" ); //$NON-NLS-1$
  String CD_ARG_SECTION_NAME  = Messages.getString( "CD_ARG_SECTION_NAME" );  //$NON-NLS-1$
  String CD_ARG_SECTION_DESCR = Messages.getString( "CD_ARG_SECTION_DESCR" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // ConsoleCmdLs
  //
  String LS_CMD_ID    = Messages.getString( "LS_CMD_ID" );    //$NON-NLS-1$
  String LS_CMD_ALIAS = Messages.getString( "LS_CMD_ALIAS" ); //$NON-NLS-1$
  String LS_CMD_NAME  = Messages.getString( "LS_CMD_NAME" );  //$NON-NLS-1$
  String LS_CMD_DESCR = Messages.getString( "LS_CMD_DESCR" ); //$NON-NLS-1$

  String LS_ARG_SECTION_ID    = Messages.getString( "LS_ARG_SECTION_ID" );    //$NON-NLS-1$
  String LS_ARG_SECTION_ALIAS = Messages.getString( "LS_ARG_SECTION_ALIAS" ); //$NON-NLS-1$
  String LS_ARG_SECTION_NAME  = Messages.getString( "LS_ARG_SECTION_NAME" );  //$NON-NLS-1$
  String LS_ARG_SECTION_DESCR = Messages.getString( "LS_ARG_SECTION_DESCR" ); //$NON-NLS-1$

  String LS_ARG_DESCRIPTION_ID      = Messages.getString( "LS_ARG_DESCRIPTION_ID" );      //$NON-NLS-1$
  String LS_ARG_DESCRIPTION_ALIAS   = Messages.getString( "LS_ARG_DESCRIPTION_ALIAS" );   //$NON-NLS-1$
  String LS_ARG_DESCRIPTION_NAME    = Messages.getString( "LS_ARG_DESCRIPTION_NAME" );    //$NON-NLS-1$
  String LS_ARG_DESCRIPTION_DESCR   = Messages.getString( "LS_ARG_DESCRIPTION_DESCR" );   //$NON-NLS-1$
  String LS_ARG_DESCRIPTION_DEFAULT = Messages.getString( "LS_ARG_DESCRIPTION_DEFAULT" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // ConsoleCmdBatch
  //
  String BATCH_CMD_ID    = Messages.getString( "BATCH_CMD_ID" );    //$NON-NLS-1$
  String BATCH_CMD_ALIAS = Messages.getString( "BATCH_CMD_ALIAS" ); //$NON-NLS-1$
  String BATCH_CMD_NAME  = Messages.getString( "BATCH_CMD_NAME" );  //$NON-NLS-1$

  // FIXME l10n
  String BATCH_CMD_DESCR =
      "Команда -batch построчно читает из представленного текстового файла команды skadmin и запускает их на выполнение. "
          + "Строки начинающиеся символом # игнорируются (строка комментарий).\n\n "
          + "Поиск скрипта по указанному имени выполняется в каталогах (в указанном порядке):\n"
          + "   * Каталог $APPLICATION_HOME/scripts;\n" + "   * Каталог $SKADMIN_HOME/scripts.\n";

  String BATCH_ARG_FILE_ID    = Messages.getString( "BATCH_ARG_FILE_ID" );    //$NON-NLS-1$
  String BATCH_ARG_FILE_ALIAS = Messages.getString( "BATCH_ARG_FILE_ALIAS" ); //$NON-NLS-1$
  String BATCH_ARG_FILE_NAME  = Messages.getString( "BATCH_ARG_FILE_NAME" );  //$NON-NLS-1$
  String BATCH_ARG_FILE_DESCR = Messages.getString( "BATCH_ARG_FILE_DESCR" ); //$NON-NLS-1$

  String BATCH_ARG_ARGS_ID    = Messages.getString( "BATCH_ARG_ARGS_ID" );    //$NON-NLS-1$
  String BATCH_ARG_ARGS_ALIAS = Messages.getString( "BATCH_ARG_ARGS_ALIAS" ); //$NON-NLS-1$
  String BATCH_ARG_ARGS_NAME  = Messages.getString( "BATCH_ARG_ARGS_NAME" );  //$NON-NLS-1$
  String BATCH_ARG_ARGS_DESCR = Messages.getString( "BATCH_ARG_ARGS_DESCR" ); //$NON-NLS-1$

  String BATCH_ARG_CHARSET_ID      = Messages.getString( "BATCH_ARG_CHARSET_ID" );      //$NON-NLS-1$
  String BATCH_ARG_CHARSET_ALIAS   = Messages.getString( "BATCH_ARG_CHARSET_ALIAS" );   //$NON-NLS-1$
  String BATCH_ARG_CHARSET_NAME    = Messages.getString( "BATCH_ARG_CHARSET_NAME" );    //$NON-NLS-1$
  String BATCH_ARG_CHARSET_DESCR   = Messages.getString( "BATCH_ARG_CHARSET_DESCR" );   //$NON-NLS-1$
  String BATCH_ARG_CHARSET_DEFAULT = Messages.getString( "BATCH_ARG_CHARSET_DEFAULT" ); //$NON-NLS-1$

  String BATCH_ARG_EXIT_ID      = Messages.getString( "BATCH_ARG_EXIT_ID" );      //$NON-NLS-1$
  String BATCH_ARG_EXIT_ALIAS   = Messages.getString( "BATCH_ARG_EXIT_ALIAS" );   //$NON-NLS-1$
  String BATCH_ARG_EXIT_NAME    = Messages.getString( "BATCH_ARG_EXIT_NAME" );    //$NON-NLS-1$
  String BATCH_ARG_EXIT_DESCR   = Messages.getString( "BATCH_ARG_EXIT_DESCR" );   //$NON-NLS-1$
  String BATCH_ARG_EXIT_DEFAULT = Messages.getString( "BATCH_ARG_EXIT_DEFAULT" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // ConsoleCmdHasParam
  //
  String HAS_CMD_ID           = Messages.getString( "HAS_CMD_ID" );           //$NON-NLS-1$
  String HAS_CMD_ALIAS        = Messages.getString( "HAS_CMD_ALIAS" );        //$NON-NLS-1$
  String HAS_CMD_NAME         = Messages.getString( "HAS_CMD_NAME" );         //$NON-NLS-1$
  String HAS_CMD_DESCR        = Messages.getString( "HAS_CMD_DESCR" );        //$NON-NLS-1$
  String HAS_CMD_RESULT_DESCR = Messages.getString( "HAS_CMD_RESULT_DESCR" ); //$NON-NLS-1$

  String HAS_ARG_NAME_ID    = Messages.getString( "HAS_ARG_NAME_ID" );    //$NON-NLS-1$
  String HAS_ARG_NAME_ALIAS = Messages.getString( "HAS_ARG_NAME_ALIAS" ); //$NON-NLS-1$
  String HAS_ARG_NAME_NAME  = Messages.getString( "HAS_ARG_NAME_NAME" );  //$NON-NLS-1$
  String HAS_ARG_NAME_DESCR = Messages.getString( "HAS_ARG_NAME_DESCR" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // ConsoleCmdSignal
  //
  String SIGNAL_CMD_ID       = Messages.getString( "SIGNAL_CMD_ID" );       //$NON-NLS-1$
  String SIGNAL_CMD_ALIAS    = Messages.getString( "SIGNAL_CMD_ALIAS" );    //$NON-NLS-1$
  String SIGNAL_CMD_NAME     = Messages.getString( "SIGNAL_CMD_NAME" );     //$NON-NLS-1$
  String SIGNAL_CMD_DESCR    = Messages.getString( "SIGNAL_CMD_DESCR" );    //$NON-NLS-1$
  String SIGNAL_RESULT_DESCR = Messages.getString( "SIGNAL_RESULT_DESCR" ); //$NON-NLS-1$

  String ARG_SIGNAL_ID_ID    = Messages.getString( "ARG_SIGNAL_ID_ID" );    //$NON-NLS-1$
  String ARG_SIGNAL_ID_ALIAS = Messages.getString( "ARG_SIGNAL_ID_ALIAS" ); //$NON-NLS-1$
  String ARG_SIGNAL_ID_NAME  = Messages.getString( "ARG_SIGNAL_ID_NAME" );  //$NON-NLS-1$
  String ARG_SIGNAL_ID_DESCR = Messages.getString( "ARG_SIGNAL_ID_DESCR" ); //$NON-NLS-1$

  String ARG_SIGNAL_CMD_ID    = Messages.getString( "ARG_SIGNAL_CMD_ID" );    //$NON-NLS-1$
  String ARG_SIGNAL_CMD_ALIAS = Messages.getString( "ARG_SIGNAL_CMD_ALIAS" ); //$NON-NLS-1$
  String ARG_SIGNAL_CMD_NAME  = Messages.getString( "ARG_SIGNAL_CMD_NAME" );  //$NON-NLS-1$
  String ARG_SIGNAL_CMD_DESCR = Messages.getString( "ARG_SIGNAL_CMD_DESCR" ); //$NON-NLS-1$

  String ARG_SIGNAL_VALUE_ID      = Messages.getString( "ARG_SIGNAL_VALUE_ID" );      //$NON-NLS-1$
  String ARG_SIGNAL_VALUE_ALIAS   = Messages.getString( "ARG_SIGNAL_VALUE_ALIAS" );   //$NON-NLS-1$
  String ARG_SIGNAL_VALUE_NAME    = Messages.getString( "ARG_SIGNAL_VALUE_NAME" );    //$NON-NLS-1$
  String ARG_SIGNAL_VALUE_DESCR   = Messages.getString( "ARG_SIGNAL_VALUE_DESCR" );   //$NON-NLS-1$
  String ARG_SIGNAL_VALUE_DEFAULT = Messages.getString( "ARG_SIGNAL_VALUE_DEFAULT" ); //$NON-NLS-1$

  String ARG_SIGNAL_TIMEOUT_ID      = Messages.getString( "ARG_SIGNAL_TIMEOUT_ID" );      //$NON-NLS-1$
  String ARG_SIGNAL_TIMEOUT_ALIAS   = Messages.getString( "ARG_SIGNAL_TIMEOUT_ALIAS" );   //$NON-NLS-1$
  String ARG_SIGNAL_TIMEOUT_NAME    = Messages.getString( "ARG_SIGNAL_TIMEOUT_NAME" );    //$NON-NLS-1$
  String ARG_SIGNAL_TIMEOUT_DESCR   = Messages.getString( "ARG_SIGNAL_TIMEOUT_DESCR" );   //$NON-NLS-1$
  String ARG_SIGNAL_TIMEOUT_DEFAULT = Messages.getString( "ARG_SIGNAL_TIMEOUT_DEFAULT" ); //$NON-NLS-1$

  String E_SIGNAL_CREATE = Messages.getString( "E_SIGNAL_CREATE" ); //$NON-NLS-1$
  String E_SIGNAL_DELETE = Messages.getString( "E_SIGNAL_DELETE" ); //$NON-NLS-1$
  String E_SIGNAL_WAIT   = Messages.getString( "E_SIGNAL_WAIT" );   //$NON-NLS-1$

  String E_SIGNAL_CREATE_D = Messages.getString( "E_SIGNAL_CREATE_D" ); //$NON-NLS-1$
  String E_SIGNAL_DELETE_D = Messages.getString( "E_SIGNAL_DELETE_D" ); //$NON-NLS-1$
  String E_SIGNAL_WAIT_D   = Messages.getString( "E_SIGNAL_WAIT_D" );   //$NON-NLS-1$

  String E_OPERATOR_NOOP   = Messages.getString( "E_OPERATOR_NOOP" );   //$NON-NLS-1$
  String E_OPERATOR_NOOP_D = Messages.getString( "E_OPERATOR_NOOP_D" ); //$NON-NLS-1$

  String E_OPERATOR_IF   = Messages.getString( "E_OPERATOR_IF" );   //$NON-NLS-1$
  String E_OPERATOR_IF_D = Messages.getString( "E_OPERATOR_IF_D" ); //$NON-NLS-1$

  String E_OPERATOR_WHILE   = Messages.getString( "E_OPERATOR_WHILE" );   //$NON-NLS-1$
  String E_OPERATOR_WHILE_D = Messages.getString( "E_OPERATOR_WHILE_D" ); //$NON-NLS-1$

  String E_OPERATOR_END   = Messages.getString( "E_OPERATOR_END" );   //$NON-NLS-1$
  String E_OPERATOR_END_D = Messages.getString( "E_OPERATOR_END_D" ); //$NON-NLS-1$

  String E_OPERATOR_RETURN   = Messages.getString( "E_OPERATOR_RETURN" );   //$NON-NLS-1$
  String E_OPERATOR_RETURN_D = Messages.getString( "E_OPERATOR_RETURN_D" ); //$NON-NLS-1$

  String E_OPERATOR_INCLUDE   = Messages.getString( "E_OPERATOR_INCLUDE" );   //$NON-NLS-1$
  String E_OPERATOR_INCLUDE_D = Messages.getString( "E_OPERATOR_INCLUDE_D" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // ConsoleCmdTimeout
  //
  String TIMEOUT_CMD_ID    = Messages.getString( "TIMEOUT_CMD_ID" );    //$NON-NLS-1$
  String TIMEOUT_CMD_ALIAS = Messages.getString( "TIMEOUT_CMD_ALIAS" ); //$NON-NLS-1$
  String TIMEOUT_CMD_NAME  = Messages.getString( "TIMEOUT_CMD_NAME" );  //$NON-NLS-1$
  String TIMEOUT_CMD_DESCR = Messages.getString( "TIMEOUT_CMD_DESCR" ); //$NON-NLS-1$

  String ARG_TIMEOUT_VALUE_ID      = Messages.getString( "ARG_TIMEOUT_VALUE_ID" );      //$NON-NLS-1$
  String ARG_TIMEOUT_VALUE_ALIAS   = Messages.getString( "ARG_TIMEOUT_VALUE_ALIAS" );   //$NON-NLS-1$
  String ARG_TIMEOUT_VALUE_NAME    = Messages.getString( "ARG_TIMEOUT_VALUE_NAME" );    //$NON-NLS-1$
  String ARG_TIMEOUT_VALUE_DESCR   = Messages.getString( "ARG_TIMEOUT_VALUE_DESCR" );   //$NON-NLS-1$
  String ARG_TIMEOUT_VALUE_DEFAULT = Messages.getString( "ARG_TIMEOUT_VALUE_DEFAULT" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // ConsoleCmdTimeToString
  //
  String TIME_TO_STRING_CMD_ID    = Messages.getString( "TIME_TO_STRING_CMD_ID" );    //$NON-NLS-1$
  String TIME_TO_STRING_CMD_ALIAS = Messages.getString( "TIME_TO_STRING_CMD_ALIAS" ); //$NON-NLS-1$
  String TIME_TO_STRING_CMD_NAME  = Messages.getString( "TIME_TO_STRING_CMD_NAME" );  //$NON-NLS-1$
  String TIME_TO_STRING_CMD_DESCR = Messages.getString( "TIME_TO_STRING_CMD_DESCR" ); //$NON-NLS-1$

  String ARG_TIME_ID    = Messages.getString( "ARG_TIME_ID" );    //$NON-NLS-1$
  String ARG_TIME_ALIAS = Messages.getString( "ARG_TIME_ALIAS" ); //$NON-NLS-1$
  String ARG_TIME_NAME  = Messages.getString( "ARG_TIME_NAME" );  //$NON-NLS-1$
  String ARG_TIME_DESCR = Messages.getString( "ARG_TIME_DESCR" ); //$NON-NLS-1$

  String TIME_TO_STRING_RESULT_DESCR = Messages.getString( "TIME_TO_STRING_RESULT_DESCR" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // ConsoleCmdEcho
  //
  String ECHO_CMD_ID    = Messages.getString( "ECHO_CMD_ID" );    //$NON-NLS-1$
  String ECHO_CMD_ALIAS = Messages.getString( "ECHO_CMD_ALIAS" ); //$NON-NLS-1$
  String ECHO_CMD_NAME  = Messages.getString( "ECHO_CMD_NAME" );  //$NON-NLS-1$

  // FIXME l10n
  String ECHO_CMD_DESCR    =
      "Вывод текста на экран. При выводе можно управлять цветом и фоном выводимого текста. Для того, чтобы изменить цвет/фон дальше выводимого "
          + "текста необходимо определить :" + COLOR_ID + FORE_COLOR_ID + COLOR_RESET + "" + COLOR_SINGLE_VALUE
          + "COLOR_NAME" + COLOR_RESET + " или для фона :" + COLOR_ID + BACK_COLOR_ID + COLOR_RESET + ""
          + COLOR_SINGLE_VALUE + "COLOR_NAME" + COLOR_RESET + ", где " + COLOR_SINGLE_VALUE + "COLOR_NAME "
          + COLOR_RESET + "должен быть одной из " + "следующих строковых констант: " + CLR_FORE_BLACK + "BLACK, "
          + COLOR_RESET

          + CLR_FORE_RED + "RED, " + COLOR_RESET

          + CLR_FORE_GREEN + "GREEN, " + COLOR_RESET

          + CLR_FORE_YELLOW + "YELLOW, " + COLOR_RESET

          + CLR_FORE_BLUE + "BLUE, " + COLOR_RESET

          + CLR_FORE_MAGENTA + "MAGENTA, " + COLOR_RESET

          + CLR_FORE_CYAN + "CYAN, " + COLOR_RESET

          + CLR_FORE_WHITE + "WHITE, " + COLOR_RESET

          + CLR_FORE_BRIGHT_BLACK + "BRIGHT_BLACK, " + COLOR_RESET

          + CLR_FORE_BRIGHT_RED + "BRIGHT_RED, " + COLOR_RESET

          + CLR_FORE_BRIGHT_GREEN + "BRIGHT_GREEN, " + COLOR_RESET

          + CLR_FORE_BRIGHT_YELLOW + "BRIGHT_YELLOW, " + COLOR_RESET

          + CLR_FORE_BRIGHT_BLUE + "BRIGHT_BLUE, " + COLOR_RESET

          + CLR_FORE_BRIGHT_MAGENTA + "BRIGHT_MAGENTA, " + COLOR_RESET

          + CLR_FORE_BRIGHT_CYAN + "BRIGHT_CYAN, " + COLOR_RESET

          + CLR_FORE_BRIGHT_WHITE + "BRIGHT_WHITE, " + COLOR_RESET

          + "DEFAULT.";
  String ECHO_RESULT_DESCR = Messages.getString( "ECHO_RESULT_DESCR" );                                                                         //$NON-NLS-1$

  String ECHO_ARG_TEXT_ID    = Messages.getString( "ECHO_ARG_TEXT_ID" );    //$NON-NLS-1$
  String ECHO_ARG_TEXT_ALIAS = Messages.getString( "ECHO_ARG_TEXT_ALIAS" ); //$NON-NLS-1$
  String ECHO_ARG_TEXT_NAME  = Messages.getString( "ECHO_ARG_TEXT_NAME" );  //$NON-NLS-1$
  String ECHO_ARG_TEXT_DESCR = Messages.getString( "ECHO_ARG_TEXT_DESCR" ); //$NON-NLS-1$

  String ECHO_ARG_EOL_ID      = Messages.getString( "ECHO_ARG_EOL_ID" );      //$NON-NLS-1$
  String ECHO_ARG_EOL_ALIAS   = Messages.getString( "ECHO_ARG_EOL_ALIAS" );   //$NON-NLS-1$
  String ECHO_ARG_EOL_NAME    = Messages.getString( "ECHO_ARG_EOL_NAME" );    //$NON-NLS-1$
  String ECHO_ARG_EOL_DESCR   = Messages.getString( "ECHO_ARG_EOL_DESCR" );   //$NON-NLS-1$
  String ECHO_ARG_EOL_DEFAULT = Messages.getString( "ECHO_ARG_EOL_DEFAULT" ); //$NON-NLS-1$

  String ECHO_ARG_SILENT_ID      = Messages.getString( "ECHO_ARG_SILENT_ID" );      //$NON-NLS-1$
  String ECHO_ARG_SILENT_ALIAS   = Messages.getString( "ECHO_ARG_SILENT_ALIAS" );   //$NON-NLS-1$
  String ECHO_ARG_SILENT_NAME    = Messages.getString( "ECHO_ARG_SILENT_NAME" );    //$NON-NLS-1$
  String ECHO_ARG_SILENT_DESCR   = Messages.getString( "ECHO_ARG_SILENT_DESCR" );   //$NON-NLS-1$
  String ECHO_ARG_SILENT_DEFAULT = Messages.getString( "ECHO_ARG_SILENT_DEFAULT" ); //$NON-NLS-1$

  String ECHO_ARG_SPACE_TRAIL_ID      = Messages.getString( "ECHO_ARG_SPACE_TRAIL_ID" );      //$NON-NLS-1$
  String ECHO_ARG_SPACE_TRAIL_ALIAS   = Messages.getString( "ECHO_ARG_SPACE_TRAIL_ALIAS" );   //$NON-NLS-1$
  String ECHO_ARG_SPACE_TRAIL_NAME    = Messages.getString( "ECHO_ARG_SPACE_TRAIL_NAME" );    //$NON-NLS-1$
  String ECHO_ARG_SPACE_TRAIL_DESCR   = Messages.getString( "ECHO_ARG_SPACE_TRAIL_DESCR" );   //$NON-NLS-1$
  String ECHO_ARG_SPACE_TRAIL_DEFAULT = Messages.getString( "ECHO_ARG_SPACE_TRAIL_DEFAULT" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // ConsoleCmdEquals
  //
  String IS_EQUAL_CMD_ID           = Messages.getString( "IS_EQUAL_CMD_ID" );           //$NON-NLS-1$
  String IS_EQUAL_CMD_ALIAS        = Messages.getString( "IS_EQUAL_CMD_ALIAS" );        //$NON-NLS-1$
  String IS_EQUAL_CMD_NAME         = Messages.getString( "IS_EQUAL_CMD_NAME" );         //$NON-NLS-1$
  String IS_EQUAL_CMD_DESCR        = Messages.getString( "IS_EQUAL_CMD_DESCR" );        //$NON-NLS-1$
  String IS_EQUAL_CMD_RESULT_DESCR = Messages.getString( "IS_EQUAL_CMD_RESULT_DESCR" ); //$NON-NLS-1$

  String IS_EQUAL_ARG_PAR1_ID    = Messages.getString( "IS_EQUAL_ARG_PAR1_ID" );    //$NON-NLS-1$
  String IS_EQUAL_ARG_PAR1_ALIAS = Messages.getString( "IS_EQUAL_ARG_PAR1_ALIAS" ); //$NON-NLS-1$
  String IS_EQUAL_ARG_PAR1_NAME  = Messages.getString( "IS_EQUAL_ARG_PAR1_NAME" );  //$NON-NLS-1$
  String IS_EQUAL_ARG_PAR1_DESCR = Messages.getString( "IS_EQUAL_ARG_PAR1_DESCR" ); //$NON-NLS-1$

  String IS_EQUAL_ARG_PAR2_ID    = Messages.getString( "IS_EQUAL_ARG_PAR2_ID" );    //$NON-NLS-1$
  String IS_EQUAL_ARG_PAR2_ALIAS = Messages.getString( "IS_EQUAL_ARG_PAR2_ALIAS" ); //$NON-NLS-1$
  String IS_EQUAL_ARG_PAR2_NAME  = Messages.getString( "IS_EQUAL_ARG_PAR2_NAME" );  //$NON-NLS-1$
  String IS_EQUAL_ARG_PAR2_DESCR = Messages.getString( "IS_EQUAL_ARG_PAR2_DESCR" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // ConsoleCmdExit
  //
  String EXIT_CMD_ID    = Messages.getString( "EXIT_CMD_ID" );    //$NON-NLS-1$
  String EXIT_CMD_ALIAS = Messages.getString( "EXIT_CMD_ALIAS" ); //$NON-NLS-1$
  String EXIT_CMD_NAME  = Messages.getString( "EXIT_CMD_NAME" );  //$NON-NLS-1$
  String EXIT_CMD_DESCR = Messages.getString( "EXIT_CMD_DESCR" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // ConsoleCmdQuit
  //
  String QUIT_CMD_ID    = Messages.getString( "QUIT_CMD_ID" );    //$NON-NLS-1$
  String QUIT_CMD_ALIAS = Messages.getString( "QUIT_CMD_ALIAS" ); //$NON-NLS-1$
  String QUIT_CMD_NAME  = Messages.getString( "QUIT_CMD_NAME" );  //$NON-NLS-1$
  String QUIT_CMD_DESCR = Messages.getString( "QUIT_CMD_DESCR" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Сообщения
  //
  // FIXME l10n
  String MSG_CHILD_SECTION        = Messages.getString( "MSG_CHILD_SECTION" );                                         //$NON-NLS-1$
  String MSG_CMD_INFO             = "Раздел   : " + COLOR_ID + "%s\n" + COLOR_RESET + "Имя      : " + COLOR_ID + "%s\n"
      + COLOR_RESET + "Роли     : %s\n" + "Кратко   : %s";
  String MSG_CMD_ALIAS_INFO       = "Раздел   : " + COLOR_ID + "%s\n" + COLOR_RESET + "Имя,алиас: " + COLOR_ID + "%s\n"
      + COLOR_RESET + "Роли     : %s\n" + "Кратко   : %s";
  String MSG_CMD_DESCR            = Messages.getString( "MSG_CMD_DESCR" );                                             //$NON-NLS-1$
  String MSG_ROLES_ALL            = Messages.getString( "MSG_ROLES_ALL" );                                             //$NON-NLS-1$
  String MSG_CMD_CONTEXT          = Messages.getString( "MSG_CMD_CONTEXT" );                                           //$NON-NLS-1$
  String MSG_CMD_CONTEXT_NOT_USED = Messages.getString( "MSG_CMD_CONTEXT_NOT_USED" );                                  //$NON-NLS-1$
  String MSG_CMD_CONTEXT_INFO     = "%s  $%-14s %-20s" + COLOR_RESET;
  String MSG_CMD_CONTEXT_DESCR    = Messages.getString( "MSG_CMD_CONTEXT_DESCR" );                                     //$NON-NLS-1$
  String MSG_CMD_ARGS             = Messages.getString( "MSG_CMD_ARGS" );                                              //$NON-NLS-1$
  String MSG_CMD_ARG_INFO         = "%s  %-15s%s %-20s" + COLOR_RESET;
  String MSG_CMD_ARG_DESCR        = Messages.getString( "MSG_CMD_ARG_DESCR" );                                         //$NON-NLS-1$
  String MSG_CMD_ARG_DEFAULT      = " Значение по умолчанию: " + COLOR_SINGLE_VALUE + "%s" + COLOR_RESET + ".";
  String MSG_CMD_RESULT           = "Результат: %s       %-20s " + COLOR_RESET;
  String MSG_NO_RESULT            = Messages.getString( "MSG_NO_RESULT" );                                             //$NON-NLS-1$
  // @formatter:off
  String MSG_HELP =
      "Общий формат команды определяется как: "
          + COLOR_ID
          + "имя_команды "
          + COLOR_RESET
          + COLOR_SINGLE_VALUE
          + "значения_аргументов"
          + COLOR_RESET
          + ", где "
          + COLOR_ID
          + "имя_команды"
          + COLOR_RESET
          + " - это ИД-путь в котором через точку перечисляются имена родительских разделов и который заканчивается"
          + " ИД-именем команды, например: "
          + COLOR_ID
          + "level1_section_name.level2_section_name.cmd_name"
          + COLOR_RESET
          + ".\n\nИД-путь команды может быть неполным. В этом случае, система пытается автоматически составить полный"
          + " ИД-путь команды используя ИД-путь текущего раздела и ИД-путь введенный пользователем, например: "
          + "[/"
          + COLOR_ID
          + "level1_section_name"
          + COLOR_RESET
          + "/"
          + COLOR_ID
          + "level2_section_name"
          + COLOR_RESET
          + "]$"
          + COLOR_ID
          + "level3_section_name.cmd_name"
          + COLOR_RESET
          + " будет представлять команду с полным ИД-путем: "
          + COLOR_ID
          + "level1_section_name.level2_section_name.level3_section_name.cmd_name"
          + COLOR_RESET
          + ".\nЕсли система не найдет команды с полученным ИД-путем, то она попытается найти команду "
          + "используя только ИД-путь введенный пользователем. Следствием этого, является то, что команды корневого "
          + "раздела, доступны по их ИД-имени в любом разделе если они не перегружаются командами с теми же ИД-именами в дочерних разделах.\n\n"
          //
          //
          + "Аргументы команды могут иметь значения по умолчанию. Ввод значений таких аргументов не является обязательным. Значение "
          + "по умолчанию определяется при описании аргумента. Значения-списки и значения именованных наборов всегда имеют значение по умолчанию: пустой список.\n\n"
          + "Значения аргументов могут быть определены в канонической или упрощенной форме. В канонической форме "
          + "значения аргументов указываются через ИД-имя аргумента(с префиксом '-') за которым следует его значение. "
          + "Исключением из этого правила является использование аргумента-флага. Тип значений флага может "
          + "быть только логическим. Когда система определяет, что за ИД-именем аргумента логического типа нет значения, "
          + "то она считает аргумент флагом. Фактическое значение флага всегда определяется как " + COLOR_SINGLE_VALUE + "true" + COLOR_RESET+ ". "
          + ". При этом, возможна ситуация, когда текущий набор значений других аргументов запрещает использование флага. "
          + "Об этом выводится соответствующее сообщение. Порядок перечисления аргументов в канонической форме может быть любым. Например: "
          + COLOR_ID
          + "cmdName -argFlagName1 -argName2"
          + COLOR_RESET
          + COLOR_SINGLE_VALUE
          + " value2"
          + COLOR_RESET
          + COLOR_ID
          + " -argName3"
          + COLOR_RESET
          + COLOR_VALUE_LIST
          + " value3_item0"
          + COLOR_RESET
          + ","
          + COLOR_VALUE_LIST
          + " value3_item1"
          + COLOR_RESET
          + ".\n\n"
          + "В упрощенной форме имена аргументов не указываются. Значения аргументов перечисляются в том же "
          + "порядке в котором они определены в описании команды. Аргументы с значениями по умолчанию могут быть "
          + "не указаны только в том случае, если за ними, в описании команды, нет аргументов со значениями обязательного "
          + "ввода. Выше приведенная команда в упрощенной форме будет выглядеть следующим образом: "
          + COLOR_ID
          + "cmdName "
          + COLOR_RESET
          + COLOR_SINGLE_VALUE
          + " true value2"
          + COLOR_RESET
          + COLOR_VALUE_LIST
          + " value3_item0"
          + COLOR_RESET
          + ","
          + COLOR_VALUE_LIST
          + " value3_item1"
          + COLOR_RESET
          + ".\n\n"
          + "Формат значений аргументов допускает ввод логических, числовых, меток времени, строковых данных, а также их списков и именованных наборов. Далее приводится информация по вводу значений некоторых типов данных. "
          + "\n\n"
          + COLOR_SINGLE_VALUE + "Timestamp" + COLOR_RESET + ": Метки времени вводятся в формате: " + COLOR_SINGLE_VALUE
          + "YYYY-MM-DD_HH:MM:SS.mmm" + COLOR_RESET + " или в одном из его сокращенных форм: "
          + COLOR_SINGLE_VALUE
          + "YYYY"
          + COLOR_RESET
          + ", "
          + COLOR_SINGLE_VALUE
          + "YYYY-MM"
          + COLOR_RESET
          + ", "
          + COLOR_SINGLE_VALUE
          + "YYYY-MM-DD"
          + COLOR_RESET
          + ", "
          + COLOR_SINGLE_VALUE
          + "YYYY-MM-DD_HH"
          + COLOR_RESET
          + ", "
          + COLOR_SINGLE_VALUE
          + "YYYY-MM-DD_HH:MM"
          + COLOR_RESET
          + ", "
          + COLOR_SINGLE_VALUE
          + "YYYY-MM-DD_HH:MM:SS"
          + COLOR_RESET
          + ".\n\n"
          + COLOR_SINGLE_VALUE + "Valobj" + COLOR_RESET + ": Значения вводятся в формате: " + COLOR_SINGLE_VALUE + "@KeeperId[keeperFormatString]" + COLOR_RESET
          + ", где " + COLOR_SINGLE_VALUE + "KeeperId" + COLOR_RESET + " - идентификатор KEEPER, " + COLOR_SINGLE_VALUE + "keeperFormatString" + COLOR_RESET + " - строка представляющее значение в формате этого KEEPER. "
          + "Например: " + COLOR_SINGLE_VALUE + "@Gwid[sk.User[root]]" + COLOR_RESET
          + ". Особый случай представления значения " + COLOR_SINGLE_VALUE + "IAtomicValue.NULL" + COLOR_RESET + " которое вводится как: "
          + COLOR_SINGLE_VALUE + "@{}" + COLOR_RESET
          + ".\n\n"
          + COLOR_SINGLE_VALUE + "None" + COLOR_RESET + ": тип имеет только ОДНО значение IAtomicValue.NULL которое вводится как: "
          + COLOR_SINGLE_VALUE + COLOR_SINGLE_VALUE + "None"+ COLOR_RESET  //
          + ".\n\n"
          + COLOR_SINGLE_VALUE + "List" + COLOR_RESET + ": Список значений параметров вводятся в формате: "
          + COLOR_SINGLE_VALUE + COLOR_SINGLE_VALUE + "Значение_Параметра1"+ COLOR_RESET + ", "
          + COLOR_SINGLE_VALUE + "Значение_Параметра2"+ COLOR_RESET + ", ..."
          + COLOR_SINGLE_VALUE + "Значение_ПараметраN"+ COLOR_RESET
          + ".\n\n"
          + COLOR_SINGLE_VALUE + "OptionSet" + COLOR_RESET + ": Именованные параметры вводятся в формате: "
          + COLOR_SINGLE_VALUE + "Имя_Параметра1"+ COLOR_RESET + "=" + COLOR_SINGLE_VALUE + "Значение_Параметра1"+ COLOR_RESET + ", "
          + COLOR_SINGLE_VALUE + "Имя_Параметра2"+ COLOR_RESET + "=" + COLOR_SINGLE_VALUE + "Значение_Параметра2"+ COLOR_RESET + ", ..."
          + COLOR_SINGLE_VALUE + "Имя_ПараметраN"+ COLOR_RESET + "=" + COLOR_SINGLE_VALUE + "Значение_ПараметраN"+ COLOR_RESET
          + ".\n\nЕсли в текстовом значении используются пробелы или спец.символы, то значение должно быть обрамлено кавычками: " + COLOR_SINGLE_VALUE + "\"строка\"" + COLOR_RESET
          + ". Если внутри строки находятся неэкранированные кавычки, то необходимо использовать super-кавычки: " + COLOR_SINGLE_VALUE + "'''строка'''" + COLOR_RESET
          + ".\n\nПри написании скриптов с командами для консоли рекомендуется "
          + "всегда обрамлять кавычками строковые данные и использовать каноническую форму определения значений аргументов."
          + "\n\n"
          + "ПРИМЕРЫ."
          + "\n1. Отправка команды:\n"
          + COLOR_ID + "sk.dev.commands.send -classId "
          + COLOR_SINGLE_VALUE + "sk.Alarm " + COLOR_RESET
          + COLOR_ID + "-strid " + COLOR_RESET
          + COLOR_SINGLE_VALUE + "almCalibr_P1 " + COLOR_RESET
          + COLOR_ID + "-cmdId " + COLOR_RESET
          + COLOR_SINGLE_VALUE + "cmdAcknowledge "  + COLOR_RESET
          + COLOR_ID + "-args " + COLOR_RESET
          + COLOR_VALUE_LIST + "ackAuthorGwid=@Gwid[sk.User[root]]"+ COLOR_RESET +", " + COLOR_VALUE_LIST + "ackComment=\"it's ok now\"" + COLOR_RESET
          + "\n\n" //
          + "2. Прием ВСЕХ текущих данных ВСЕХ объектов класса " + COLOR_SINGLE_VALUE + "AnalogInput" + COLOR_RESET + ":\n"
          + COLOR_ID + "sk.dev.rtdata.read -classId "
          + COLOR_SINGLE_VALUE + "AnalogInput " + COLOR_RESET
          + COLOR_ID + "-strid " + COLOR_RESET
          + COLOR_SINGLE_VALUE + "* " + COLOR_RESET
          + COLOR_ID + "-dataId " + COLOR_RESET
          + COLOR_SINGLE_VALUE + "*" + COLOR_RESET
          + "\n\n" //
          + "3. Установка значения " + COLOR_SINGLE_VALUE + "IAtomicValue.NULL" + COLOR_RESET + "(тип EAtomicType.VALOBJ) для контекстной переменной " + COLOR_SINGLE_REF + "$a" + COLOR_RESET
          + ":\n"
          + COLOR_SINGLE_REF + "$a" + COLOR_RESET + "=" + COLOR_SINGLE_VALUE + "@{}" + COLOR_RESET //
          + "\n\n" //
          + "4. Установка значения " + COLOR_SINGLE_VALUE + "IAtomicValue.NULL" + COLOR_RESET + "(тип EAtomicType.NONE) для контекстной переменной " + COLOR_SINGLE_REF + "$a" + COLOR_RESET + ":\n"
          + COLOR_SINGLE_REF + "$a" + COLOR_RESET + "=" + COLOR_SINGLE_VALUE + "None" + COLOR_RESET
          + "\n\n"
          + "Справка по команде (упрощенная форма): " + COLOR_ID + "help" + COLOR_RESET + COLOR_SINGLE_VALUE
          + " имя_команды"
          + COLOR_RESET //
          + "\n\n" //
          + "Справка по всем командам (каноническая форма с использованием флага): " + COLOR_ID + "help -a"
          + COLOR_RESET //
          + "\n\n" //
          + "Список доступных команд текущего раздела (каноническая форма с использованием флага): " + COLOR_ID
          + "list -d" + COLOR_RESET + "\n";
  // @formatter:on

  // ------------------------------------------------------------------------------------
  // Ошибки
  //
  String ERR_MSG_CMD_REJECT            = Messages.getString( "ERR_MSG_CMD_REJECT" );            //$NON-NLS-1$
  String ERR_MSG_CMD_NOT_FOUND         = Messages.getString( "ERR_MSG_CMD_NOT_FOUND" );         //$NON-NLS-1$
  String ERR_MSG_SECTION_NOT_FOUND     = Messages.getString( "ERR_MSG_SECTION_NOT_FOUND" );     //$NON-NLS-1$
  String ERR_MSG_FILE_NOT_FOUND        = Messages.getString( "ERR_MSG_FILE_NOT_FOUND" );        //$NON-NLS-1$
  String ERR_MSG_SIGNAL_DELETE_TIMEOUT = Messages.getString( "ERR_MSG_SIGNAL_DELETE_TIMEOUT" ); //$NON-NLS-1$
  String ERR_MSG_SIGNAL_WAIT_TIMEOUT   = Messages.getString( "ERR_MSG_SIGNAL_WAIT_TIMEOUT" );   //$NON-NLS-1$

  String ERR_MSG_SCRIPT              = Messages.getString( "ERR_MSG_SCRIPT" );              //$NON-NLS-1$
  String ERR_MSG_UNEXPECTED_OPERATOR = Messages.getString( "ERR_MSG_UNEXPECTED_OPERATOR" ); //$NON-NLS-1$
  String ERR_MSG_WRONG_IF_FORMAT     = Messages.getString( "ERR_MSG_WRONG_IF_FORMAT" );     //$NON-NLS-1$
  String ERR_MSG_EXPECTED_LOGICAL    = Messages.getString( "ERR_MSG_EXPECTED_LOGICAL" );    //$NON-NLS-1$
  String ERR_MSG_COMPARE_IMPOSSIBLE  = Messages.getString( "ERR_MSG_COMPARE_IMPOSSIBLE" );  //$NON-NLS-1$
  String ERR_MSG_UNKNOW_COMPARE      = Messages.getString( "ERR_MSG_UNKNOW_COMPARE" );      //$NON-NLS-1$
  String MSG_ERR_CMD_UNEXPECTED      = Messages.getString( "MSG_ERR_CMD_UNEXPECTED" );      //$NON-NLS-1$
  String ERR_MSG_UNCLOSED_OPERATOR   = Messages.getString( "ERR_MSG_UNCLOSED_OPERATOR" );   //$NON-NLS-1$
  // FIXME l10n
  String ERR_MSG_INCLUDE_NOT_FOUND  = COLOR_ERROR + "Невозможно включить файл %s. Файл не найден\n" + COLOR_RESET;
  String ERR_MSG_UNCLOSED_MULTILINE = Messages.getString( "ERR_MSG_UNCLOSED_MULTILINE" );                         //$NON-NLS-1$

  String ERR_MSG_OPEN_FILE_READER             = Messages.getString( "ERR_MSG_OPEN_FILE_READER" );             //$NON-NLS-1$
  String ERR_MSG_OPEN_FILE_UNSUPPORT_ENCODING = Messages.getString( "ERR_MSG_OPEN_FILE_UNSUPPORT_ENCODING" ); //$NON-NLS-1$
  String ERR_MSG_OPEN_FILE_STREAM_READER      = Messages.getString( "ERR_MSG_OPEN_FILE_STREAM_READER" );      //$NON-NLS-1$
  String ERR_MSG_OPEN_FILE_NOT_FOUND          = Messages.getString( "ERR_MSG_OPEN_FILE_NOT_FOUND" );          //$NON-NLS-1$
  String ERR_MSG_OPEN_FILE_STREAM             = Messages.getString( "ERR_MSG_OPEN_FILE_STREAM" );             //$NON-NLS-1$

}
