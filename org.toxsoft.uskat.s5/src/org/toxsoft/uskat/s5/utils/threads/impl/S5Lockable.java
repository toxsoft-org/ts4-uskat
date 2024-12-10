package org.toxsoft.uskat.s5.utils.threads.impl;

import static org.toxsoft.core.log4j.LoggerWrapper.*;

import java.util.concurrent.locks.*;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.s5.server.*;

/**
 * Базовая реализация блокируемого ресурса
 *
 * @author mvk
 */
public class S5Lockable {

  /**
   * Включена отладка системы. Требуется трассировка вызовов
   */
  public static boolean LOCK_TRACING = true;

  /**
   * Идентификатор трассируемого (вызовы tryLockXxx/unlockXxx) управления блокировки. Пустая строка: отключена
   * <p>
   * Пример: "S5LockControl[126]"
   */
  public static String LOCK_TRACING_ID = TsLibUtils.EMPTY_STRING;

  /**
   * Управление блокировкой
   */
  private final S5LockControl control;

  /**
   * Таймаут (мсек) попытки захвата блокировки по умолчанию
   */
  private static long accessTimeoutDefault = IS5ImplementConstants.ACCESS_TIMEOUT_DEFAULT;

  /**
   * Карта блокировок
   * <p>
   * Ключ: блокировка<br>
   * Значение: управление блокировкой
   */
  private static IMapEdit<ReentrantReadWriteLock, S5Lockable> lockables = new ElemMap<>();

  /**
   * Блокировка доступа к {@link #lockables}
   */
  private static S5Lockable lockablesLock = new S5Lockable();

  /**
   * Журнал работы
   */
  private static final ILogger logger = getLogger( "S5Lockable" ); //$NON-NLS-1$

  static {
    // Запрет трассировки блокировок
    String tracing = System.getProperty( "s5.lock.tracing" ); //$NON-NLS-1$
    LOCK_TRACING = (tracing != null ? Boolean.parseBoolean( tracing ) : false);
    // Идентификатор трассируемой блокировки
    String tracingId = System.getProperty( "s5.lock.tracing.id" ); //$NON-NLS-1$
    LOCK_TRACING_ID = (tracingId != null ? tracingId : TsLibUtils.EMPTY_STRING);
    // Вывод в журнал
    logger.info( "LOCK_TRACING = %b, LOCK_TRACING_ID = %s", Boolean.valueOf( LOCK_TRACING ), LOCK_TRACING_ID ); //$NON-NLS-1$
  }

  /**
   * Конструктор по умолчанию
   */
  public S5Lockable() {
    control = new S5LockControl( new ReentrantReadWriteLock() );
  }

  /**
   * Конструктор
   *
   * @param aLock {@link ReentrantReadWriteLock} блокировка
   * @param aName String имя блокировки для идентификакции в журналах
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5Lockable( ReentrantReadWriteLock aLock, String aName ) {
    TsNullArgumentRtException.checkNulls( aLock, aName );
    control = new S5LockControl( aLock, aName );
  }

  /**
   * Возвращает таймаут попытки захвата блокировки по умолчанию
   *
   * @return таймаут (мсек)
   */
  public static long accessTimeoutDefault() {
    return accessTimeoutDefault;
  }

  /**
   * Устанавливает таймаут попытки захвата блокировки по умолчанию
   *
   * @param aTimeout long таймаут (мсек)
   * @throws TsIllegalArgumentRtException таймаут <= 0
   */
  public static void setAccessTimeoutDefault( long aTimeout ) {
    TsIllegalArgumentRtException.checkTrue( aTimeout <= 0 );
    accessTimeoutDefault = aTimeout;
  }

  // ------------------------------------------------------------------------------------
  // Открытые методы для наследников
  //
  /**
   * Возвращает {@link ReentrantReadWriteLock#getQueueLength()}
   *
   * @return int {@link ReentrantReadWriteLock#getQueueLength()}
   */
  public final int queueLength() {
    return control.queueLength();
  }

  /**
   * Возвращает {@link ReentrantReadWriteLock#getReadLockCount()}
   *
   * @return int {@link ReentrantReadWriteLock#getReadLockCount()}
   */
  public final int readLockCount() {
    return control.readLockCount();
  }

  /**
   * Возвращает {@link ReentrantReadWriteLock#getReadHoldCount()}
   *
   * @return int {@link ReentrantReadWriteLock#getReadHoldCount()}
   */
  public final int readHoldCount() {
    return control.readHoldCount();
  }

