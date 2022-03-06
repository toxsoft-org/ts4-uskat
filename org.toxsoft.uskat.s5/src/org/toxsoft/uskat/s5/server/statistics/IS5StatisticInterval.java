package org.toxsoft.uskat.s5.server.statistics;

import org.toxsoft.core.tslib.bricks.strid.IStridable;

/**
 * Описание интервала обработки статистики
 *
 * @author mvk
 */
public interface IS5StatisticInterval
    extends IStridable {

  /**
   * Возвращает время интервала в мсек.
   *
   * @return long время интервала. <= 0 - за весь период работы
   */
  int milli();
}
