package org.toxsoft.uskat.s5.server.sequences.maintenance;

/**
 * Статистика выполнения процесса проверки блоков блоков
 *
 * @author mvk
 */
public interface IS5SequenceValidationStat {

  /**
   * Возвращает общее количество обработанных данных
   *
   * @return int количество обработанных данных
   */
  int infoCount();

  /**
   * Возвращает общее количество обработанных блоков
   *
   * @return int количество обработанных блоков
   */
  int processedCount();

  /**
   * Возвращает количество предупреждений
   *
   * @return int количество предупреждений
   */
  int warnCount();

  /**
   * Возвращает количество ошибок
   *
   * @return int количество ошибок
   */
  int errCount();

  /**
   * Возвращает общее количество блоков обновленных в dbms
   *
   * @return int количество блоков
   */
  int dbmsMergedCount();

  /**
   * Возвращает общее количество блоков удаленных из dbms
   *
   * @return int количество удаленных блоков
   */
  int dbmsRemovedCount();

  /**
   * Возвращает общее количество значений в блоках
   *
   * @return int количество значений в блоках
   */
  int valuesCount();

  /**
   * Возвращает общее количество блоков с неэффективным хранением
   *
   * @return int количество блоков
   */
  int nonOptimalCount();

}
