package org.toxsoft.uskat.s5.utils.threads.impl;

import static org.toxsoft.uskat.s5.utils.threads.impl.IS5Resources.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.utils.threads.IS5ReadThread;
import org.toxsoft.uskat.s5.utils.threads.IS5ReadThreadExecutor;

/**
 * Реализация исполнителя потоков чтения данных из s5-платформы по умолчанию {@link IS5ReadThreadExecutor}
 *
 * @author mvk
 * @param <THREAD_RESULT> тип результата(прочитанные данные) выполнения одного потока
 */
public class S5ReadThreadExecutor<THREAD_RESULT>
    extends S5AbstactThreadExecutor<IS5ReadThread<THREAD_RESULT>>
    implements IS5ReadThreadExecutor<THREAD_RESULT> {

  private IListEdit<THREAD_RESULT> results;

  /**
   * Конструктор
   *
   * @param aThreadFactory {@link ThreadFactory} фабрика java-потоков
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5ReadThreadExecutor( ThreadFactory aThreadFactory ) {
    super( aThreadFactory );
  }

  /**
   * Конструктор
   *
   * @param aThreadFactory {@link ThreadFactory} фабрика java-потоков
   * @param aLogger {@link ILogger} журнал
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5ReadThreadExecutor( ThreadFactory aThreadFactory, ILogger aLogger ) {
    super( aThreadFactory, aLogger );
  }

  /**
   * Конструктор
   *
   * @param aExecutorService {@link ExecutorService} внешняя служба выполнения java-потоков
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5ReadThreadExecutor( ExecutorService aExecutorService ) {
    super( aExecutorService );
  }

  /**
   * Конструктор
   *
   * @param aExecutorService {@link ExecutorService} внешняя служба выполнения java-потоков
   * @param aLogger {@link ILogger} журнал
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException недопустимое количество потоков которые должны быть выполнены
   */
  public S5ReadThreadExecutor( ExecutorService aExecutorService, ILogger aLogger ) {
    super( aExecutorService, aLogger );
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
  // Реализация IS5ReadThreadExecutor
  //
  @Override
  public IList<THREAD_RESULT> results() {
    if( results == null ) {
      // Результаты еще не подготовлены
      IList<IS5ReadThread<THREAD_RESULT>> threads = threads();
      results = new ElemArrayList<>( threads.size() );
      for( int index = 0, n = threads.size(); index < n; index++ ) {
        IS5ReadThread<THREAD_RESULT> thread = threads.get( index );
        switch( thread.state() ) {
          case WAIT:
          case RUNNING:
            // Потоки еще не завершены
            throw new TsIllegalStateRtException( ERR_THREADS_RUNNING );
          case COMPLETED:
            results.add( thread.result() );
            continue;
          case ERROR:
            // Ошибка при выполнении потока
            throw thread.error();
          case CANCELED:
            // Выполнение потоков было отменено
            throw new TsIllegalStateRtException( ERR_THREADS_CANCELED );
          default:
            throw new TsNotAllEnumsUsedRtException();
        }
      }
    }
    return results;
  }

}
