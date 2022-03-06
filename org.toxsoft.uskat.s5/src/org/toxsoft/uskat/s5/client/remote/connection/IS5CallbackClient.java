package org.toxsoft.uskat.s5.client.remote.connection;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.toxsoft.core.pas.common.IPasTxChannel;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddon;

/**
 * Получатель обратных вызовов сервера
 *
 * @author mvk
 */
public interface IS5CallbackClient
    extends ICloseable {

  /**
   * Подготовка канала получателя к работе в рамках S5Connection
   *
   * @param aTopology {@link S5ClusterTopology} топология кластеров сервера доступная сессии клиента
   * @return {@link InetAddress} локальный адрес на котором принимаются обратные вызовы
   * @throws TsNullArgumentRtException любой аргумент null
   */
  InetSocketAddress start( S5ClusterTopology aTopology );

  /**
   * Разрешить уведомлять соединение о событиях
   */
  void setNotificationEnabled();

  /**
   * Возвращает открытый канал для передачи
   *
   * @return {@link IPasTxChannel} канал передачи. null: нет открытых каналов
   */
  IPasTxChannel findChannel();

  /**
   * Возвращает описания расширений {@link IS5BackendAddon} бекенда поддерживаемых сервером
   *
   * @return {@link IStringMap}&lt;String&gt; карта описания расширений.
   *         <p>
   *         Ключ: идентификатор расширения {@link IS5BackendAddon#id()};<br>
   *         Значение: полное имя java-класса реализующий расширение {@link IS5BackendAddon};<br>
   * @throws TsIllegalStateRtException нет связи с сервером или конфигурация не получена
   */
  IStringMap<String> backendAddonInfos();

  /**
   * Обновить топологию кластеров доступных клиенту
   *
   * @param aTopology {@link S5ClusterTopology} топология кластеров
   * @throws TsNullArgumentRtException аргумент = null
   */
  void updateClusterTopology( S5ClusterTopology aTopology );

  /**
   * Обработка события соединения: разорвано соединение с сервером
   */
  void disconnected();

}
