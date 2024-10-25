package org.toxsoft.uskat.s5.utils.threads.impl;

import java.util.concurrent.*;

import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.s5.utils.threads.*;

/**
 * Реализация исполнителя потоков записи данных в s5-платформу по умолчанию {@link IS5ReadThreadExecutor}
 *
 * @author mvk
 */
public class S5WriteThreadExecutor
    extends S5AbstactThreadExecutor<IS5WriteThread>
    implements IS5WriteThreadExecutor {

  /**
   * Конструктор
   *
   * @param aThreadFactory {@link ThreadFactory} фабрика java-потоков
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5WriteThreadExecutor( ThreadFactory aThreadFactory ) {
    super( aThreadFactory );
  }

  /**
   * Конструктор
   *
   * @param aThreadFactory {@link ThreadFactory} фабрика java-потоков
   * @param aLogger {@link ILogger} журнал
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5WriteThreadExecutor( ThreadFactory aThreadFactory, ILogger aLogger ) {
    super( aThreadFactory, aLogger );
  }

  /**
   * Конструктор
   *
   * @param aExecutor {@link ExecutorService} внешняя служба выполнения java-потоков
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5WriteThreadExecutor( Executor aExecutor ) {
    super( aExecutor );
  }

  /**
   * Конструктор
   *
   * @param aExecutor {@link ExecutorService} внешняя служба выполнения java-потоков
   * @param aLogger {@link ILogger} журнал
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5WriteThreadExecutor( Executor aExecutor, ILogger aLogger ) {
    super( aExecutor, aLogger );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов
  //
  @Override
  protected void doRun() {
    // nop
  }

  @Override
  protected void doClose() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5ServerWriteThreadManager
  //

}
