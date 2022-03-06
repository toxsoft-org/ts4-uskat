package org.toxsoft.uskat.s5.utils.threads;

import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;

/**
 * Поток проводящий чтение данных из s5-платформы
 *
 * @author mvk
 * @param <THREAD_RESULT> тип результата(прочитанные данные) выполнения потока
 */
public interface IS5ReadThread<THREAD_RESULT>
    extends IS5Thread {

  /**
   * Возвращает результат выполнения потока
   * <p>
   * Внимание: реализация потока обязанна ВСЕГДА (если поток завершился без ошибки) возвращать не-null значение.
   *
   * @return T прочитанные данные
   * @throws TsIllegalStateRtException поток еще не завершил свое выполнение
   */
  THREAD_RESULT result();
}
