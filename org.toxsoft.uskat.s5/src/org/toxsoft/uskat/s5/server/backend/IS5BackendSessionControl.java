package org.toxsoft.uskat.s5.server.backend;

import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.s5.client.remote.connection.*;
import org.toxsoft.uskat.s5.common.sessions.*;

import jakarta.ejb.*;

/**
 * Локальный доступ к управлению {@link ISkBackend} предоставляемый s5-сервером
 *
 * @author mvk
 */
@Local
public interface IS5BackendSessionControl
    extends IS5Verifiable {

  /**
   * Возвращает информацию о сессии пользователя
   *
   * @return {@link IS5SessionInfo} информация о сессии
   */
  IS5SessionInfo sessionInfo();

  /**
   * Установить описание топологии кластеров доступных клиенту
   *
   * @param aClusterTopology {@link S5ClusterTopology} описание топологии
   * @throws TsNullArgumentRtException аргумент = null
   */
  void setClusterTopology( S5ClusterTopology aClusterTopology );

  /**
   * Возвращает имя узла кластера на котором работает служба
   *
   * @return String имя узла кластера на котором работает служба
   */
  String nodeName();

  /**
   * Асинхронное удаление сессии пользователя.
   * <p>
   * После вызова этого метода сессионный бин представляющий API сессии будет удален (смотри аннотацию @Remove).
   */
  void removeAsync();
}
