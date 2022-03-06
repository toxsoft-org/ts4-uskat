package org.toxsoft.uskat.s5.server.sequences.writer;

import org.toxsoft.core.tslib.bricks.time.ITimeInterval;

/**
 * Статистика выполнения процесса записи последовательности блоков
 *
 * @author mvk
 */
public interface IS5SequenceWriteStat {

  /**
   * Возвращает время создания статистики
   *
   * @return long время мсек (мсек с начала эпохи)
   */
  long createTime();

  /**
   * Возвращает время начала процесса записи
   *
   * @return long время мсек (мсек с начала эпохи)
   */
  long startTime();

  /**
   * Возвращает время завершения записи (НЕ транзакции!)
   *
   * @return long время мсек (мсек с начала эпохи)
   */
  long endTime();

  /**
   * Возвращает количество записываемых данных
   *
   * @return int количество записываемых данных
   */
  int dataCount();

  /**
   * Возвращает общее количество записанных блоков в последовательность
   *
   * @return int количество блоков
   */
  int writedBlockCount();

  /**
   * Возвращает совокупный интервал записанных блоков
   *
   * @return {@link ITimeInterval} интервал
   */
  ITimeInterval writedBlockInterval();

  /**
   * Возвращает статистику ввода/вывода dbms
   *
   * @return {@link IS5DbmsStatistics} статистика ввода/вывода dbms
   */
  IS5DbmsStatistics dbmsStatistics();

}
