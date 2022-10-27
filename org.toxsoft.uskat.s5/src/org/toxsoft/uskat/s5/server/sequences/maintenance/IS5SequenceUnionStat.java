package org.toxsoft.uskat.s5.server.sequences.maintenance;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceFragmentInfo;

/**
 * Статистика выполнения процесса дефрагментации хранимых данных
 *
 * @author mvk
 */
public interface IS5SequenceUnionStat {

  /**
   * Возвращает список описаний данных поступивших в запрос на дефрагментацию
   *
   * @return {@link IList}&lt;{@link IS5SequenceFragmentInfo}&gt; список описаний фрагментированных данных
   */
  IList<IS5SequenceFragmentInfo> infoes();

  /**
   * Возвращает общее количество проанализированных последовательностей при поиске дефрагментации
   *
   * @return int количество последовательностей
   */
  int lookupCount();

  /**
   * Возвращает количество блоков обновленных в dbms
   *
   * @return int количество блоков
   */
  int dbmsMergedCount();

  /**
   * Возвращает количество блоков удаленных из dbms
   *
   * @return int количество блоков
   */
  int dbmsRemovedCount();

  /**
   * Возвращает общее количество обработанных значений
   *
   * @return int количество обработанных значений блоков
   */
  int valueCount();

  /**
   * Возвращает общее количество ошибок возникших при дефрагментации
   *
   * @return общее количество ошибок дефрагментации блоков
   */
  int errorCount();

  /**
   * Возвращает текущий размер очереди на дефрагментацию
   *
   * @return int количество данных в очереди
   */
  int queueSize();
}
