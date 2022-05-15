package org.toxsoft.uskat.skadmin.core.impl;

/**
 * Константы, локализуемые ресурсы реализации библиотеки {@link AdminCmdLibraryManager}.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IAdminResources {

  String CHAR_APOSTROPHE = "'";
  String CHAR_COMMA      = ",";
  String CHAR_EQUAL      = "=";

  String CHAR_YES = "y";
  String CHAR_NO  = "n";

  String MSG_LIBRARY_LOAD    = "Загрузка библиотеки(плагина): %s.";
  String MSG_LIBRARY_CONTEXT = "Установка контекста команд для библиотеки(плагина): %s.";
  String MSG_LIBRARY_CLOSE   = "Завершение работы библиотеки(плагина): %s.";

  String ERR_LIBRARY_LOAD               = "Ошибка загрузка библиотеки: pluginId: %s, plugintype = %s";
  String ERR_CMD_DOUBLE_DEFINE          = "Команда '%s' определяется более чем в одной библиотеке:  '%s' и '%s'.";
  String ERR_CMD_NOT_RESULT_DESCR       = "Команда '%s' определяет результат %s, но не имеет его описания.";
  String ERR_CMD_NOT_FOUND              = "Не найдена команда '%s'.";
  String ERR_CMD_ID_MUST_PATH           =
      "Библиотека '%s': невалидный идентификатор команды '%s'. Должен быть ИД-путь.";
  String ERR_CMD_ALREADY_EXIST          = "Команда '%s' уже существует.";
  String ERR_CMD_NOT_ID                 = "Команда определяется без идентификатора и алиаса";
  String ERR_ARG_NOT_FOUND              = "В команде '%s' не найден аргумент '%s'.";
  String ERR_DOUBLE_ARG                 = "Адресация одновременно через идентификатор '%s' и алиас '%s' запрещена.";
  String ERR_ARG_UNKNOW_ARGS            = "Для команды '%s' определены неизвестные аргументы: %s.";
  String ERR_ARG_WRONG_REF_TYPE         = "В команде '%s' тип значений аргумента '%s' должен быть '%s'.";
  String ERR_ARG_WRONG_TYPE             = "В команде '%s' тип значений аргумента '%s' не может быть '%s'.";
  String ERR_ARG_WRONG_CONTEXT_TYPE     =
      "Команда '%s' не может использовать параметр(аргумент) контекста '%s' имеющий тип  '%s'.";
  String ERR_ARG_WRONG_ITEM_TYPE        = "В команде '%s' тип элементов значения аргумента '%s' должен быть '%s'.";
  String ERR_ARG_CANT_BOOL_LIST_NARROW  =
      "В команде '%s' значение аргумента '%s' не может быть представлено в виде списка логических значений.";
  String ERR_ARG_CANT_STR_LIST_NARROW   =
      "В команде '%s' значение аргумента '%s' не может быть представлено в виде списка строк.";
  String ERR_ARG_CANT_INT_LIST_NARROW   =
      "В команде '%s' значение аргумента '%s' не может быть представлено в виде списка целых значений.";
  String ERR_ARG_CANT_FLOAT_LIST_NARROW =
      "В команде '%s' значение аргумента '%s' не может быть представлено в виде списка вещественных значений.";
  String ERR_ARG_CANT_OPTIONS_NARROW    =
      "В команде '%s' значение аргумента '%s': нарушение формата определения набора именованных значений. Должно быть: -имя аргумента \"имя_параметра1=значение_параметра1\", \"имя_параметра2=значение_параметра2\",...,\"имя_параметраN=значение_параметраN\".";
  String ERR_ARG_ALREADY_EXIST          = "Команда '%s', аргумент '%s' уже существует.";
  String ERR_ARG_ID_MUST_NAME           = "Команда '%s': невалидный идентификатор аргумента '%s'. Должно быть ИД-имя.";
  String ERR_ARG_NOT_ID                 = "Аргумент команды '%s' определяется без идентификатора и алиаса.";
  String ERR_CMD_NOT_EXECUTE            = "Команда '%s' в данный момент не исполняется.";
  String ERR_CMD_NOT_RESULT             = "Команда '%s' не сформировала результат.";
  String ERR_RESULT_MUST_BE             = "Команда '%s' должна формировать результат.";
  String ERR_RESULT_WRONG_TYPE          = "Команда должна формировать результат %s, а не %s.";

  String ERR_CONTEXT_NOT_FOUND          = "В контексте нет параметра с именем: %s.";
  String ERR_CONTEXT_READONLY           = "Значение параметра контекста %s доступно только для чтения.";
  String ERR_CLOSED                     = "Библиотека завершила свою работу.";
  String ERR_RESULT                     = "При выполнении команды %s произошла ошибка. Trace: %s";
  String ERR_WRONG_PARENT_IMPL          =
      "Родительский контекст должен быть создан с помощью фабрики AdminCmdLibraryUtils";
  String ERR_NOT_RESULT                 = "Нет результата выполнения команды. isOk()==false";
  String ERR_RESULT_ALREADY_FINISHED    = "Результат выполнения команды уже сформирован";
  String ERR_WRONG_TYPE_RESULT          = "Значение результата %s должно иметь plexy-тип %s";
  String ERR_WRONG_OBJREF_RESULT        =
      "Тип фактического значения результата объектной ссылки %s не может преобразовано к типу результата описанному в команде %s";
  String ERR_WRONG_VALUE_RESULT         =
      "Тип фактического значения результата %s не соответствуюет заявленному описанному в команде %s";
  String ERR_CMD_CMD_WITHOUT_RESULT_CTX =
      "Команда '%s' не регистрирует изменение в контексте параметра %s. Cмотри документацию IAdminCmd.resultContextParams()";
  String ERR_READ_CONTEXT               = "Ошибка чтения параметра [%d](%s): %s";
}
