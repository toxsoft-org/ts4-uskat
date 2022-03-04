package org.toxsoft.uskat.s5.utils.threads;

import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.uskat.s5.common.error.S5RuntimeException;

/**
 * Поток выполняемый в рамках платформы s5
 * <p>
 * Интерфейс расширяет {@link ICloseable}. Следует учитывать, что вызов {@link #close()} блокирует поток выполнения до
 * завершения работы целевого потока.
 *
 * @author mvk
 */
public interface IS5Thread
    extends Runnable, ICloseable {

  /**
   * Возвращает время (мсек с начала эпохи) запуска потока
   *
   * @return long время запуска: {@link TimeUtils#MAX_TIMESTAMP}: поток не был запущен
   */
  long startTime();

  /**
   * Возвращает время (мсек с начала эпохи) завершения потока (штатное или по ошибке)
   *
   * @return long время запуска: {@link TimeUtils#MAX_TIMESTAMP}: поток не был запущен или еще работает
   */
  long endTime();

  /**
   * Состояние выполнения потока
   */
  enum EState {
    /**
     * Поток ожидает выполнения.
     */
    WAIT,

    /**
     * Нормальное завершение
     */
    COMPLETED,

    /**
     * Завершение потока по ошибке
     */
    ERROR,

    /**
     * Выполнение потока
     */
    RUNNING,

    /**
     * Поток в состоянии отмены выполнения.
     */
    CANCELED,
  }

  /**
   * Возвращает текущее состояние потока
   *
   * @return {@link EState} состояние потока.
   */
  EState state();

  /**
   * Возвращает признак того что поток завершил свою работу
   *
   * @return boolean <b>true</b> поток завершил свою работу; <b>false</b> поток выполняется или ожидает выполнение.
   */
  default boolean completed() {
    EState state = state();
    return (state != IS5Thread.EState.WAIT && state != IS5Thread.EState.RUNNING);
  }

  /**
   * Производит попытку отмены выполнения потока
   */
  void cancel();

  /**
   * Возвращает ошибку которая завершила выполнение потока
   *
   * @return {@link S5RuntimeException} ошибка выполнения потока
   * @throws TsIllegalArgumentRtException поток не завершил свое выполнение ошибкой (смотри {@link EState})
   */
  S5RuntimeException error();

}
