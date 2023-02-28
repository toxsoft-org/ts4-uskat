package org.toxsoft.uskat.s5.server.sequences.reader;

import java.sql.Connection;
import java.sql.Statement;

import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceFactory;

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
   * @return IS5SequenceFactory фабрику последовательности значений
   */
  IS5SequenceFactory<?> factory();

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
   * Возвращает признак того, выполнение запроса было завершено вызовом {@link #close()}.
   *
   * @return <b>true</b> выполнение запроса завершено
   */
  boolean isClosed();

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

  /**
   * Устанавливает поток в рамках которого выполняется запрос
   *
   * @param aThread {@link Thread} поток
   * @throws TsNullArgumentRtException аргумент = null
   */
  void setThread( Thread aThread );
}
