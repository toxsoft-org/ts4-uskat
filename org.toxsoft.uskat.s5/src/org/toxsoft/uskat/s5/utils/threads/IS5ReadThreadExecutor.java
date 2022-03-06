package org.toxsoft.uskat.s5.utils.threads;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;
import org.toxsoft.uskat.s5.common.error.S5RuntimeException;

/**
 * Исполнитель потоков чтения данных s5-платформы {@link IS5ReadThread}
 *
 * @author mvk
 * @param <THREAD_RESULT> тип результата(прочитанные данные) выполнения одного потока
 */
public interface IS5ReadThreadExecutor<THREAD_RESULT>
    extends IS5ThreadExecutor<IS5ReadThread<THREAD_RESULT>> {

  /**
   * Возвращает результаты выполнения потоков
   *
   * @return {@link IList}&lt;THREAD_RESULT&gt; список результатов выполнения потоков
   * @throws TsIllegalStateRtException потоки еще не сформировали результат или произошла отмена их выполнения
   * @throws S5RuntimeException ошибка выполнения потоков
   */
  IList<THREAD_RESULT> results();
}
