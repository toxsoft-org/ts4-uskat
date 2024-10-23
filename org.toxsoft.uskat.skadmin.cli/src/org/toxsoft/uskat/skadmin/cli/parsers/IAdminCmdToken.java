package org.toxsoft.uskat.skadmin.cli.parsers;

import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Лексема лексического или синтаксического анализа командной строки
 *
 * @author mvk
 */
public interface IAdminCmdToken {

  /**
   * Тип лексемы
   *
   * @return {@link ETokenType} - тип лексемы
   */
  ETokenType type();

  /**
   * Начальный индекс лексемы в строке
   *
   * @return индекс в строке
   */
  int startIndex();

  /**
   * Конечный(включительно) индекс лексемы в строке
   *
   * @return индекс в строке
   */
  int finishIndex();

  /**
   * Индекс лексемы в списке лексем типа {@link ETokenType#LIST_VALUE}
   *
   * @return int индекс в списке лексем
   * @throws TsIllegalStateRtException тип лексемы отличается от {@link ETokenType#LIST_VALUE}
   */
  int listIndex();

  /**
   * Данные лексемы
   *
   * @return данные лексемы
   */
  String data();

  /**
   * Возвращает признак того, что данные лексемы находились в кавычках
   *
   * @return <b>true</b> данные лексемы находились в кавычках; <b>false</b> данные лексемы были без кавычек
   */
  boolean quoted();
}