  /**
   * Возвращает {@link ReentrantReadWriteLock#getWriteHoldCount()}
   *
   * @return int {@link ReentrantReadWriteLock#getWriteHoldCount()}
   */
  public final int writeHoldCount() {
    return control.writeHoldCount();
  }

  // ------------------------------------------------------------------------------------
  // API для наследников
  //
  /**
   * Делает попытку захвата блокировки на чтение
   *
   * @throws TsIllegalArgumentRtException ошибка получения блокировки
   * @throws TsIllegalStateRtException ожидание блокировки было прервано вызовом {@link Thread#interrupt()}
   */
  protected final void lockRead() {
    control.lockRead( accessTimeoutDefault );
  }

  /**
   * Делает попытку захвата блокировки на чтение
   *
   * @param aAccessTimeout long таймаут(мсек) попытки захвата блокировки по умолчанию
   * @throws TsIllegalArgumentRtException таймаут <= 0
   * @throws TsIllegalArgumentRtException ошибка получения блокировки
   * @throws TsIllegalStateRtException ожидание блокировки было прервано вызовом {@link Thread#interrupt()}
   */
  protected final void lockRead( long aAccessTimeout ) {
    control.lockRead( aAccessTimeout );
  }

  /**
   * Делает попытку захвата блокировки на чтение
   *
   * @return <b>true</b> блокировка получена;<b>false</b> ошибка получения блокировки.
   * @throws TsIllegalArgumentRtException таймаут <= 0
   */
  boolean tryLockRead() {
    return control.tryLockRead();
  }

  /**
   * Делает попытку захвата блокировки на чтение
   *
   * @param aAccessTimeout long таймаут(мсек) попытки захвата блокировки
   * @return <b>true</b> блокировка получена;<b>false</b> ошибка получения блокировки.
   * @throws TsIllegalArgumentRtException таймаут <= 0
   */
  boolean tryLockRead( long aAccessTimeout ) {
    return control.tryLockRead( aAccessTimeout );
  }

  /**
   * Освобождает блокировку на чтение
   */
  protected final void unlockRead() {
    control.unlockRead();
  }

  /**
   * Делает попытку захвата блокировки на запись
   *
   * @throws TsIllegalArgumentRtException ошибка получения блокировки
   * @throws TsIllegalStateRtException ожидание блокировки было прервано вызовом {@link Thread#interrupt()}
   */
  protected final void lockWrite() {
    control.lockWrite( accessTimeoutDefault );
  }

  /**
   * Делает попытку захвата блокировки на запись
   *
   * @param aAccessTimeout long таймаут(мсек) попытки захвата блокировки по умолчанию
   * @throws TsIllegalArgumentRtException таймаут <= 0
   * @throws TsIllegalArgumentRtException ошибка получения блокировки
   * @throws TsIllegalStateRtException ожидание блокировки было прервано вызовом {@link Thread#interrupt()}
   */
  protected final void lockWrite( long aAccessTimeout ) {
    control.lockWrite( aAccessTimeout );
  }

  /**
   * Делает попытку захвата блокировки на запись.
   *
   * @return <b>true</b> блокировка получена;<b>false</b> ошибка получения блокировки.
   * @throws TsIllegalArgumentRtException таймаут <= 0
   */
  boolean tryLockWrite() {
    return control.tryLockWrite();
  }

  /**
   * Делает попытку захвата блокировки на запись.
   *
   * @param aAccessTimeout long таймаут(мсек) попытки захвата блокировки
   * @return <b>true</b> блокировка получена;<b>false</b> ошибка получения блокировки.
   * @throws TsIllegalArgumentRtException таймаут <= 0
   */
  boolean tryLockWrite( long aAccessTimeout ) {
    return control.tryLockWrite( aAccessTimeout );
  }

  /**
   * Освобождает блокировку на запись
   */
  protected final void unlockWrite() {
    control.unlockWrite();
  }

  /**
   * Делает попытку разблокировать ресурс через прерывание потоков владельцев блокировки
   */
  protected final void lockThreadInterrupt() {
    control.lockThreadInterrupt();
  }

  /**
   * Возвращает native-блокировку
   *
   * @return {@link ReentrantReadWriteLock} блокировка
   */
  protected final ReentrantReadWriteLock nativeLock() {
    return control.nativeLock();
  }

