package org.toxsoft.uskat.s5.utils.threads.impl;

import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.utils.threads.IS5ReadThread;

/**
 * Абстрактная реализация потока чтения данных s5-платформы
 *
 * @author mvk
 * @param <THREAD_RESULT> тип результата(прочитанные данные) выполнения потока
 */
public abstract class S5AbstractReadThread<THREAD_RESULT>
    extends S5AbstractThread<IS5ReadThread<THREAD_RESULT>>
    implements IS5ReadThread<THREAD_RESULT> {

  /**
   * Конструктор
   */
  protected S5AbstractReadThread() {
  }

  /**
   * Конструктор
   *
   * @param aLogger {@link ILogger} журнал потока
   * @throws TsNullArgumentRtException аргумент = null
   */
  protected S5AbstractReadThread( ILogger aLogger ) {
    super( aLogger );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5ReadThread
  //
}
