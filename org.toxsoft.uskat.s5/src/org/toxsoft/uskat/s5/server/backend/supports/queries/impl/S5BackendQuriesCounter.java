package org.toxsoft.uskat.s5.server.backend.supports.queries.impl;

/**
 * Счетчик чего-либо (например, сформированных агрегированных значений)
 *
 * @author mvk
 */
class S5BackendQuriesCounter {

  private volatile int counter;

  /**
   * Добавляет к счетчику указанного количество
   *
   * @param aCount int значение на которое должен быть увеличен счетчик
   */
  void add( int aCount ) {
    counter += aCount;
  }

  /**
   * Возвращает текущее значение счетчика
   *
   * @return int текущее значение счетчика
   */
  int current() {
    return counter;
  }
}