  // ------------------------------------------------------------------------------------
  // Открытые вспомогательные методы
  //
  /**
   * Делает попытку захвата блокировки на чтение
   *
   * @param aLocable блокируемый ресурс
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException ошибка получения блокировки
   * @throws TsIllegalStateRtException ожидание блокировки было прервано вызовом {@link Thread#interrupt()}
   */
  public static void lockRead( S5Lockable aLocable ) {
    TsNullArgumentRtException.checkNull( aLocable );
    aLocable.lockRead();
  }

  /**
   * Делает попытку захвата блокировки на чтение
   *
   * @param aLocable блокируемый ресурс
   * @param aAccessTimeout long таймаут(мсек) попытки захвата блокировки по умолчанию
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException таймаут <= 0
   * @throws TsIllegalArgumentRtException ошибка получения блокировки
   * @throws TsIllegalStateRtException ожидание блокировки было прервано вызовом {@link Thread#interrupt()}
   */
  public static void lockRead( S5Lockable aLocable, long aAccessTimeout ) {
    TsNullArgumentRtException.checkNull( aLocable );
    aLocable.lockRead( aAccessTimeout );
  }

  /**
   * Делает попытку захвата блокировки на чтение
   *
   * @param aLocable блокируемый ресурс
   * @param aAccessTimeout long таймаут(мсек) попытки захвата блокировки
   * @return <b>true</b> блокировка получена;<b>false</b> ошибка получения блокировки.
   * @throws TsIllegalArgumentRtException таймаут <= 0
   */
  public static boolean tryLockRead( S5Lockable aLocable, long aAccessTimeout ) {
    TsNullArgumentRtException.checkNull( aLocable );
    return aLocable.tryLockRead( aAccessTimeout );
  }

  /**
   * Освобождает блокировку на чтение
   *
   * @param aLocable блокируемый ресурс
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static void unlockRead( S5Lockable aLocable ) {
    TsNullArgumentRtException.checkNull( aLocable );
    aLocable.unlockRead();
  }

  /**
   * Делает попытку захвата блокировки на запись
   *
   * @param aLocable блокируемый ресурс
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException ошибка получения блокировки
   * @throws TsIllegalStateRtException ожидание блокировки было прервано вызовом {@link Thread#interrupt()}
   */
  public static void lockWrite( S5Lockable aLocable ) {
    TsNullArgumentRtException.checkNull( aLocable );
    aLocable.lockWrite();
  }

  /**
   * Делает попытку захвата блокировки на запись
   *
   * @param aLocable блокируемый ресурс
   * @param aAccessTimeout long таймаут(мсек) попытки захвата блокировки по умолчанию
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException таймаут <= 0
   * @throws TsIllegalArgumentRtException ошибка получения блокировки
   * @throws TsIllegalStateRtException ожидание блокировки было прервано вызовом {@link Thread#interrupt()}
   */
  public static void lockWrite( S5Lockable aLocable, long aAccessTimeout ) {
    TsNullArgumentRtException.checkNull( aLocable );
    aLocable.lockWrite( aAccessTimeout );
  }

  /**
   * Делает попытку захвата блокировки на запись.
   *
   * @param aLocable блокируемый ресурс
   * @param aAccessTimeout long таймаут(мсек) попытки захвата блокировки
   * @return <b>true</b> блокировка получена;<b>false</b> ошибка получения блокировки.
   * @throws TsIllegalArgumentRtException таймаут <= 0
   */
  public static boolean tryLockWrite( S5Lockable aLocable, long aAccessTimeout ) {
    TsNullArgumentRtException.checkNull( aLocable );
    return aLocable.tryLockWrite( aAccessTimeout );
  }

  /**
   * Освобождает блокировку на запись
   *
   * @param aLocable блокируемый ресурс
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static final void unlockWrite( S5Lockable aLocable ) {
    TsNullArgumentRtException.checkNull( aLocable );
    aLocable.unlockWrite();
  }

  /**
   * Делает попытку разблокировать ресурс через прерывание потоков владельцев блокировки
   *
   * @param aLocable блокируемый ресурс
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static final void lockThreadInterrupt( S5Lockable aLocable ) {
    TsNullArgumentRtException.checkNull( aLocable );
    aLocable.lockThreadInterrupt();
  }

  /**
   * Возвращает native-блокировку
   *
   * @param aLocable блокируемый ресурс
   * @return {@link ReentrantReadWriteLock} блокировка доступа
   */
  public static final ReentrantReadWriteLock nativeLock( S5Lockable aLocable ) {
    TsNullArgumentRtException.checkNull( aLocable );
    return aLocable.nativeLock();
  }

