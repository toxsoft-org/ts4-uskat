package org.toxsoft.uskat.skadmin.cli.parsers;

import static org.toxsoft.uskat.skadmin.cli.parsers.AdminCmdLexicalParser.*;

/**
 * Константы, локализуемые ресурсы парсеров консоли.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IAdminResources {

  /**
   * Формат представления даты-времени в строковом представлении
   */
  String DATETIME_FORMAT = "dd.MM.yyyy HH:mm:ss";

  // ------------------------------------------------------------------------------------
  // AdminCmdParser
  //
  String ERR_MSG_NOT_INITED                 = "Нет инициализации";
  String ERR_MSG_WRONG_FORMAT               = "Нарушение формата команды";
  String ERR_MSG_NOT_CANONIC                = "Команда определяется не в каноническом формате";
  String ERR_MSG_CMD_NOT_FOUND              = "Команда '%s' не существует.";
  String ERR_MSG_INVALID_CMD_ID             = "Команда должна быть ИД-путем.";
  String ERR_MSG_ARG_ID_EXPECTED            = "В позиции %d ожидался идентификатор аргумента или его алиас.";
  String ERR_MSG_INVALID_ARG_ID             = "В позиции %d указан неверный идентификатор аргумента.";
  String ERR_MSG_ARG_NOT_FOUND              = "Аргумент '%s' не существует.";
  String ERR_MSG_ARG_ALREADY_EXIST          = "Аргумент '%s' определяется несколько раз.";
  String ERR_MSG_VALUE_EXPECTED             = "В позиции %d ожидалось значение '%s'.";
  String ERR_MSG_REF_ARG_UNEXPECTED         = "В позиции %d неожиданное появление аргумента с типом объектных ссылок";
  String ERR_MSG_NAMED_ARG_UNEXPECTED       = "В позиции %d ожидался именованный параметр";
  String ERR_MSG_ARG_FLAG_INPOSSIBLE        =
      "В текущем наборе значений аргументов использование флага '%s' недопустимо.";
  String ERR_MSG_VALUE_UNEXPECTED           = "В позиции %d не ожидалось определение '%s'.";
  String ERR_MSG_VALUE_MUST_BE_SINGLE       = "Значение '%s' должно быть представлено единичным атомарным значением.";
  String ERR_MSG_VALUE_MUST_BE_LIST         = "Значение '%s' должно быть представлено списком атомарных значений.";
  String ERR_MSG_VALUE_MUST_BE_NAMED        = "Значение '%s' должно быть представлено именованным значением.";
  String ERR_MSG_UNPOSSIBLE_VALUE           = "Недопустимое значение '%s' для '%s'. Возможные значения: %s.";
  String ERR_MSG_INVALID_TYPE               = "Тип значений '%s' должен быть '%s'";
  String ERR_MSG_INVALID_VALUE              = "Аргумент '%s'. Недопустимый формат значения: '%s'.";
  String ERR_MSG_INVALID_PARAM_VALUE        =
      "Аргумент '%s'. Параметр '%s'. Недопустимый формат значения параметра: '%s'.";
  String ERR_MSG_NEED_STRING                = " Строковые значения должны быть указаны в кавычках.";
  String ERR_MSG_INVALID_QUOTED             =
      "Неверный формат строкового значения %s. Значение должно быть указано в кавычках.";
  String ERR_MSG_NOT_DEFINED_CONTEXT        = "Неопределены параметры контекста.";
  String ERR_MSG_NOT_FOUND_CONTEXT          = "В контексте не найден параметр '%s'.";
  String ERR_MSG_NOT_FOUND_NAMED_VALUE      = "Не найдено значение именованного параметра '%s'.";
  String ERR_MSG_CTX_READONLY               =
      "Параметр  '%s' не может быть записан в контекст так как он уже существует с признаком только чтение. ";
  String ERR_MSG_CTX_STATEMENT_EXPECTED     = "В позиции %d ожидался оператор утверждения параметров контекста.";
  String ERR_MSG_CTX_WRONG_STATEMENT        =
      "В позиции %d оператор утверждения '%s' не может быть использован для параметров контекста. Только '"
          + STATEMENT_WRITE + "' или '" + STATEMENT_APPLY + "'";
  String ERR_MSG_DOUBLE_EQ                  = "Неожиданное появление оператора '='.";
  String MSG_ERR_SUPER_QUOTE_EXPECTED       = "Вместо символа ('%c') ожидались супер кавычки (строка: %s)";
  String MSG_ERR_READ_SUPER_QUOTE           =
      "Ошибка чтения супер кавычек %s. Вместо символа ('%c') ожидались символ ('%c')";
  String MSG_ERR_CLOSE_SUPER_QUOTE_EXPECTED = "Не найдены закрывающие супер кавычки (строка: %s)";

}
