package org.toxsoft.uskat.s5.utils.threads.impl;

import static java.util.concurrent.TimeUnit.*;
import static org.toxsoft.core.log4j.Logger.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.IS5Resources.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;

import java.util.Formatter;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.IMapEdit;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ELogSeverity;
import org.toxsoft.core.tslib.utils.logs.ILogger;

/**
 * Управление блокировкой
 *
 * @author mvk
 */
final class S5LockControl {

  private static AtomicLong            instanceCounter = new AtomicLong();
  private final String                 name;
  private final ReentrantReadWriteLock lock;
  @SuppressWarnings( "unused" )
  private final StackTraceElement[]    createLocation  = Thread.currentThread().getStackTrace();

  private final IMapEdit<Thread, IListEdit<StackTraceElement[]>> readsByThreads  = new ElemMap<>();
  private final IMapEdit<Thread, IListEdit<StackTraceElement[]>> writesByThreads = new ElemMap<>();

  private final ILogger logger = getLogger( getClass() );

  /**
   * Конструктор
   *
   * @param aLock {@link ReentrantReadWriteLock} блокировка
   */
  S5LockControl( ReentrantReadWriteLock aLock ) {
    this( aLock, String.valueOf( instanceCounter.incrementAndGet() ) );
  }

  /**
   * Конструктор
   *
   * @param aLock {@link ReentrantReadWriteLock} блокировка
   * @param aName String имя блокировки для идентификакции в журналах
   */
  @SuppressWarnings( "nls" )
  S5LockControl( ReentrantReadWriteLock aLock, String aName ) {
    TsNullArgumentRtException.checkNulls( aLock, aName );
    lock = aLock;
    name = "S5LockControl[" + aName + "]";
  }

  // ------------------------------------------------------------------------------------
  // Методы пакета
  //
  /**
   * Делает попытку захвата блокировки на чтение
   *
   * @param aAccessTimeout long таймаут(мсек) попытки захвата блокировки
   * @throws TsIllegalArgumentRtException таймаут <= 0
   * @throws TsIllegalArgumentRtException ошибка получения блокировки
   * @throws TsIllegalStateRtException ожидание блокировки было прервано вызовом {@link Thread#interrupt()}
   */
  void lockRead( long aAccessTimeout ) {
    TsIllegalArgumentRtException.checkFalse( aAccessTimeout > 0 );
    Thread thread = Thread.currentThread();
    try {
      if( !lock.readLock().tryLock( aAccessTimeout, MILLISECONDS ) ) {
        throwLockException( ERR_READ, this, aAccessTimeout );
      }
      if( LOCK_TRACING ) {
        synchronized (this) {
          IListEdit<StackTraceElement[]> reads = readsByThreads.findByKey( thread );
          if( reads == null ) {
            reads = new ElemLinkedList<>();
            readsByThreads.put( thread, reads );
          }
          reads.add( thread.getStackTrace() );
          // Вывод в журнал
          debug( this, "tryLockRead" ); //$NON-NLS-1$
        }
      }
    }
    catch( InterruptedException e ) {
      // Ожидание блокировки было прервано вызовом {@link Thread#interrupt()}
      throw new TsIllegalStateRtException( e, ERR_INTERRUPT, this, thread.getName() );
    }
  }

  /**
   * Делает попытку захвата блокировки на чтение
   *
   * @return <b>true</b> блокировка получена;<b>false</b> ошибка получения блокировки.
   * @throws TsIllegalArgumentRtException таймаут <= 0
   */
  boolean tryLockRead() {
    if( !lock.readLock().tryLock() ) {
      return false;
    }
    if( LOCK_TRACING ) {
      synchronized (this) {
        Thread thread = Thread.currentThread();
        IListEdit<StackTraceElement[]> reads = readsByThreads.findByKey( thread );
        if( reads == null ) {
          reads = new ElemLinkedList<>();
          readsByThreads.put( thread, reads );
        }
        reads.add( thread.getStackTrace() );
        // Вывод в журнал
        debug( this, "tryLockRead" ); //$NON-NLS-1$
      }
    }
    return true;
  }