  /**
   * Установить блокировку на чтение
   *
   * @param aLock {@link ReentrantReadWriteLock} блокировка ресурса
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static void lockRead( ReentrantReadWriteLock aLock ) {
    TsNullArgumentRtException.checkNull( aLock );
    getLockableFromPool( aLock ).lockRead( accessTimeoutDefault );
  }

  /**
   * Отменить блокировку на чтение
   *
   * @param aLock {@link ReentrantReadWriteLock} блокировка ресурса
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static final void unlockRead( ReentrantReadWriteLock aLock ) {
    TsNullArgumentRtException.checkNull( aLock );
    getLockableFromPool( aLock ).unlockRead();
  }

  /**
   * Установить блокировку на запись
   *
   * @param aLock {@link ReentrantReadWriteLock} блокировка ресурса
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static void lockWrite( ReentrantReadWriteLock aLock ) {
    TsNullArgumentRtException.checkNull( aLock );
    getLockableFromPool( aLock ).lockWrite( accessTimeoutDefault );
  }

  /**
   * Отменить блокировку на запись
   *
   * @param aLock {@link ReentrantReadWriteLock} блокировка ресурса
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static final void unlockWrite( ReentrantReadWriteLock aLock ) {
    TsNullArgumentRtException.checkNull( aLock );
    getLockableFromPool( aLock ).unlockWrite();
  }

  /**
   * Зарегистрировать блокировку в пуле если она еще не зарегистрирована
   *
   * @param aLock {@link ReentrantReadWriteLock} блокировка ресурса
   * @param aName String имя блокировки для идентификакции в журналах
   * @return {@link S5Lockable} зарегистрированная блокировка
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static final S5Lockable addLockableIfNotExistToPool( ReentrantReadWriteLock aLock, String aName ) {
    TsNullArgumentRtException.checkNulls( aLock, aName );
    lockWrite( lockablesLock );
    try {
      S5Lockable retValue = lockables.findByKey( aLock );
      if( retValue != null ) {
        return retValue;
      }
      retValue = new S5Lockable( aLock, aName );
      lockables.put( aLock, retValue );
      return retValue;
    }
    finally {
      unlockWrite( lockablesLock );
    }
  }

  /**
   * Вернуть блокировку из пула
   *
   * @param aLock {@link ReentrantReadWriteLock} блокировка ресурса
   * @return {@link S5Lockable} зарегистрированная блокировка
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException блокировка не найдена
   */
  public static final S5Lockable getLockableFromPool( ReentrantReadWriteLock aLock ) {
    TsNullArgumentRtException.checkNull( aLock );
    lockRead( lockablesLock );
    try {
      return lockables.getByKey( aLock );
    }
    finally {
      unlockRead( lockablesLock );
    }
  }

  /**
   * Вернуть блокировку из пула
   *
   * @param aLock {@link ReentrantReadWriteLock} блокировка ресурса
   * @return {@link S5Lockable} зарегистрированная блокировка. null: блокировка не найдена
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static final S5Lockable findLockableFromPool( ReentrantReadWriteLock aLock ) {
    TsNullArgumentRtException.checkNull( aLock );
    lockRead( lockablesLock );
    try {
      return lockables.findByKey( aLock );
    }
    finally {
      unlockRead( lockablesLock );
    }
  }

  /**
   * Дерегистрировать блокировку в пуле
   * <p>
   * Если блокировка не зарегистрирована, то ничего не делает
   *
   * @param aLock {@link ReentrantReadWriteLock} блокировка ресурса
   * @return {@link S5Lockable} дерегистрированная блокировка. null: не найдена в пуле
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static final S5Lockable removeLockableFromPool( ReentrantReadWriteLock aLock ) {
    TsNullArgumentRtException.checkNull( aLock );
    lockWrite( lockablesLock );
    try {
      return lockables.removeByKey( aLock );
    }
    finally {
      unlockWrite( lockablesLock );
    }
  }

  // ------------------------------------------------------------------------------------
  // Object
  //
  @Override
  public String toString() {
    return control.toString();
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + control.hashCode();
    return result;
  }

  @Override
  public boolean equals( Object aObject ) {
    if( this == aObject ) {
      return true;
    }
    if( aObject == null ) {
      return false;
    }
    if( getClass() != aObject.getClass() ) {
      return false;
    }
    S5Lockable other = (S5Lockable)aObject;
    return control.equals( other.control );
  }
}
