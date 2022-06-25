package org.toxsoft.uskat.s5.server.backend.addons;

import org.toxsoft.uskat.s5.server.backend.IS5BackendCoreSingleton;

/**
 * Локальный доступ к серверу
 *
 * @author mvk
 */
public interface IS5BackendLocal
    extends IS5Backend {

  /**
   * Возвращает синглетон доступа к расширениям бекенда
   *
   * @return {@link IS5BackendCoreSingleton} синглетон доступа к расширениям бекенда
   */
  IS5BackendCoreSingleton backendSingleton();

}
