package org.toxsoft.uskat.s5.server.sequences.impl;

import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.IS5Resources.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;

import javax.ejb.ConcurrentAccessTimeoutException;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.server.sequences.IS5BackendSequenceSupportSingleton;
import org.toxsoft.uskat.s5.utils.threads.impl.S5AbstractWriteThread;
import org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable;

/**
 * Поток асинхронной задачи дефрагментации блоков последовательностей данных
 *
 * @author mvk
 */
final class S5SequenceUniterThread
    extends S5AbstractWriteThread {

  private final IS5BackendSequenceSupportSingleton<?, ?> service;
  private final String                                   author;

  private final S5Lockable workingLock   = new S5Lockable();
  private final Object     workingSingal = new Object();
  private boolean          working;

  /**
   * Создание асинхронной задачи проверки блоков последовательности данных
   *
   * @param aService {@link IS5BackendSequenceSupportSingleton} сиглетон службы хранимых данных
   * @param aAuthor String автор задачи
   * @param aLogger {@link ILogger} журнал
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  S5SequenceUniterThread( IS5BackendSequenceSupportSingleton<?, ?> aService, String aAuthor, ILogger aLogger ) {
    super( aLogger );
    TsNullArgumentRtException.checkNulls( aService, aAuthor );
    service = aService;
    author = aAuthor;
  }

  // ------------------------------------------------------------------------------------
  // Методы пакета
  //
  /**
   * Производит попытку запуска потока
   *
   * @return boolean <b>true</b> поток запущен; <b>false</b> поток не запущен, так как работает другой поток
   */
  boolean tryStart() {
    lockWrite( workingLock );
    try {
      if( working || completed() ) {
        return false;
      }
      working = true;
      // Формируем сигнал для разблокирования потока
      synchronized (workingSingal) {
        workingSingal.notifyAll();
      }
      return true;
    }
    finally {
      unlockWrite( workingLock );
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов S5AbstractThread
  //
  @Override
  protected void doRun() {
    while( !completed() ) {
      try {
        synchronized (workingSingal) {
          workingSingal.wait();
        }
      }
      catch( InterruptedException e ) {
        logger().error( e );
        break;
      }
      if( working ) {
        try {
          IOptionSet unionOptions = service.configuration();
          service.union( author, unionOptions );
        }
        catch( ConcurrentAccessTimeoutException e ) {
          // Ошибка доступа к модулю проводящему дефрагментацию:
          logger().warning( ERR_ASYNC_UNION_THREAD_BUSY, author, service.getClass().getSimpleName(), cause( e ) );
        }
        catch( Throwable e ) {
          // Ошибка выполнения потока дефрагментации блоков последовательностей
          logger().error( e, ERR_ASYNC_UNION_THREAD, author, cause( e ) );
        }
      }
      lockWrite( workingLock );
      try {
        working = false;
      }
      finally {
        unlockWrite( workingLock );
      }
    }
  }

  @Override
  protected void doCancel() {
    lockWrite( workingLock );
    try {
      working = false;
      // Формируем сигнал для разблокирования потока
      synchronized (workingSingal) {
        workingSingal.notifyAll();
      }
    }
    finally {
      unlockWrite( workingLock );
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренняя реализация
  //

}
