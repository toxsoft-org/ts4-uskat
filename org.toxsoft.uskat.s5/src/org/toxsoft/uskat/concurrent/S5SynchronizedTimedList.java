package org.toxsoft.uskat.concurrent;

import java.util.Iterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.cmdserv.ISkCommand;

/**
 * Синхронизация доступа к {@link ITimedList} (декоратор)
 *
 * @author mvk
 * @param <T> тип элементов
 */
public final class S5SynchronizedTimedList<T extends ITimestampable>
    extends S5SynchronizedResource<ITimedList<T>>
    implements ITimedList<T> {

  /**
   * Конструктор
   *
   * @param aTarget {@link ISkCommand} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedTimedList( ITimedList<T> aTarget, ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ITimedList<T> aPrevTarget, ITimedList<T> aNewTarget,
      ReentrantReadWriteLock aNewLock ) {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // ITimedList
  //
  @Override
  public int indexOf( T aElem ) {
    lockWrite( this );
    try {
      return target().indexOf( aElem );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public T get( int aIndex ) {
    lockWrite( this );
    try {
      return target().get( aIndex );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public boolean hasElem( T aElem ) {
    lockWrite( this );
    try {
      return target().hasElem( aElem );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public T[] toArray( T[] aSrcArray ) {
    lockWrite( this );
    try {
      return target().toArray( aSrcArray );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public Object[] toArray() {
    lockWrite( this );
    try {
      return target().toArray();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public Iterator<T> iterator() {
    lockWrite( this );
    try {
      return target().iterator();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public int size() {
    lockWrite( this );
    try {
      return target().size();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ITimeInterval getInterval() {
    lockWrite( this );
    try {
      return target().getInterval();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ITimedListEdit<T> selectInterval( ITimeInterval aTimeInterval ) {
    lockWrite( this );
    try {
      return target().selectInterval( aTimeInterval );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ITimedListEdit<T> selectExtendedInterval( ITimeInterval aTimeInterval ) {
    lockWrite( this );
    try {
      return target().selectExtendedInterval( aTimeInterval );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ITimedListEdit<T> selectAfter( long aTimestamp ) {
    lockWrite( this );
    try {
      return target().selectAfter( aTimestamp );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ITimedListEdit<T> selectBefore( long aTimestamp ) {
    lockWrite( this );
    try {
      return target().selectBefore( aTimestamp );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public int firstIndexOf( long aTimestamp ) {
    lockWrite( this );
    try {
      return target().firstIndexAfter( aTimestamp );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public int lastIndexOf( long aTimestamp ) {
    lockWrite( this );
    try {
      return target().lastIndexOrAfter( aTimestamp );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public int firstIndexOrBefore( long aTimestamp ) {
    lockWrite( this );
    try {
      return target().firstIndexOrBefore( aTimestamp );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public int firstIndexOrAfter( long aTimestamp ) {
    lockWrite( this );
    try {
      return target().firstIndexOrAfter( aTimestamp );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public int lastIndexOrBefore( long aTimestamp ) {
    lockWrite( this );
    try {
      return target().lastIndexOrBefore( aTimestamp );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public int lastIndexOrAfter( long aTimestamp ) {
    lockWrite( this );
    try {
      return target().lastIndexOrAfter( aTimestamp );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public int firstIndexAfter( long aTimestamp ) {
    lockWrite( this );
    try {
      return target().firstIndexAfter( aTimestamp );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public int lastIndexBefore( long aTimestamp ) {
    lockWrite( this );
    try {
      return target().lastIndexBefore( aTimestamp );
    }
    finally {
      unlockWrite( this );
    }
  }
}
