package org.toxsoft.uskat.s5.utils.indexes.impl;

import static org.toxsoft.uskat.s5.utils.indexes.impl.IS5Resources.*;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.IntArrayList;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.utils.indexes.IIntElemIndex;

/**
 * Карта значений по целочисленному ключу на индексе доступа {@link IIntElemIndex}
 *
 * @author mvk
 * @param <E> тип значений
 */
class S5IntElemMap<E>
    implements IIntMap<E>, Serializable {

  private static final long serialVersionUID = 157157L;

  private final IIntElemIndex<E> index;

  /**
   * Создание карты значений по целочисленному ключу на индексе доступа {@link IIntElemIndex}
   *
   * @param aIndex {@link IIntElemIndex} индекс доступа к значениям
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException в индексе доступа есть ключи имеющие одинаковые значения
   */
  S5IntElemMap( IIntElemIndex<E> aIndex ) {
    TsNullArgumentRtException.checkNull( aIndex );
    index = aIndex;
    if( index.watermark() > 0 ) {
      // Проверка уникальности ключей
      int prevKey = index.intKey( 0 );
      for( int i = 1, n = index.watermark(); i < n; i++ ) {
        int key = index.intKey( i );
        if( key == prevKey ) {
          throw new TsIllegalArgumentRtException( MSG_ERR_DOUBLE_KEY, Long.valueOf( key ) );
        }
        prevKey = key;
      }
    }
  }

  // --------------------------------------------------------------------------
  // Реализация интерфейса IIntMap
  //
  @Override
  public boolean hasKey( int aId ) {
    int i = index.findIndex( aId );
    return i >= 0 && index.intKey( i ) == aId;
  }

  @Override
  public boolean hasElem( E aElem ) {
    TsNullArgumentRtException.checkNull( aElem );
    for( int i = 0, n = index.watermark(); i < n; i++ ) {
      if( index.value( i ).equals( aElem ) ) {
        return true;
      }
    }
    return false;
  }

  @Override
  public E getByKey( int aId ) {
    return index.getValue( aId );
  }

  @Override
  public E findByKey( int aId ) {
    int i = index.findIndex( aId );
    if( i >= 0 && aId == index.intKey( i ) ) {
      return index.value( i );
    }
    return null;
  }

  @Override
  public IIntList keys() {
    IIntListEdit ids = new IntArrayList( index.watermark() );
    for( int i = 0, n = index.watermark(); i < n; i++ ) {
      ids.add( index.intKey( i ) );
    }
    return ids;
  }

  @Override
  public IList<E> values() {
    IListEdit<E> values = new ElemArrayList<>( index.watermark() );
    for( int i = 0, n = index.watermark(); i < n; i++ ) {
      values.add( index.value( i ) );
    }
    return values;
  }

  @Override
  public boolean hasKey( Integer aKey ) {
    return hasKey( aKey.intValue() );
  }

  @Override
  public E findByKey( Integer aKey ) {
    return findByKey( aKey.intValue() );
  }

  @Override
  public Iterator<E> iterator() {
    return new Iterator<>() {

      int i = 0;

      @Override
      public boolean hasNext() {
        return i < size();
      }

      @Override
      public E next() {
        if( hasNext() ) {
          return getByKey( i++ );
        }
        throw new NoSuchElementException();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

    };
  }

  @Override
  public boolean isEmpty() {
    return index.watermark() == 0;
  }

  @Override
  public int size() {
    return index.watermark();
  }

  @Override
  public E[] toArray( E[] aSrcArray ) {
    throw new TsUnderDevelopmentRtException();
  }

  @Override
  public Object[] toArray() {
    throw new TsUnderDevelopmentRtException();
  }
}