  /**
   * Делает попытку захвата блокировки на чтение
   *
   * @param aAccessTimeout long таймаут(мсек) попытки захвата блокировки
   * @return <b>true</b> блокировка получена;<b>false</b> ошибка получения блокировки.
   * @throws TsIllegalArgumentRtException таймаут <= 0
   */
  boolean tryLockRead( long aAccessTimeout ) {
    TsIllegalArgumentRtException.checkFalse( aAccessTimeout > 0 );
    Thread thread = Thread.currentThread();
    try {
      if( !lock.readLock().tryLock( aAccessTimeout, MILLISECONDS ) ) {
        return false;
      }
      if( LOCK_TRACING ) {
        synchronized (this) {
          IListEdit<StackTraceElement[]> reads = readsByThreads.findByKey( thread );
          if( reads == null ) {
            reads = new ElemLinkedList<>();
            readsByThreads.put( thread, reads );
          }
          reads.add( thread.getStackTrace() );
          // Вывод в журнал
          debug( this, "tryLockRead" ); //$NON-NLS-1$
        }
      }
      return true;
    }
    catch( InterruptedException e ) {
      // Ожидание блокировки было прервано вызовом {@link Thread#interrupt()}
      throw new TsIllegalStateRtException( e, ERR_INTERRUPT, this, thread.getName() );
    }
  }

  /**
   * Освобождает блокировку на чтение
   */
  void unlockRead() {
    if( LOCK_TRACING ) {
      Thread thread = Thread.currentThread();
      synchronized (this) {
        IListEdit<StackTraceElement[]> reads = readsByThreads.findByKey( thread );
        if( reads.size() > 0 ) {
          reads.removeByIndex( reads.size() - 1 );
          if( reads.size() == 0 ) {
            readsByThreads.removeByKey( thread );
          }
        }
      }
    }
    lock.readLock().unlock();
    // Вывод в журнал
    debug( this, "unlockRead" ); //$NON-NLS-1$
  }

  /**
   * Делает попытку захвата блокировки на запись
   *
   * @param aAccessTimeout long таймаут(мсек) попытки захвата блокировки
   * @throws TsIllegalArgumentRtException таймаут <= 0
   * @throws TsIllegalArgumentRtException ошибка получения блокировки
   * @throws TsIllegalStateRtException ожидание блокировки было прервано вызовом {@link Thread#interrupt()}
   */
  void lockWrite( long aAccessTimeout ) {
    TsIllegalArgumentRtException.checkFalse( aAccessTimeout > 0 );
    Thread thread = Thread.currentThread();
    try {
      if( LOCK_TRACING ) {
        synchronized (this) {
          IListEdit<StackTraceElement[]> reads = readsByThreads.findByKey( thread );
          if( reads != null && reads.size() > 0 ) {
            // Нельзя делать блокировку на запись при открытой блокировке на чтение
            throwLockException( ERR_WRITE_AFTER_READ, this, aAccessTimeout );
          }
        }
      }
      if( !lock.writeLock().tryLock( aAccessTimeout, MILLISECONDS ) ) {
        throwLockException( ERR_WRITE, this, aAccessTimeout );
      }
      if( LOCK_TRACING ) {
        synchronized (this) {
          IListEdit<StackTraceElement[]> writes = writesByThreads.findByKey( thread );
          if( writes == null ) {
            writes = new ElemLinkedList<>();
            writesByThreads.put( thread, writes );
          }
          writes.add( thread.getStackTrace() );
          // Вывод в журнал
          debug( this, "tryLockWrite" ); //$NON-NLS-1$
        }
      }
    }
    catch( InterruptedException e ) {
      // Ожидание блокировки было прервано вызовом {@link Thread#interrupt()}
      throw new TsIllegalStateRtException( e, ERR_INTERRUPT, this, thread.getName() );
    }
  }

