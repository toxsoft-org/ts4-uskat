package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable;

/**
 * Синхронизация доступа к разделяемому ресурсу (декоратор)
 *
 * @author mvk
 * @param <T> тип ресурса
 */
abstract class S5SynchronizedResource<T> {

  private T          target;
  private S5Lockable lock;

  /**
   * Конструктор
   *
   * @param aTarget T защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedResource( T aTarget, ReentrantReadWriteLock aLock ) {
    TsNullArgumentRtException.checkNulls( aTarget, aLock );
    target = aTarget;
    lock = S5Lockable.getLockableFromPool( aLock );
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Возвращает защищаемый ресурс
   *
   * @return T ресурс
   */
  public final T target() {
    return target;
  }

  /**
   * Замена защищаемого ресурса
   *
   * @param aTarget T ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException недопустимый тип ресурса
   */
  @SuppressWarnings( "unchecked" )
  public final void changeTarget( Object aTarget, ReentrantReadWriteLock aLock ) {
    TsNullArgumentRtException.checkNulls( aTarget, aLock );
    if( aTarget.equals( target ) ) {
      // Ничего не изменилось
      return;
    }
    // Переключение на новое соединение проходит под блокировкой старого соединения
    T prevTarget = target;
    S5Lockable oldLock = lock;
    S5Lockable.lockWrite( oldLock );
    try {
      try {
        target = (T)aTarget;
        lock = S5Lockable.getLockableFromPool( aLock );
      }
      catch( Exception ex ) {
        // Восстановление состояния
        target = prevTarget;
        throw new TsIllegalArgumentRtException( ex, ex.getMessage() );
      }
    }
    finally {
      S5Lockable.unlockWrite( oldLock );
    }
    // Оповещение наследика проходит под блокировкой нового соединения
    S5Lockable.lockWrite( lock );
    try {
      doChangeTarget( prevTarget, (T)aTarget, aLock );
    }
    finally {
      S5Lockable.unlockWrite( lock );
    }
  }

  /**
   * Выполнить необходимые действия после замены защищаемого ресурса
   *
   * @param aPrevTarget T предыдущий защищаемый ресурс
   * @param aNewTarget T новый защищаемый ресурс
   * @param aNewLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   */
  protected abstract void doChangeTarget( T aPrevTarget, T aNewTarget, ReentrantReadWriteLock aNewLock );

  /**
   * Делает попытку захвата блокировки
   *
   * @param aResource {@link S5SynchronizedResource} блокируемый ресурс
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException ошибка получения блокировки
   * @throws TsIllegalStateRtException ожидание блокировки было прервано вызовом {@link Thread#interrupt()}
   */
  protected static void lockWrite( S5SynchronizedResource<?> aResource ) {
    TsNullArgumentRtException.checkNull( aResource );
    S5Lockable.lockWrite( aResource.lock );
  }

  /**
   * Освобождает блокировку ресурса
   *
   * @param aResource {@link S5SynchronizedResource} блокируемый ресурс
   * @throws TsNullArgumentRtException аргумент = null
   */
  protected static void unlockWrite( S5SynchronizedResource<?> aResource ) {
    TsNullArgumentRtException.checkNull( aResource );
    S5Lockable.unlockWrite( aResource.lock );
  }

  /**
   * Возвращает блокировку используемую для защиты ресурса
   *
   * @return {@link ReentrantReadWriteLock} блокировка
   */
  protected final ReentrantReadWriteLock lock() {
    return S5Lockable.nativeLock( lock );
  }
}
