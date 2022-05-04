package org.toxsoft.uskat.s5.legacy;

import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.basis.ITsCollection;
import org.toxsoft.core.tslib.coll.basis.ITsSynchronizedCollectionWrapper;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.coll.impl.TsCollectionsUtils;
import org.toxsoft.core.tslib.coll.synch.SynchronizedList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Потоко-безопасная оболочка над редактируемой картой отображения {@link IMapEdit}.
 *
 * @author goga
 * @param <K> - тип ключей в карте
 * @param <E> - тип хранимых элементов, значений в карте
 */
public class SynchronizedMap<K, E>
    implements IMapEdit<K, E>, ITsSynchronizedCollectionWrapper<E>, Serializable {

  private static final long serialVersionUID = 157157L;

  protected final ReentrantReadWriteLock lock;
  protected final IList<K>               synchKeys;
  protected final IList<E>               synchValues;
  protected final IMapEdit<K, E>         source;

  /**
   * Создает оболочку над aSource с потоко-безопасным доступом.
   *
   * @param aSource {@link IMapEdit} - карта - источник
   * @throws TsNullArgumentRtException аргумент = null
   */
  public SynchronizedMap( IMapEdit<K, E> aSource ) {
    this( aSource, new ReentrantReadWriteLock() );
  }

  /**
   * Создает оболочку над aSource с потоко-безопасным доступом с указанием блокировки.
   *
   * @param aSource {@link IMapEdit} - карта - источник
   * @param aLock {@link ReentrantReadWriteLock} - блокировка карты
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public SynchronizedMap( IMapEdit<K, E> aSource, ReentrantReadWriteLock aLock ) {
    TsNullArgumentRtException.checkNulls( aSource, aLock );
    source = aSource;
    lock = aLock;
    synchKeys = new SynchronizedList<>( source.keys(), lock );
    synchValues = new SynchronizedList<>( source.values(), lock );
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IMap
  //

  @Override
  public E getByKey( K aKey ) {
    lock.readLock().lock();
    try {
      return source.getByKey( aKey );
    }
    finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public E findByKey( K aKey ) {
    lock.readLock().lock();
    try {
      return source.findByKey( aKey );
    }
    finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public boolean hasElem( E aElem ) {
    lock.readLock().lock();
    try {
      return source.hasElem( aElem );
    }
    finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public boolean hasKey( K aKey ) {
    lock.readLock().lock();
    try {
      return source.hasKey( aKey );
    }
    finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public boolean isEmpty() {
    lock.readLock().lock();
    try {
      return source.isEmpty();
    }
    finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public IList<K> keys() {
    lock.readLock().lock();
    try {
      return synchKeys;
    }
    finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public int size() {
    lock.readLock().lock();
    try {
      return source.size();
    }
    finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public IList<E> values() {
    lock.readLock().lock();
    try {
      return synchValues;
    }
    finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public Iterator<E> iterator() {
    lock.readLock().lock();
    try {
      return synchValues.iterator();
    }
    finally {
      lock.readLock().unlock();
    }
  }

  /**
   * Копировать ключи и значения карты в представленную карту
   *
   * @param aDest {@link IMapEdit}&lt;K, E&gt; карта-приемник. null: создается по умолчанию
   * @return {@link IMapEdit} карта-приемник
   */
  public IMapEdit<K, E> copyTo( IMapEdit<K, E> aDest ) {
    IMapEdit<K, E> dest = aDest;
    if( dest == null ) {
      dest = new ElemMap<>( TsCollectionsUtils.getMapBucketsCount( TsCollectionsUtils.estimateOrder( size() ) ),
          TsCollectionsUtils.DEFAULT_BUNDLE_CAPACITY );
    }
    lock.readLock().lock();
    try {
      dest.putAll( this );
      return dest;
    }
    finally {
      lock.readLock().unlock();
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IMapEdit
  //

  @Override
  public void clear() {
    lock.writeLock().lock();
    try {
      source.clear();
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public E put( K aKey, E aElem ) {
    lock.writeLock().lock();
    try {
      return source.put( aKey, aElem );
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public void putAll( IMap<K, ? extends E> aSrc ) {
    lock.writeLock().lock();
    try {
      source.putAll( aSrc );
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public void setAll( IMap<K, ? extends E> aSrc ) {
    lock.writeLock().lock();
    try {
      source.setAll( aSrc );
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public E removeByKey( K aKey ) {
    lock.writeLock().lock();
    try {
      return source.removeByKey( aKey );
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public E[] toArray( E[] aSrcArray ) {
    lock.writeLock().lock();
    try {
      return source.toArray( aSrcArray );
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public Object[] toArray() {
    lock.writeLock().lock();
    try {
      return source.toArray();
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса ITsSynchronizedCollectionTag
  //

  @Override
  public ReentrantReadWriteLock getLockObject() {
    return lock;
  }

  @Override
  public ITsCollection<E> getSourceCollection() {
    return source;
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов Object
  //
  @Override
  public String toString() {
    return TsCollectionsUtils.countableCollectionToString( this );
  }

  @Override
  public boolean equals( Object obj ) {
    lock.readLock().lock();
    try {
      return source.equals( obj );
    }
    finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public int hashCode() {
    lock.readLock().lock();
    try {
      return source.hashCode();
    }
    finally {
      lock.readLock().unlock();
    }
  }

}
