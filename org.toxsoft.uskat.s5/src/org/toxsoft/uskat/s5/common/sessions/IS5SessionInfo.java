package org.toxsoft.uskat.s5.common.sessions;

import java.io.ObjectStreamException;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullObjectErrorRtException;
import org.toxsoft.uskat.s5.client.remote.connection.S5ClusterTopology;
import org.toxsoft.uskat.s5.server.statistics.IS5Statistic;

import ru.uskat.core.api.users.ISkSession;

/**
 * Информация о сессии пользователя
 *
 * @author mvk
 */
public interface IS5SessionInfo {

  /**
   * Пустая (нет связи с сервером) информация о сессии пользователя
   */
  IS5SessionInfo NULL = new NullSessionInfo();

  /**
   * Возвращает идентификатор сессии пользователя {@link ISkSession}
   *
   * @return {@link Skid} идентификатор сессии.
   */
  Skid sessionID();

  /**
   * Возвращает удаленный адрес клиента (IP или сетевое имя)
   *
   * @return @link String адрес клиента. Пустая строка: адрес неопределен
   */
  String remoteAddress();

  /**
   * Возвращает удаленный порт клиента
   *
   * @return int адрес клиента. <0: адрес неопределен
   */
  int remotePort();

  /**
   * Возвращает время открытия сессии пользователя
   *
   * @return long время (мсек с начала эпохи)
   */
  long openTime();

  /**
   * Возвращает время закрытия сессии пользователя
   *
   * @return long время (мсек с начала эпохи). {@link TimeUtils#MAX_TIMESTAMP} сессия не закрыта
   */
  long closeTime();

  /**
   * Возвращает учетное имя пользователя открывшего сессию сервера
   *
   * @return String учетное имя пользователя
   */
  String login();

  /**
   * Возвращает информацию о топологии кластеров доступных клиенту
   *
   * @return {@link S5ClusterTopology} информация о топологии
   */
  S5ClusterTopology clusterTopology();

  /**
   * Возвращает параметры подключения клиента к серверу
   *
   * @return {@link IOptionSet} параметры подключения
   */
  IOptionSet clientOptions();

  /**
   * Возвращает статистические данные сессии
   *
   * @return {@link IS5Statistic} статистические данные
   */
  IS5Statistic statistics();

  /**
   * Признак завершения сессии по инициативе удаленной стороны
   *
   * @return boolean <b>true</b> сессия завершена по инициативе удаленной стороны; <b>false</b> сессия открыта или
   *         завершена системой.
   */
  boolean closeByRemote();
}

/**
 * Реализация пустой сессии пользователя
 *
 * @author mvk
 */
class NullSessionInfo
    implements IS5SessionInfo {

  /**
   * Метод корректно восстанавливает сериализированный {@link IS5SessionInfo#NULL}.
   *
   * @return Object объект {@link IS5SessionInfo#NULL}
   * @throws ObjectStreamException это обявление, оно тут не выбрасывается
   */
  @SuppressWarnings( { "static-method" } )
  private Object readResolve()
      throws ObjectStreamException {
    return IS5SessionInfo.NULL;
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5SessionInfo
  //
  @Override
  public String remoteAddress() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public int remotePort() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public long openTime() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public long closeTime() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public String login() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public Skid sessionID() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public S5ClusterTopology clusterTopology() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public IOptionSet clientOptions() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public IS5Statistic statistics() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public boolean closeByRemote() {
    throw new TsNullObjectErrorRtException();
  }
}
