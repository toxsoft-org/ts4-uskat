package org.toxsoft.uskat.s5.common.info;

import javax.ejb.SessionContext;

import org.toxsoft.uskat.s5.server.transactions.ETransactionStatus;

/**
 * Информация о транзакции s5-сервера
 *
 * @author mvk
 */
public interface ITransactionInfo {

  /**
   * Возвращает имя владельца сессии ({@link SessionContext#getCallerPrincipal()} открывшей транзакцию
   *
   * @return String имя сессии
   */
  String session();

  /**
   * Возвращает строковое представление идентификатора транзакции
   *
   * @return String идентификатор транзакции
   */
  String key();

  /**
   * Возвращает полное имя класса бизнес-метод которого открыл транзакцию
   *
   * @return String имя класса
   */
  String className();

  /**
   * Возвращает имя бизнес-метода класса {@link #className()} открывшего транзакцию
   *
   * @return String имя бизнес метода
   */
  String methodName();

  /**
   * Возвращает строковое представление значений аргументов метода {@link #methodName()} открывшего транзакцию
   *
   * @return String строковое представление значений аргументов перечисленных через запятую
   */
  String methodArgs();

  /**
   * Возвращает текущий статус транзакции
   *
   * @return {@link ETransactionStatus} статус транзакции
   */
  ETransactionStatus status();

  /**
   * Возвращает время открытия транзакции
   *
   * @return long время (мсек с начала)
   */
  long openTime();

  /**
   * Возвращает время последнего изменения статуса транзакции
   *
   * @return long время (мсек с начала)
   */
  long statusTime();

  /**
   * Возвращает время завершения или отмены транзакции
   *
   * @return long время (мсек с начала). 0: транзакция еще открыта
   */
  long closeTime();

  /**
   * Возвращает комментарий к транзакции сформированный при ее обработке
   *
   * @return String комментарий. Пустая строка: нет комментария
   */
  String description();

}
