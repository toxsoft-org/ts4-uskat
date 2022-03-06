package org.toxsoft.uskat.s5.server.sequences.reader;

import java.sql.Connection;
import java.sql.Statement;

import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.sequences.ISequenceFactory;

/**
 * Запрос чтения хранимых данных
 *
 * @author mvk
 */
public interface IS5SequenceReadQuery
    extends ICloseable {

  /**
   * Возвращает фронтенд сформировавший запрос
   *
   * @return {@link IS5FrontendRear} фронтенд. {@link IS5FrontendRear#NULL}: запрос сфорирован без фроненда.
   */
  IS5FrontendRear frontend();

  /**
   * Идентификатор запроса
   *
   * @return String идентификатор запроса
   */
  String queryId();

  /**
   * Возвращает интервал запроса значений
   *
   * @return IQueryInterval интервал запроса значений
   */
  IQueryInterval interval();

  /**
   * Возвращает фабрику последовательности значений
   *
   * @return ISequenceFactory фабрику последовательности значений
   */
  ISequenceFactory<?> factory();

  /**
   * Возвращает максимальное время(мсек) выполнения запроса.
   *
   * @return long время(мсек). < 1000: без ограничения
   */
  long maxExecutionTimeout();

  /**
   * Возвращает соединение с базой данных
   *
   * @return {@link Connection} соединение с базой данных
   */
  Connection connection();

  /**
   * Добавление в запрос подзапроса получения данных
   *
   * @param aStatement {@link Statement} подзапрос
   * @throws TsNullArgumentRtException аргумент = null
   */
  void addStatement( Statement aStatement );

  /**
   * Удаление из запроса подзапроса получения данных
   *
   * @param aStatement {@link Statement} подзапрос
   * @throws TsNullArgumentRtException аргумент = null
   */
  void removeStatement( Statement aStatement );
}
