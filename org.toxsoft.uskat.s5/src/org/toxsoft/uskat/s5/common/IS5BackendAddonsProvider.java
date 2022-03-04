package org.toxsoft.uskat.s5.common;

import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddon;

/**
 * Поставщик расширений бекенда необходимых для работы клиента
 *
 * @author mvk
 */
public interface IS5BackendAddonsProvider {

  /**
   * Возвращает список расширений бекенда предоставляемых реализацией сервера
   *
   * @return {@link IStridablesList}&lt;{@link IS5BackendAddon}&gt; список расширений бекенда
   */
  IStridablesList<IS5BackendAddon> addons();
}