  /**
   * Делает попытку захвата блокировки на запись.
   *
   * @return <b>true</b> блокировка получена;<b>false</b> ошибка получения блокировки.
   * @throws TsIllegalArgumentRtException таймаут <= 0
   */
  boolean tryLockWrite() {
    Thread thread = Thread.currentThread();
    if( LOCK_TRACING ) {
      synchronized (this) {
        IListEdit<StackTraceElement[]> reads = readsByThreads.findByKey( thread );
        if( reads != null && reads.size() > 0 ) {
          return false;
        }
      }
    }
    if( !lock.writeLock().tryLock() && !lock.writeLock().tryLock() ) {
      return false;
    }
    if( LOCK_TRACING ) {
      synchronized (this) {
        IListEdit<StackTraceElement[]> writes = writesByThreads.findByKey( thread );
        if( writes == null ) {
          writes = new ElemLinkedList<>();
          writesByThreads.put( thread, writes );
        }
        writes.add( thread.getStackTrace() );
        // Вывод в журнал
        debug( this, "tryLockWrite" ); //$NON-NLS-1$
      }
    }
    return true;
  }

  /**
   * Делает попытку захвата блокировки на запись.
   *
   * @param aAccessTimeout long таймаут(мсек) попытки захвата блокировки
   * @return <b>true</b> блокировка получена;<b>false</b> ошибка получения блокировки.
   * @throws TsIllegalArgumentRtException таймаут <= 0
   */
  boolean tryLockWrite( long aAccessTimeout ) {
    TsIllegalArgumentRtException.checkFalse( aAccessTimeout > 0 );
    Thread thread = Thread.currentThread();
    try {
      if( LOCK_TRACING ) {
        synchronized (this) {
          IListEdit<StackTraceElement[]> reads = readsByThreads.findByKey( thread );
          if( reads != null && reads.size() > 0 ) {
            return false;
          }
        }
      }
      if( !lock.writeLock().tryLock() && !lock.writeLock().tryLock( aAccessTimeout, MILLISECONDS ) ) {
        return false;
      }
      if( LOCK_TRACING ) {
        synchronized (this) {
          IListEdit<StackTraceElement[]> writes = writesByThreads.findByKey( thread );
          if( writes == null ) {
            writes = new ElemLinkedList<>();
            writesByThreads.put( thread, writes );
          }
          writes.add( thread.getStackTrace() );
          // Вывод в журнал
          debug( this, "tryLockWrite" ); //$NON-NLS-1$
        }
      }
      return true;
    }
    catch( InterruptedException e ) {
      // Ожидание блокировки было прервано вызовом {@link Thread#interrupt()}
      throw new TsIllegalStateRtException( e, ERR_INTERRUPT, this, thread.getName() );
    }
  }

  /**
   * Освобождает блокировку на запись
   */
  void unlockWrite() {
    if( LOCK_TRACING ) {
      Thread thread = Thread.currentThread();
      synchronized (this) {
        IListEdit<StackTraceElement[]> writes = writesByThreads.findByKey( thread );
        if( writes != null && writes.size() > 0 ) {
          writes.removeByIndex( writes.size() - 1 );
          if( writes.size() == 0 ) {
            writesByThreads.removeByKey( thread );
          }
        }
      }
    }
    lock.writeLock().unlock();
    // Вывод в журнал
    debug( this, "unlockWrite" ); //$NON-NLS-1$
  }

  /**
   * Возвращает native-блокировку
   *
   * @return {@link ReentrantReadWriteLock} блокировка
   */
  ReentrantReadWriteLock nativeLock() {
    return lock;
  }

  /**
   * Возвращает {@link ReentrantReadWriteLock#getQueueLength()}
   *
   * @return int {@link ReentrantReadWriteLock#getQueueLength()}
   */
  int queueLength() {
    return lock.getQueueLength();
  }

  /**
   * Возвращает {@link ReentrantReadWriteLock#getReadLockCount()}
   *
   * @return int {@link ReentrantReadWriteLock#getReadLockCount()}
   */
  int readLockCount() {
    return lock.getReadLockCount();
  }

