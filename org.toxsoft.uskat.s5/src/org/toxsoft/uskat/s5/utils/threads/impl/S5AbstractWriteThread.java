package org.toxsoft.uskat.s5.utils.threads.impl;

import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.utils.threads.IS5WriteThread;

/**
 * Абстрактная реализация потока записи данных s5-платформы
 *
 * @author mvk
 */
public abstract class S5AbstractWriteThread
    extends S5AbstractThread<IS5WriteThread>
    implements IS5WriteThread {

  /**
   * Конструктор
   */
  protected S5AbstractWriteThread() {
  }

  /**
   * Конструктор
   *
   * @param aLogger {@link ILogger} журнал потока
   * @throws TsNullArgumentRtException аргумент = null
   */
  protected S5AbstractWriteThread( ILogger aLogger ) {
    super( aLogger );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5WriteThread
  //
}
