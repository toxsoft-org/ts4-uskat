package org.toxsoft.uskat.s5.server.transactions;

import javax.ejb.Local;

import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.common.info.ITransactionsInfos;

/**
 * Локальный интерфейс синглтона мониторнига и управления транзакциями сервера s5
 *
 * @author mvk
 */
@Local
public interface IS5TransactionManagerSingleton {

  /**
   * Возвращает количество выполняемых транзакций на текущий момент времени
   *
   * @return int количество выполняемых транзакций
   */
  int openCount();

  /**
   * Возвращает текущую информацию о транзакциях s5-сервера
   *
   * @return {@link ITransactionsInfos} информация о транзакциях
   */
  ITransactionsInfos getInfos();

  /**
   * Возвращает текущую транзакцию
   *
   * @return {@link IS5Transaction} транзакция. null: нет транзакции
   */
  IS5Transaction findTransaction();

  /**
   * Возвращает текущую транзакцию
   *
   * @return {@link IS5Transaction} транзакция
   * @throws TsIllegalStateRtException нет транзакции
   */
  IS5Transaction getTransaction();

  /**
   * Вызов проверяет, возможно ли завершить транзакцию
   * <p>
   * Метод не гарантирует, что транзакция после этого будет завершена и используется только для предупреждающего
   * сообщения (бизнес-ошибки состояния), до вызова процесса завершения транзакции
   *
   * @throws TsIllegalStateRtException нет текущей транзакции
   * @throws TsIllegalArgumentRtException бизнес-ошибка по которой невозможно завершить транзакцию
   */
  void checkCommitReady();

  // ------------------------------------------------------------------------------------
  // Управление
  //
  // void begin();
  //
  // void commit();
  //
  // void rollback();
  //
  // void rollbackNow();

  // ------------------------------------------------------------------------------------
  // Извещения
  //
  /**
   * Добавляет слушателя изменения статуса ВСЕХ транзакций.
   * <p>
   * Если такой слушатель уже зарегистрирован, метод ничего не делает.
   *
   * @param aListener {@link IS5TransactionListener} - слушатель
   * @throws TsNullArgumentRtException аргумент = null
   */
  void addTransactionListener( IS5TransactionListener aListener );

  /**
   * Удаляет слушателя изменения статуса ВСЕХ транзакций.
   * <p>
   * Если такой слушатель не зарегистрирован, то метод ничего не делает.
   *
   * @param aListener {@link IS5TransactionListener} - слушатель
   * @throws TsNullArgumentRtException аргумент = null
   */
  void removeTransactionListener( IS5TransactionListener aListener );
}
