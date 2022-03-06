package org.toxsoft.uskat.s5.server.backend;

/**
 * Интерфейс модуля с возможностью проверки работоспособности
 *
 * @author mvk
 */
public interface IS5Verifiable {

  /**
   * Выполнить проверку работоспособности модуля
   *
   * @throws RuntimeException любая ошибка проверки работоспособности
   */
  void verify();
}
