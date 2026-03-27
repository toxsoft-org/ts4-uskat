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
  String HELP_CMD_ID    = "help";
  String HELP_CMD_ALIAS = "h";
  String HELP_CMD_NAME  = "Помощь";
  String HELP_CMD_DESCR = "Справка по командам и аргументам";

  String HELP_ARG_CMD_ID    = "cmd";
  String HELP_ARG_CMD_ALIAS = "c";
  String HELP_ARG_CMD_NAME  = "Имя команды";
  String HELP_ARG_CMD_DESCR = "Имя команды по которой требуется вывести детальную справку";

  String HELP_ARG_ALL_ID      = "all";
  String HELP_ARG_ALL_ALIAS   = "a";
  String HELP_ARG_ALL_NAME    = "Флаг";
  String HELP_ARG_ALL_DESCR   = "Вывести справку по всем командам";
  String HELP_ARG_ALL_DEFAULT = "false";

  // ------------------------------------------------------------------------------------
  // ConsoleCmdClear
  //
  String CLEAR_CMD_ID    = "clear";
  String CLEAR_CMD_ALIAS = "";
  String CLEAR_CMD_NAME  = "Очистка экрана";
  String CLEAR_CMD_DESCR = "";

  // ------------------------------------------------------------------------------------
  // ConsoleCmdCd
  //
  String CD_CMD_ID    = "chdir";
  String CD_CMD_ALIAS = "cd";
  String CD_CMD_NAME  = "Смена раздела";
  String CD_CMD_DESCR = "Смена текущего раздела команд";

  String CD_ARG_SECTION_ID    = "section";
  String CD_ARG_SECTION_ALIAS = "s";
  String CD_ARG_SECTION_NAME  = "Имя раздела";
  String CD_ARG_SECTION_DESCR = "Раздел на который требуется осуществить переход";

  // ------------------------------------------------------------------------------------
  // ConsoleCmdLs
  //
  String LS_CMD_ID    = "list";
  String LS_CMD_ALIAS = "ls";
  String LS_CMD_NAME  = "Список команд и разделов";
  String LS_CMD_DESCR = "Вывод на экран разделов и команд доступных в разделе";

  String LS_ARG_SECTION_ID    = "section";
  String LS_ARG_SECTION_ALIAS = "s";
  String LS_ARG_SECTION_NAME  = "Имя раздела";
  String LS_ARG_SECTION_DESCR =
      "Раздел по которому требуется вывести информацию. Пустая строка: вывод по текущему разделу";

  String LS_ARG_DESCRIPTION_ID      = "description";
  String LS_ARG_DESCRIPTION_ALIAS   = "d";
  String LS_ARG_DESCRIPTION_NAME    = "Флаг";
  String LS_ARG_DESCRIPTION_DESCR   = "Вывести описание команд";
  String LS_ARG_DESCRIPTION_DEFAULT = "false";

  // ------------------------------------------------------------------------------------
  // ConsoleCmdBatch
  //
  String BATCH_CMD_ID    = "batch";
  String BATCH_CMD_ALIAS = "b";
  String BATCH_CMD_NAME  = "Выполнение сценария";
  String BATCH_CMD_DESCR =
      "Команда -batch построчно читает из представленного текстового файла команды skadmin и запускает их на выполнение. "
          + "Строки начинающиеся символом # игнорируются (строка комментарий).\n\n "
          + "Поиск скрипта по указанному имени выполняется в каталогах (в указанном порядке):\n"
          + "   * Каталог $APPLICATION_HOME/scripts;\n" + "   * Каталог $SKADMIN_HOME/scripts.\n";

  String BATCH_ARG_FILE_ID    = "file";
  String BATCH_ARG_FILE_ALIAS = "f";
  String BATCH_ARG_FILE_NAME  = "Имя файла";
  String BATCH_ARG_FILE_DESCR = "Текстовый файл с командами для выполнения";

  String BATCH_ARG_ARGS_ID    = "args";
  String BATCH_ARG_ARGS_ALIAS = "";
  String BATCH_ARG_ARGS_NAME  = "";
  String BATCH_ARG_ARGS_DESCR =
      "Список фактических значений аргументов для выполнения скрипта. В сценарии эти значения доступны через параметры контекста $1, $2,...$N";

  String BATCH_ARG_CHARSET_ID      = "charset";
  String BATCH_ARG_CHARSET_ALIAS   = "";
  String BATCH_ARG_CHARSET_NAME    = "";
  String BATCH_ARG_CHARSET_DESCR   = "Кодировка текстового файла";
  String BATCH_ARG_CHARSET_DEFAULT = "UTF8";

  String BATCH_ARG_EXIT_ID      = "exit";
  String BATCH_ARG_EXIT_ALIAS   = "";
  String BATCH_ARG_EXIT_NAME    = "";
  String BATCH_ARG_EXIT_DESCR   = "Признак завершения работы консоли после выполнения команд файла";
  String BATCH_ARG_EXIT_DEFAULT = "false";

  // ------------------------------------------------------------------------------------
  // ConsoleCmdHasParam
  //
  String HAS_CMD_ID           = "hasContextParam";
  String HAS_CMD_ALIAS        = "";
  String HAS_CMD_NAME         = "";
  String HAS_CMD_DESCR        = "Возвращает  признак существования параметра в текущем контексте";
  String HAS_CMD_RESULT_DESCR = "Признак существования параметра";

  String HAS_ARG_NAME_ID    = "name";
  String HAS_ARG_NAME_ALIAS = "";
  String HAS_ARG_NAME_NAME  = "";
  String HAS_ARG_NAME_DESCR = "Имя проверяемого параметра. ВНИМАНИЕ: без префикса '$'";

  // ------------------------------------------------------------------------------------
  // ConsoleCmdSignal
  //
  String SIGNAL_CMD_ID       = "signal";
  String SIGNAL_CMD_ALIAS    = "";
  String SIGNAL_CMD_NAME     = "управление сигналами";
  String SIGNAL_CMD_DESCR    = "Команда -signal позволяет устанавливать, ожидать и снимать сигналы. ";
  String SIGNAL_RESULT_DESCR = "Значение сигнала в строковом виде. ";

  String ARG_SIGNAL_ID_ID    = "id";
  String ARG_SIGNAL_ID_ALIAS = "";
  String ARG_SIGNAL_ID_NAME  = "Идентификатор";
  String ARG_SIGNAL_ID_DESCR = "Идентификатор сигнала";

  String ARG_SIGNAL_CMD_ID    = "cmd";
  String ARG_SIGNAL_CMD_ALIAS = "";
  String ARG_SIGNAL_CMD_NAME  = "";
  String ARG_SIGNAL_CMD_DESCR = "Команда управления сигналом. Допустимые значения: create, delete, wait, check";

  String ARG_SIGNAL_VALUE_ID      = "value";
  String ARG_SIGNAL_VALUE_ALIAS   = "";
  String ARG_SIGNAL_VALUE_NAME    = "Значение";
  String ARG_SIGNAL_VALUE_DESCR   = "Значение сигнала. Пустая строка - любое значение";
  String ARG_SIGNAL_VALUE_DEFAULT = "";

  String ARG_SIGNAL_TIMEOUT_ID      = "timeout";
  String ARG_SIGNAL_TIMEOUT_ALIAS   = "";
  String ARG_SIGNAL_TIMEOUT_NAME    = "";
  String ARG_SIGNAL_TIMEOUT_DESCR   = "Таймаут(сек) удержания или ожидания сигнала или его значения";
  String ARG_SIGNAL_TIMEOUT_DEFAULT = "5";

  String E_SIGNAL_N_CREATE = "Создание";
  String E_SIGNAL_N_DELETE = "Удаление";
  String E_SIGNAL_N_WAIT   = "Ожидание";

  String E_SIGNAL_D_CREATE = "Создание или удержание сигнала. Используются аргументы: -id, -value, -timeout";
  String E_SIGNAL_D_DELETE = "Удаление сигнала. Используются аргументы: -id";
  String E_SIGNAL_D_WAIT   = "Ожидание сигнала. Используются аргументы -id, -value, -timeout";

  String E_OPERATOR_N_NOOP = "Нет оператора";
  String E_OPERATOR_D_NOOP = "Нет оператора";

  String E_OPERATOR_N_IF = "Условие";
  String E_OPERATOR_D_IF = "Цикл";

  String E_OPERATOR_N_WHILE = "Оператор условия if";
  String E_OPERATOR_D_WHILE = "Оператор цикла while";

  String E_OPERATOR_N_END = "Завершение";
  String E_OPERATOR_D_END = "Оператор завершения блока команда предыдущего оператора if или while";

  String E_OPERATOR_N_RETURN = "Выход";
  String E_OPERATOR_D_RETURN = "Оператор завершения работы скрипта с формированием строкового результата";

  String E_OPERATOR_N_INCLUDE = "Включение";
  String E_OPERATOR_D_INCLUDE = "Оператор включения команд в сценарий из файла";

  // ------------------------------------------------------------------------------------
  // ConsoleCmdTimeout
  //
  String TIMEOUT_CMD_ID    = "timeout";
  String TIMEOUT_CMD_ALIAS = "";
  String TIMEOUT_CMD_NAME  = "таймаут";
  String TIMEOUT_CMD_DESCR = "Останавливает выполнение сценария или потока выполнения на указанное время. ";

  String ARG_TIMEOUT_VALUE_ID      = "value";
  String ARG_TIMEOUT_VALUE_ALIAS   = "";
  String ARG_TIMEOUT_VALUE_NAME    = "";
  String ARG_TIMEOUT_VALUE_DESCR   = "Значение таймаута(мсек)";
  String ARG_TIMEOUT_VALUE_DEFAULT = "1000";

  // ------------------------------------------------------------------------------------
  // ConsoleCmdTimeToString
  //
  String TIME_TO_STRING_CMD_ID    = "timeToString";
  String TIME_TO_STRING_CMD_ALIAS = "";
  String TIME_TO_STRING_CMD_NAME  = "Вывод времени";
  String TIME_TO_STRING_CMD_DESCR = "Выводит метку времени в текстовом виде.";

  String ARG_TIME_ID    = "time";
  String ARG_TIME_ALIAS = "";
  String ARG_TIME_NAME  = "";
  String ARG_TIME_DESCR = "Значение метки времени (мсек с начала эпохи)";

  String TIME_TO_STRING_RESULT_DESCR = "Значение метки времени в текстовом виде. ";

  // ------------------------------------------------------------------------------------
  // ConsoleCmdEcho
  //
  String ECHO_CMD_ID       = "echo";
  String ECHO_CMD_ALIAS    = "";
  String ECHO_CMD_NAME     = "Вывод текста";
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
  String ECHO_RESULT_DESCR = "Текст выведенный на экран";

  String ECHO_ARG_TEXT_ID    = "text";
  String ECHO_ARG_TEXT_ALIAS = "t";
  String ECHO_ARG_TEXT_NAME  = "Выводимый текст";
  String ECHO_ARG_TEXT_DESCR = "Выводимый текст c элементами переключения цвета и фона";

  String ECHO_ARG_EOL_ID      = "eol";
  String ECHO_ARG_EOL_ALIAS   = "e";
  String ECHO_ARG_EOL_NAME    = "Конец строки";
  String ECHO_ARG_EOL_DESCR   = "Добавлять у указанному тексту символ конца строки";
  String ECHO_ARG_EOL_DEFAULT = "false";

  String ECHO_ARG_SILENT_ID      = "silent";
  String ECHO_ARG_SILENT_ALIAS   = "s";
  String ECHO_ARG_SILENT_NAME    = "";
  String ECHO_ARG_SILENT_DESCR   = "Не выводить текст на экран (только формировать результат)";
  String ECHO_ARG_SILENT_DEFAULT = "false";

  String ECHO_ARG_SPACE_TRAIL_ID      = "spaceTrail";
  String ECHO_ARG_SPACE_TRAIL_ALIAS   = "";
  String ECHO_ARG_SPACE_TRAIL_NAME    = "Дополнение пробелами";
  String ECHO_ARG_SPACE_TRAIL_DESCR   = "Добавлять строку пробелами до правой границы экрана";
  String ECHO_ARG_SPACE_TRAIL_DEFAULT = "false";

  // ------------------------------------------------------------------------------------
  // ConsoleCmdEquals
  //
  String IS_EQUAL_CMD_ID           = "isEqual";
  String IS_EQUAL_CMD_ALIAS        = "isEq";
  String IS_EQUAL_CMD_NAME         = "Сравнение параметров контекста";
  String IS_EQUAL_CMD_DESCR        = "Сравнение параметров контекста";
  String IS_EQUAL_CMD_RESULT_DESCR = "Признак того, что параметры контекста имеют эквивалентные значения";

  String IS_EQUAL_ARG_PAR1_ID    = "param1";
  String IS_EQUAL_ARG_PAR1_ALIAS = "p1";
  String IS_EQUAL_ARG_PAR1_NAME  = "param1";
  String IS_EQUAL_ARG_PAR1_DESCR = "Первый параметр контекста";

  String IS_EQUAL_ARG_PAR2_ID    = "param2";
  String IS_EQUAL_ARG_PAR2_ALIAS = "p2";
  String IS_EQUAL_ARG_PAR2_NAME  = "param2";
  String IS_EQUAL_ARG_PAR2_DESCR = "Второй параметр контекста";

  // ------------------------------------------------------------------------------------
  // ConsoleCmdExit
  //
  String EXIT_CMD_ID    = "exit";
  String EXIT_CMD_ALIAS = "";
  String EXIT_CMD_NAME  = "Выход";
  String EXIT_CMD_DESCR = "Завершение работы с сохранением настроек консоли";

  // ------------------------------------------------------------------------------------
  // ConsoleCmdQuit
  //
  String QUIT_CMD_ID    = "quit";
  String QUIT_CMD_ALIAS = "";
  String QUIT_CMD_NAME  = "Выход";
  String QUIT_CMD_DESCR = "Завершение работы без сохранения настроек консоли";

  // ------------------------------------------------------------------------------------
  // Сообщения
  //
  String MSG_CHILD_SECTION        = "Раздел команд";
  String MSG_CMD_INFO             = "Раздел   : " + COLOR_ID + "%s\n" + COLOR_RESET + "Имя      : " + COLOR_ID + "%s\n"
      + COLOR_RESET + "Роли     : %s\n" + "Кратко   : %s";
  String MSG_CMD_ALIAS_INFO       = "Раздел   : " + COLOR_ID + "%s\n" + COLOR_RESET + "Имя,алиас: " + COLOR_ID + "%s\n"
      + COLOR_RESET + "Роли     : %s\n" + "Кратко   : %s";
  String MSG_CMD_DESCR            = "Описание : ";
  String MSG_ROLES_ALL            = "Без ограничений";
  String MSG_CMD_CONTEXT          = "Контекст : ";
  String MSG_CMD_CONTEXT_NOT_USED = "Контекст : Не используется";
  String MSG_CMD_CONTEXT_INFO     = "%s  $%-14s %-20s" + COLOR_RESET;
  String MSG_CMD_CONTEXT_DESCR    = " %s.";
  String MSG_CMD_ARGS             = "Аргументы: ";
  String MSG_CMD_ARG_INFO         = "%s  %-15s%s %-20s" + COLOR_RESET;
  String MSG_CMD_ARG_DESCR        = " %s.";
  String MSG_CMD_ARG_DEFAULT      = " Значение по умолчанию: " + COLOR_SINGLE_VALUE + "%s" + COLOR_RESET + ".";
  String MSG_CMD_RESULT           = "Результат: %s       %-20s " + COLOR_RESET;
  String MSG_NO_RESULT            = "Результат: Нет\n";
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
  String ERR_MSG_CMD_REJECT            = "Пользователь отказался от выполнения команды '%s'";
  String ERR_MSG_CMD_NOT_FOUND         = "Команда '%s' не существует";
  String ERR_MSG_SECTION_NOT_FOUND     = "Раздел '%s' не существует";
  String ERR_MSG_FILE_NOT_FOUND        = "Файл '%s' не существует. Введите другой файл";
  String ERR_MSG_SIGNAL_DELETE_TIMEOUT =
      "Невозможно (нет сигнала или значения) удалить сигнал '%s' по таймауту: %d сек. ";
  String ERR_MSG_SIGNAL_WAIT_TIMEOUT   = "Невозможно получить сигнал '%s' или его значения по таймауту: %d сек. ";

  String ERR_MSG_SCRIPT              = "Ошибка скрипта %s[%d]: %s\n";
  String ERR_MSG_UNEXPECTED_OPERATOR = "Неожиданное появление оператора: %s";
  String ERR_MSG_WRONG_IF_FORMAT     =
      "Неверный формат оператора if. Формат должен быть 'if logicalValue' или 'if numberValue1 op numberValue2'";
  String ERR_MSG_EXPECTED_LOGICAL    = "Ожидается значение логического типа. Причина: %s'";
  String ERR_MSG_COMPARE_IMPOSSIBLE  = "Невозможно сравнить операнды. value1: %s op: %s, value2: %s. Причина: %s'";
  String ERR_MSG_UNKNOW_COMPARE      = "Неизвестный тип сравнения: %s'";
  String MSG_ERR_CMD_UNEXPECTED      = "Неожиданная ошибка выполнения сценария %s[%d]. Причина: '%s'";
  String ERR_MSG_UNCLOSED_OPERATOR   = "Не найдено завершения оператора'";
  String ERR_MSG_INCLUDE_NOT_FOUND   = COLOR_ERROR + "Невозможно включить файл %s. Файл не найден\n" + COLOR_RESET;
  String ERR_MSG_UNCLOSED_MULTILINE  = "Незавершено формирование многострочной команды'";

  String ERR_MSG_OPEN_FILE_READER             =
      "Не могу открыть файл сценария для чтения (BufferedReader): %s. Причина: %s";
  String ERR_MSG_OPEN_FILE_UNSUPPORT_ENCODING =
      "Не могу открыть файл сценария для чтения (неподдерживаемая кодировка символов): %s. Причина: %s";
  String ERR_MSG_OPEN_FILE_STREAM_READER      =
      "Не могу открыть файл сценария для чтения (InputStreamReader): %s. Причина: %s";
  String ERR_MSG_OPEN_FILE_NOT_FOUND          =
      "Не могу открыть файл сценария для чтения (файл не найден): %s. Причина: %s";
  String ERR_MSG_OPEN_FILE_STREAM             =
      "Не могу открыть файл сценария для чтения (FileInputStream): %s. Причина: %s";

}
