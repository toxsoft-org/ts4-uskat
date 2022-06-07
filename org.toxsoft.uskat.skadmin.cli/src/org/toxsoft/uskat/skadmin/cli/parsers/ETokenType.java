package org.toxsoft.uskat.skadmin.cli.parsers;

/**
 * Типы лексем
 */
public enum ETokenType {
  /**
   * Неизвестный тип
   */
  UNDEF,
  /**
   * Единичный параметр контекста. Например: $editor0
   */
  CONTEXT,
  /**
   * Список параметров контекста. Например: $editor0, $editor1, $editor2
   */
  LIST_CONTEXT,
  /**
   * Оператор утверждения. Например: =, ->, >, <, =>, =<, ==, !=
   */
  STATEMENT,
  /**
   * Идентификатор
   */
  ID,
  /**
   * Значение аргумента
   */
  VALUE,
  /**
   * Элемент списка-значения аргумента
   */
  LIST_VALUE,
  /**
   * Именованное значение
   */
  NAMED_VALUE,
}
