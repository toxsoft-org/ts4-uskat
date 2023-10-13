package org.toxsoft.uskat.s5.server.sequences.maintenance;

import org.toxsoft.core.tslib.coll.IList;

/**
 * Статистика выполнения процесса выполнения операций над разделами таблиц хранимых данных
 *
 * @author mvk
 */
public interface IS5SequencePartitionStat {

  /**
   * Возвращает список описаний операций над разделами таблиц
   *
   * @return {@link IList}&lt;{@link S5PartitionOperation}&gt; список описаний операций над разделами таблиц
   */
  IList<S5PartitionOperation> operations();

  /**
   * Возвращает общее количество проанализированных последовательностей при поиске значений для удаления
   *
   * @return int количество последовательностей
   */
  int lookupCount();

  /**
   * Возвращает количество добавленных разделов таблиц
   *
   * @return int количество разделов
   */
  int addedCount();

  /**
   * Возвращает количество разделов таблиц удаленных из dbms
   *
   * @return int количество разделов
   */
  int removedPartitionCount();

  /**
   * Возвращает количество блоков удаленных из dbms
   *
   * @return int количество блоков
   */
  int removedBlockCount();

  /**
   * Возвращает общее количество ошибок возникших при дефрагментации
   *
   * @return общее количество ошибок дефрагментации блоков
   */
  int errorCount();

  /**
   * Возвращает текущий размер очереди на удаление данных
   *
   * @return int количество данных в очереди
   */
  int queueSize();
}
