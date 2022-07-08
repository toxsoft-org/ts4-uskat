package org.toxsoft.uskat.s5.server.sessions.init;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.client.IS5ConnectionParams;
import org.toxsoft.uskat.s5.client.remote.connection.S5ClusterTopology;
import org.toxsoft.uskat.s5.common.sessions.ISkSession;
import org.toxsoft.uskat.s5.server.frontend.IS5BackendAddonData;

/**
 * Данные инициализации сессии
 *
 * @author mvk
 */
public interface IS5SessionInitData {

  /**
   * Возвращает идентификатор сессии
   *
   * @return {@link Skid} идентификатор сессии {@link ISkSession}.
   */
  default Skid sessionID() {
    return IS5ConnectionParams.OP_SESSION_ID.getValue( clientOptions() ).asValobj();
  }

  /**
   * Возвращает параметры подключения клиента к серверу
   *
   * @return {@link IOptionSet} параметры подключения
   */
  IOptionSet clientOptions();

  /**
   * Возвращает информацию о топологии кластеров доступных клиенту
   *
   * @return {@link S5ClusterTopology} информация о топологии
   */
  S5ClusterTopology clusterTopology();

  /**
   * Возвращает данные расширения бекенда
   *
   * @param aAddonId String идентификатор (ИД-путь) расширения
   * @param aAddonDataType Java-тип данных расширения
   * @return {@link IS5BackendAddonData} данные расширения. null: данных нет
   * @param <T> тип данных расширения
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws ClassCastException данные расширение есть, но они не запрошенного типа
   */
  <T extends IS5BackendAddonData> T findBackendAddonData( String aAddonId, Class<T> aAddonDataType );
}