  /**
   * Возвращает {@link ReentrantReadWriteLock#getReadHoldCount()}
   *
   * @return int {@link ReentrantReadWriteLock#getReadHoldCount()}
   */
  int readHoldCount() {
    return lock.getReadHoldCount();
  }

  /**
   * Возвращает {@link ReentrantReadWriteLock#getWriteHoldCount()}
   *
   * @return int {@link ReentrantReadWriteLock#getWriteHoldCount()}
   */
  int writeHoldCount() {
    return lock.getWriteHoldCount();
  }

  // ------------------------------------------------------------------------------------
  // Object
  //
  @Override
  public String toString() {
    return String.format( "%s", name ); //$NON-NLS-1$
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + name.hashCode();
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
    S5LockControl other = (S5LockControl)aObject;
    return name.equals( other.name );
  }

  // ------------------------------------------------------------------------------------
  // Внутренние вспомогательные методы
  //
  /**
   * Поднимает исключение о том, что невозможно получить блокировку за указанное время
   *
   * @param aError String текст ошибки
   * @param aControl {@link S5LockControl} контроль блокировки
   * @param aTimeout long таймаут ожидания блокировки
   */
  private static void throwLockException( String aError, S5LockControl aControl, long aTimeout ) {
    // Текущий стек
    Thread thread = Thread.currentThread();
    String cid = String.format( "%s[%s]", aControl.name, thread.getName() ); //$NON-NLS-1$
    String c = traceToString( thread.getStackTrace() );
    // Формирование стека блокирующий методов
    String name = aControl.name;
    String rn = name;
    String wn = name;
    StackTraceElement[] rst = null;
    StackTraceElement[] wst = null;
    // Стек одного из потоков блокирующего на ЧТЕНИЕ
    if( aControl.readsByThreads.size() > 0 ) {
      rn = String.format( "%s[%s]", name, aControl.readsByThreads.keys().first().getName() ); //$NON-NLS-1$
      rst = aControl.readsByThreads.values().first().first();
    }
    // Стек одного из потоков блокирующего на ЗАПИСЬ
    if( aControl.writesByThreads.size() > 0 ) {
      wn = String.format( "%s[%s]", name, aControl.writesByThreads.keys().first().getName() ); //$NON-NLS-1$
      wst = aControl.writesByThreads.values().first().first();
    }
    Long t = Long.valueOf( aTimeout );
    String r = (rst != null ? traceToString( rst ) : "???"); //$NON-NLS-1$
    String w = (wst != null ? traceToString( wst ) : "???"); //$NON-NLS-1$
    throw new TsIllegalStateRtException( aError, name, t, cid, c, rn, r, wn, w );
  }

  /**
   * Возвращает trace-стек в строковом виде
   *
   * @param aTrace {@link StackTraceElement}[] trace-стек
   * @return строковое представление trace-стек
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static String traceToString( StackTraceElement[] aTrace ) {
    TsNullArgumentRtException.checkNull( aTrace );
    StringBuilder sb = new StringBuilder();
    for( int index = 0, n = aTrace.length; index < n; index++ ) {
      sb.append( String.format( ERR_LINE, aTrace[index].toString() ) );
    }
    return sb.toString();
  }

  /**
   * Выводит форматированное текстовое сообщение с важностью {@link ELogSeverity#DEBUG} в лог.
   * <p>
   * Форматирование происходит согласно правилам класса {@link Formatter}.
   *
   * @param aControl {@link S5LockControl} контроль блокировки
   * @param aMessage String - текст сообщения
   * @param aArgs Object[] - аргументы
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException попутка записи в уже закрытый логер
   */
  private static void debug( S5LockControl aControl, String aMessage, Object... aArgs ) {
    if( !LOCK_TRACING || //
        LOCK_TRACING_ID == null || //
        LOCK_TRACING_ID.length() == 0 || //
        !aControl.name.equals( LOCK_TRACING_ID ) ) {
      return;
    }
    String controlContext = String.format( "%s[%s]: ", aControl, Thread.currentThread().getName() ); //$NON-NLS-1$
    aControl.logger.debug( controlContext + aMessage, aArgs );
  }
}
