package org.toxsoft.uskat.s5.common.info;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Информация о транзакциях сервера s5
 *
 * @author mvk
 */
public interface ITransactionsInfos {

  /**
   * Возвращает количество подтвержденных (успешно закрытых) транзакций с момента запуска сервера.
   *
   * @return long количество подтвержденных транзакций.
   * @throws TsNullArgumentRtException аргумент = null.
   * @throws TsItemNotFoundRtException не найдена сессия службы с идентификатором из указанного списка.
   */
  long commitCount();

  /**
   * Возвращает количество отмененных транзакций с момента запуска сервера.
   *
   * @return long количество откатов по транзакциям.
   * @throws TsNullArgumentRtException аргумент = null.
   * @throws TsItemNotFoundRtException не найдена сессия службы с идентификатором из указанного списка.
   */
  long rollbackCount();

  /**
   * Возвращает список открытых транзакций
   *
   * @return {@link IList}&lt;{@link ITransactionInfo} - список описаний транзакций
   */
  IList<ITransactionInfo> openInfos();

  /**
   * Возвращает список последних завершенных транзакций
   *
   * @return {@link IList}&lt;{@link ITransactionInfo} - список описаний транзакций
   */
  IList<ITransactionInfo> commitedInfos();

  /**
   * Возвращает список последних отмененных транзакций
   *
   * @return {@link IList}&lt;{@link ITransactionInfo} - список описаний транзакций
   */
  IList<ITransactionInfo> rollbackedInfos();

  /**
   * Возвращает список транзакций с самым длительным временем завершения или отката
   *
   * @return {@link IList}&lt;{@link ITransactionInfo} - список описаний транзакций
   */
  IList<ITransactionInfo> longTimeInfos();
}
