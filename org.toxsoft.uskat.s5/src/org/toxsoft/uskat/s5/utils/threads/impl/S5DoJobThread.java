package org.toxsoft.uskat.s5.utils.threads.impl;

import java.util.concurrent.*;

import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.s5.utils.jobs.*;
import org.toxsoft.uskat.s5.utils.threads.*;

/**
 * Абстрактная реализация потока записи данных s5-платформы
 *
 * @author mvk
 */
public class S5DoJobThread
    extends S5AbstractThread<IS5DoJobThread>
    implements IS5DoJobThread {

  private final String       name;
  private final IS5ServerJob job;
  private final long         doJobStartTimeout;
  private final long         doJobTimeout;

  /**
   * Конструктор
   *
   * @param aName String имя потока
   * @param aExecutor {@link Executor} исполнитель в потоке
   * @param aJob {@link IS5ServerJob} фоновая задача сервера
   * @param aJobTimeout long таймаут (мсек) между вызывами задачи
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException недопустимый таймаут (aJobTimeout <= 0)
   */
  public S5DoJobThread( String aName, Executor aExecutor, IS5ServerJob aJob, long aJobTimeout ) {
    this( aName, aExecutor, aJob, -1, aJobTimeout );
  }

  /**
   * Конструктор
   *
   * @param aName String имя потока
   * @param aExecutor {@link Executor} исполнитель в потоке
   * @param aJob {@link IS5ServerJob} фоновая задача сервера
   * @param aJobTimeout long таймаут (мсек) между вызывами задачи
   * @param aLogger {@link ILogger} журнал потока
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException недопустимый таймаут (aJobTimeout <= 0)
   */
  public S5DoJobThread( String aName, Executor aExecutor, IS5ServerJob aJob, long aJobTimeout,
      ILogger aLogger ) {
    this( aName, aExecutor, aJob, -1, aJobTimeout, aLogger );
  }

  /**
   * Конструктор
   *
   * @param aName String имя потока
   * @param aExecutor {@link ExecutorService} служба управления потоками
   * @param aJob {@link IS5ServerJob} фоновая задача сервера
   * @param aStartJobTimeout long таймаут (мсек) перед первым вызовом задачи. < 0: не используется
   * @param aJobTimeout long таймаут (мсек) между вызывами задачи
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException недопустимый таймаут (aJobTimeout <= 0)
   */
  public S5DoJobThread( String aName, Executor aExecutor, IS5ServerJob aJob, long aStartJobTimeout, long aJobTimeout ) {
    TsNullArgumentRtException.checkNulls( aName, aExecutor, aJob );
    TsIllegalArgumentRtException.checkTrue( aJobTimeout <= 0 );
    name = aName;
    job = aJob;
    doJobStartTimeout = aStartJobTimeout;
    doJobTimeout = aJobTimeout;
    aExecutor.execute( this );
  }

  /**
   * Конструктор
   *
   * @param aName String имя потока
   * @param aExecutor {@link Executor} исполнитель в потоке
   * @param aJob {@link IS5ServerJob} фоновая задача сервера
   * @param aStartJobTimeout long таймаут (мсек) перед первым вызовом задачи. < 0: не используется
   * @param aJobTimeout long таймаут (мсек) между вызывами задачи
   * @param aLogger {@link ILogger} журнал потока
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException недопустимый таймаут (aJobTimeout <= 0)
   */
  public S5DoJobThread( String aName, Executor aExecutor, IS5ServerJob aJob, long aStartJobTimeout, long aJobTimeout,
      ILogger aLogger ) {
    super( aLogger );
    TsNullArgumentRtException.checkNulls( aName, aExecutor, aJob );
    TsIllegalArgumentRtException.checkTrue( aJobTimeout <= 0 );
    name = aName;
    job = aJob;
    doJobStartTimeout = aStartJobTimeout;
    doJobTimeout = aJobTimeout;
    aExecutor.execute( this );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5DoJobThread
  //
  @Override
  public long doJobTimeout() {
    return doJobTimeout;
  }

  // ------------------------------------------------------------------------------------
  // Реализация S5AbstractThread
  //
  @Override
  protected void doRun() {
    Thread.currentThread().setName( "Thread doJob " + name ); //$NON-NLS-1$
    if( doJobStartTimeout > 0 ) {
      // Отработка таймаута первого запуска задачи
      try {
        Thread.sleep( doJobStartTimeout );
      }
      catch( Throwable e ) {
        logger().error( e );
      }
    }
    // Подготовка к работе
    job.doJobPrepare();
    while( !completed() && !job.completed() ) {
      try {
        job.doJob();
        Thread.sleep( doJobTimeout );
      }
      catch( Throwable e ) {
        logger().error( e );
      }
    }
  }

  @Override
  protected void doCancel() {
    job.close();
  }

}
