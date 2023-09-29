package org.toxsoft.uskat.s5.server.sequences.maintenance;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceRemoveInfo;

/**
 * Статистика выполнения процесса дефрагментации хранимых данных
 *
 * @author mvk
 */
public interface IS5SequenceRemoveStat {

  /**
   * Возвращает список описаний данных поступивших в запрос на удаление
   *
   * @return {@link IList}&lt;{@link IS5SequenceRemoveInfo}&gt; список описаний удаляемых данных
   */
  IList<IS5SequenceRemoveInfo> infoes();

  /**
   * Возвращает общее количество проанализированных последовательностей при поиске значений для удаления
   *
   * @return int количество последовательностей
   */
  int lookupCount();

  /**
   * Возвращает количество разделов таблиц удаленных из dbms
   *
   * @return int количество разделов
   */
  int removedCount();

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
