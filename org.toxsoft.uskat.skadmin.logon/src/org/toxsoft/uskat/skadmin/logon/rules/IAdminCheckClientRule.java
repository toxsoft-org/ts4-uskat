package org.toxsoft.uskat.skadmin.logon.rules;

import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.statistics.EStatisticInterval;

/**
 * Правило проверки клиента
 *
 * @author mvk
 */
public interface IAdminCheckClientRule {

  /**
   * Возвращает тип правила
   *
   * @return {@link EClientRuleType} тип правила
   */
  EClientRuleType type();

  /**
   * Возвращает время (сек) от начала суток начала действия правила.
   *
   * @return int время(сек) от начала суток начала действия правила (включительно). < 0: не установлено
   */
  int startTime();

  /**
   * Возвращает время (сек) от начала суток завершения действия правила.
   *
   * @return int время(сек) от начала суток завершения действия правила (невключительно). < 0: не установлено
   */
  int endTime();

  /**
   * Возвращает ip-адрес клиента
   *
   * @return String ip-адрес клиента. Пустая строка: любой ip
   */
  String ip();

  /**
   * Возвращает порт клиента
   *
   * @return int порт клиента. < 0: любой порт
   */
  int port();

  /**
   * Возвращает login клиента
   *
   * @return String login клиента. Пустая строка: любой логин
   */
  String login();

  /**
   * Возвращает идентификаторы особенностей клиента.
   *
   * @return {@link IStringList} список идентификаторов (ИД-пути) особенностей клиента по ИЛИ. Пустой список: любой тип
   *         клиента
   */
  IStringList clientFeatureIds();

  /**
   * Возвращает минимальное количество отправленных сообщений клиенту
   *
   * @param aInterval {@link EStatisticInterval} интервал времени по которому запрашиваются данные
   * @return int количество отправленных сообщений. < 0: не установлено
   * @throws TsNullArgumentRtException аргумент = null
   */
  int sendedMin( EStatisticInterval aInterval );

  /**
   * Возвращает максимальное количество отправленных сообщений клиенту
   *
   * @param aInterval {@link EStatisticInterval} интервал времени по которому запрашиваются данные
   * @return int количество отправленных сообщений. < 0: не установлено
   * @throws TsNullArgumentRtException аргумент = null
   */
  int sendedMax( EStatisticInterval aInterval );

  /**
   * Возвращает минимальное количество принятых сообщений от клиента
   *
   * @param aInterval {@link EStatisticInterval} интервал времени по которому запрашиваются данные
   * @return int количество принятых сообщений. < 0: не установлено
   * @throws TsNullArgumentRtException аргумент = null
   */
  int receivedMin( EStatisticInterval aInterval );

  /**
   * Возвращает максимальное количество принятых сообщений от клиента
   *
   * @param aInterval {@link EStatisticInterval} интервал времени по которому запрашиваются данные
   * @return int количество принятых сообщений. < 0: не установлено
   * @throws TsNullArgumentRtException аргумент = null
   */
  int receivedMax( EStatisticInterval aInterval );

  /**
   * Возвращает минимальное количество запросов от клиента
   *
   * @param aInterval {@link EStatisticInterval} интервал времени по которому запрашиваются данные
   * @return int количество принятых запросов. < 0: не установлено
   * @throws TsNullArgumentRtException аргумент = null
   */
  int queriesMin( EStatisticInterval aInterval );

  /**
   * Возвращает максимальное количество запросов от клиента
   *
   * @param aInterval {@link EStatisticInterval} интервал времени по которому запрашиваются данные
   * @return int количество принятых запросов. < 0: не установлено
   * @throws TsNullArgumentRtException аргумент = null
   */
  int queriesMax( EStatisticInterval aInterval );

  /**
   * Возвращает максимальное количество ошибок обработки запросов или данных клиента
   *
   * @param aInterval {@link EStatisticInterval} интервал времени по которому запрашиваются данные
   * @return int количество ошибок обработки. < 0: не установлено
   * @throws TsNullArgumentRtException аргумент = null
   */
  int errorsMax( EStatisticInterval aInterval );

}
