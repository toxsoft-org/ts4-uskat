package org.toxsoft.uskat.s5.utils.threads.impl;

import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.IS5Resources.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.common.error.S5RuntimeException;
import org.toxsoft.uskat.s5.utils.threads.IS5Thread;
import org.toxsoft.uskat.s5.utils.threads.IS5ThreadExecutor;

/**
 * Абстрактная реализация исполнителя потоков {@link IS5ThreadExecutor}
 *
 * @author mvk
 * @param <THREAD_TYPE> тип потока
 */
public abstract class S5AbstactThreadExecutor<THREAD_TYPE extends IS5Thread>
    implements IS5ThreadExecutor<THREAD_TYPE> {

  private final ThreadFactory          threadFactory;
  private final ExecutorService        executorService;
  private final IListEdit<THREAD_TYPE> threads;
  private volatile int                 completedCount;
  private Object                       completedCountSignal = new Object();
  private final ILogger                logger;
  private boolean                      closed;
  private boolean                      running;
  private boolean                      throwable;
  private volatile S5RuntimeException  error;

  /**
   * Конструктор
   *
   * @param aThreadFactory {@link ThreadFactory} фабрика java-потоков
   * @throws TsNullArgumentRtException аргумент = null
   */
  protected S5AbstactThreadExecutor( ThreadFactory aThreadFactory ) {
    TsNullArgumentRtException.checkNull( aThreadFactory );
    threadFactory = aThreadFactory;
    executorService = null;
    threads = new ElemArrayList<>();
    logger = getLogger( getClass() );
  }

  /**
   * Конструктор
   *
   * @param aThreadFactory {@link ThreadFactory} фабрика java-потоков
   * @param aLogger {@link ILogger} журнал
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  protected S5AbstactThreadExecutor( ThreadFactory aThreadFactory, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aThreadFactory, aLogger );
    threadFactory = aThreadFactory;
    executorService = null;
    threads = new ElemArrayList<>();
    logger = aLogger;
  }

  /**
   * Конструктор
   *
   * @param aExecutorService {@link ExecutorService} внешняя служба выполнения java-потоков
   * @throws TsNullArgumentRtException аргумент = null
   */
  protected S5AbstactThreadExecutor( ExecutorService aExecutorService ) {
    TsNullArgumentRtException.checkNull( aExecutorService );
    threadFactory = null;
    executorService = aExecutorService;
    threads = new ElemArrayList<>();
    logger = getLogger( getClass() );
  }

  /**
   * Конструктор
   *
   * @param aExecutorService {@link ExecutorService} внешняя служба выполнения java-потоков
   * @param aLogger {@link ILogger} журнал
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  protected S5AbstactThreadExecutor( ExecutorService aExecutorService, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aExecutorService, aLogger );
    threadFactory = null;
    executorService = aExecutorService;
    threads = new ElemArrayList<>();
    logger = aLogger;
  }

  // ------------------------------------------------------------------------------------
  // API пакета
  //
  /**
   * Поток завершил выполнение
   *
   * @param aThreadIndex int индекс потока в исполнителе
   */
  final void threadCompleted( int aThreadIndex ) {
    // Формируем сигнал для разблокирования потоков ожидающих изменения значения state
    THREAD_TYPE thread = threads.get( aThreadIndex );
    IS5Thread.EState state = thread.state();
    // Расчет времени выполнения (для информации)
    String duration = "???"; //$NON-NLS-1$
    if( thread.startTime() != TimeUtils.MAX_TIMESTAMP && thread.endTime() != TimeUtils.MAX_TIMESTAMP ) {
      duration = String.valueOf( thread.endTime() - thread.startTime() );
    }
    logger.debug( MSG_THREAD_FINISH, Integer.valueOf( aThreadIndex ), state, duration );
    if( state == IS5Thread.EState.ERROR && error == null ) {
      // При выполнении потока произошла ошибка. Запоминаем первую чтобы выдать ее при завершении работы исполнителя
      error = thread.error();
      // Требуем завершить работу всех потоков
      cancelThreads();
    }
    synchronized (completedCountSignal) {
      completedCount++;
      if( completed() || (throwable && error != null) ) {
        // Завершение работы если все потоки завершили работу или в потоке поизошла ошибка и включен режим throwable
        completedCountSignal.notify();
        // Завершаем работу менеджера
        close();
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // API наследников
  //
  /**
   * Возвращает потоки зарегистрированные в исполнителе
   *
   * @return {@link IList}&lt;THREAD_TYPE&gt; зарегистрированные потоки
   */
  protected final IList<THREAD_TYPE> threads() {
    return threads;
  }

  /**
   * Возвращает журнал исполнителя
   *
   * @return {@link ILogger} журнал исполнителя
   */
  protected final ILogger logger() {
    return logger;
  }

  // ------------------------------------------------------------------------------------
  // Методы для переопределения наследниками
  //
  /**
   * Шаблонный метод: запуск исполнителя
   */
  protected abstract void doRun();

  /**
   * Шаблонный метод: завершение работы исполнителя
   */
  protected abstract void doClose();

  // ------------------------------------------------------------------------------------
  // Реализация IS5ThreadExecutor
  //
  @Override
  public final boolean completed() {
    return completedCount == threads.size();
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public final void add( THREAD_TYPE aThread ) {
    TsNullArgumentRtException.checkNull( aThread );
    TsIllegalStateRtException.checkTrue( running );
    TsIllegalStateRtException.checkTrue( closed );
    if( !(aThread instanceof S5AbstractThread) ) {
      // Поток должен быть наследником S5AbstractThread
      throw new TsIllegalArgumentRtException();
    }
    ((S5AbstractThread<THREAD_TYPE>)aThread).setManager( this, threads.size() );
    threads.add( aThread );
  }

  @Override
  public final int threadCount() {
    return threads.size();
  }

  @Override
  public final int threadCompletedCount() {
    return completedCount;
  }

  @Override
  public final void run( boolean aWait, boolean aThrowable ) {
    TsIllegalStateRtException.checkTrue( closed );
    if( running ) {
      // Исполнитель уже работает
      return;
    }
    running = true;
    throwable = aThrowable;
    // Вызов шаблонного кода наследников
    doRun();
    // Запуск потоков на выполнение
    for( int index = 0, n = threads.size(); index < n; index++ ) {
      Runnable thread = threads.get( index );
      if( threadFactory != null ) {
        threadFactory.newThread( thread ).run();
      }
      if( executorService != null ) {
        executorService.execute( thread );
      }
    }
    if( aWait ) {
      Long rtc = Long.valueOf( threadCount() );
      // Ожидаем завершения выполнения потоков
      synchronized (completedCountSignal) {
        while( !completed() ) {
          try {
            completedCountSignal.wait( 1000 );
            logger.warning( MSG_THREADS_STATE, rtc, Long.valueOf( threadCompletedCount() ) );
          }
          catch( InterruptedException e ) {
            cancelThreads();
            logger.error( e );
          }
        }
      }
    }
    // Проверка появлении ошибки при выполнении потоков
    if( error != null ) {
      logger.error( error );
      if( aWait && throwable ) {
        throw error;
      }
    }
  }

  @Override
  public final void close() {
    if( closed ) {
      // Исполнитель уже завершил свою работу
      return;
    }
    closed = true;
    // Завершение работы потоков
    cancelThreads();
    // Проходим по всем потокам и требуем завершить выполнение
    for( int index = 0, n = threads.size(); index < n; index++ ) {
      threads.get( index ).close();
    }
    doClose();
    logger.debug( MSG_THREAD_MANAGER_FINISH );
  }

  /**
   * Посылает всем потокам сигнал об отмене выполнения
   */
  private void cancelThreads() {
    for( int index = 0, n = threads.size(); index < n; index++ ) {
      threads.get( index ).cancel();
    }
  }
}
