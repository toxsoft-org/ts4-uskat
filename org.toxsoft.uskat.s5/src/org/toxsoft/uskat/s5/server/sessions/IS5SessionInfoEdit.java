package org.toxsoft.uskat.s5.server.sessions;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.client.remote.connection.S5ClusterTopology;
import org.toxsoft.uskat.s5.common.sessions.IS5SessionInfo;
import org.toxsoft.uskat.s5.server.statistics.IS5Statistic;

import ru.uskat.core.api.users.ISkSession;

/**
 * Описание backend-сессии пользователя с возможностью изменения отдельных параметров
 */
public interface IS5SessionInfoEdit
    extends IS5SessionInfo, ICloseable {

  /**
   * Установить идентификатор сессии {@link ISkSession}
   *
   * @param aSessionID Skid идентификатор сессии
   * @throws TsNullArgumentRtException аргумент = null
   */
  void setSessionID( Skid aSessionID );

  /**
   * Установить адрес удаленного клиента
   *
   * @param aRemoteAddress String IP-адрес или сетевое имя удаленного клиента сессии
   * @param aRemotePort int номер порта
   * @throws TsNullArgumentRtException аргумент = null
   */
  void setRemoteAddress( String aRemoteAddress, int aRemotePort );

  /**
   * Установить описание топологии кластеров доступных клиенту
   *
   * @param aClusterTopology {@link S5ClusterTopology} описание топологии
   * @throws TsNullArgumentRtException аргумент = null
   */
  void setClusterTopology( S5ClusterTopology aClusterTopology );

  /**
   * Возвращает информацию о пользователе сессии с возможностью редактирования
   *
   * @return {@link IOptionSet} информация о пользователе сессии
   */
  @Override
  IOptionSet client();

  /**
   * Возвращает статистические данные сессии
   *
   * @return {@link IS5Statistic} статистические данные
   */
  @Override
  IS5Statistic statistics();

  /**
   * Устанановить значение признака того, что соединение было завершено по инициативе удаленной стороны
   *
   * @param aCloseByRemote boolean <b>true</b> сессия завершена удаленной стороной; <b>false</b> сессия открыта или
   *          завершена системой
   */
  void setCloseByRemote( boolean aCloseByRemote );
}
