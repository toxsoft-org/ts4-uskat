package org.toxsoft.uskat.s5.utils.threads.impl;

import static org.toxsoft.core.log4j.Logger.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.IS5Resources.*;

import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.common.error.S5RuntimeException;
import org.toxsoft.uskat.s5.utils.threads.IS5Thread;
import org.toxsoft.uskat.s5.utils.threads.IS5ThreadExecutor;

/**
 * Абстрактная реализация потока s5-платформы
 *
 * @author mvk
 * @param <THREAD_TYPE> тип потока
 */
public abstract class S5AbstractThread<THREAD_TYPE extends IS5Thread>
    implements IS5Thread {

  private S5AbstactThreadExecutor<THREAD_TYPE> manager;
  private int                                  threadIndex;
  private volatile EState                      state = EState.WAIT;
  private volatile S5RuntimeException          error;
  private ILogger                              logger;

  private long startTime = TimeUtils.MAX_TIMESTAMP;
  private long endTime   = TimeUtils.MAX_TIMESTAMP;

  /**
   * Конструктор
   */
  protected S5AbstractThread() {
  }

  /**
   * Конструктор
   *
   * @param aLogger {@link ILogger} журнал потока
   * @throws TsNullArgumentRtException аргумент = null
   */
  protected S5AbstractThread( ILogger aLogger ) {
    TsNullArgumentRtException.checkNull( aLogger );
    logger = aLogger;
  }

  // ------------------------------------------------------------------------------------
  // API пакета
  //
  /**
   * Инициализация менеджера
   *
   * @param aManager {@link IS5ThreadExecutor} исполнитель потоков
   * @param aThreadIndex индекс потока в менеджере
   * @throws TsNullArgumentRtException аргумент = null
   */
  void setManager( S5AbstactThreadExecutor<THREAD_TYPE> aManager, int aThreadIndex ) {
    TsNullArgumentRtException.checkNull( aManager );
    manager = aManager;
    threadIndex = aThreadIndex;
  }

  // ------------------------------------------------------------------------------------
  // API для наследников
  //
  /**
   * Индекс потока в исполнителе
   *
   * @return int индекс потока в менеджере
   */
  protected final int threadIndex() {
    return threadIndex;
  }

  /**
   * Возвращает журнал потока
   *
   * @return {@link ILogger} журнал потока
   */
  protected final ILogger logger() {
    if( logger == null ) {
      logger = manager != null ? manager.logger() : getLogger( getClass() );
    }
    return logger;
  }

  // ------------------------------------------------------------------------------------
  // Методы для переопределения наследниками
  //
  /**
   * Шаблонный метод реализации потока
   */
  protected abstract void doRun();

  /**
   * Шаблонный метод обработки запроса отмены выполнения потока
   */
  protected abstract void doCancel();

  // ------------------------------------------------------------------------------------
  // Реализация IS5Thread
  //
  @Override
  public final long startTime() {
    return startTime;
  }

  @Override
  public final long endTime() {
    return endTime;
  }

  @Override
  public final void run() {
    // TODO: mvkd: 2018-04-10: профилирование показало, что Thread.setName является тяжелым вызовом (30%)
    // Thread.currentThread().setName( format( THREAD_ID_FORMAT, this, Integer.valueOf( threadIndex ) ) );
    startTime = System.currentTimeMillis();
    try {
      EState prevState = trySetState( EState.RUNNING );
      if( prevState != EState.WAIT ) {
        // Запуск потока уже не актуален
        return;
      }
      try {
        doRun();
      }
      catch( Throwable e ) {
        prevState = trySetState( EState.ERROR );
        if( prevState == EState.RUNNING ) {
          error = new S5RuntimeException( e, ERR_THREAD_RUN, cause( e ) );
        }
        return;
      }
      trySetState( EState.COMPLETED );
    }
    finally {
      endTime = System.currentTimeMillis();
      if( manager != null ) {
        manager.threadCompleted( threadIndex );
        manager = null;
      }
    }
  }

  @Override
  public void cancel() {
    trySetState( EState.CANCELED );
    try {
      doCancel();
    }
    catch( Throwable e ) {
      error = new S5RuntimeException( e, ERR_THREAD_CANCEL, cause( e ) );
    }
  }

  @Override
  public final void close() {
    cancel();
  }

  @Override
  public final EState state() {
    synchronized (state) {
      return state;
    }
  }

  @Override
  public final S5RuntimeException error() {
    TsIllegalArgumentRtException.checkFalse( state() == EState.ERROR );
    return error;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Делает попытку установить новое состояние потока
   *
   * @param aNewState {@link org.toxsoft.uskat.s5.utils.threads.IS5Thread.EState} новое состояние потока
   * @return {@link org.toxsoft.uskat.s5.utils.threads.IS5Thread.EState} предыдущее состояние потока.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private EState trySetState( EState aNewState ) {
    TsNullArgumentRtException.checkNull( aNewState );
    synchronized (state) {
      EState prevState = state;
      switch( prevState ) {
        case COMPLETED:
        case ERROR:
        case CANCELED:
          // Нельзя изменить состояние (поток уже завершен)
          return prevState;
        case RUNNING:
          if( aNewState == EState.WAIT || aNewState == EState.RUNNING ) {
            // Нельзя изменить состояние (поток уже запущен)
            return prevState;
          }
          break;
        case WAIT:
          break;
        default:
          throw new TsNotAllEnumsUsedRtException();
      }
      state = aNewState;
      return prevState;
    }
  }

}
