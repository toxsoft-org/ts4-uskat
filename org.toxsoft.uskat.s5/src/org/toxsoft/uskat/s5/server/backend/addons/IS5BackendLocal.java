package org.toxsoft.uskat.s5.server.backend.addons;

import org.toxsoft.uskat.s5.server.backend.supports.core.*;

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

  /**
   * Возвращает имя модуля создавшего локальное соединение.
   *
   * @return String имя модуля
   */
  String localModule();

  /**
   * Возвращает узел на котором было создано локальное соединение.
   *
   * @return String имя узла
   */
  String localNode();

}
