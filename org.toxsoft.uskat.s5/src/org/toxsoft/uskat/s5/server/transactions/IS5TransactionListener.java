package org.toxsoft.uskat.s5.server.transactions;

import javax.ejb.Local;

import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;

/**
 * Слушатель транзакций
 *
 * @author mvk
 */
@Local
public interface IS5TransactionListener {

  /**
   * Вызов проверяет ресурсы транзакции на предмет ее завершения
   * <p>
   * Метод не гарантирует, что транзакция после этого будет завершена и используется только для предупреждающего
   * сообщения (бизнес-ошибки состояния), до вызова процесса завершения транзакции
   *
   * @param aTransaction {@link IS5Transaction} транзакция изменившая свой статус
   * @throws TsIllegalArgumentRtException бизнес-ошибка по которой невозможно завершить транзакцию
   */
  default void checkCommitResources( IS5Transaction aTransaction ) {
    // nop
  }

  /**
   * Оповещение: транзакция изменила свой статус
   *
   * @param aTransaction {@link S5Transaction} транзакция изменившая свой статус
   */
  default void changeTransactionStatus( IS5Transaction aTransaction ) {
    // nop
  }
}
