package org.toxsoft.uskat.s5.utils.indexes.impl;

import static org.toxsoft.uskat.s5.utils.indexes.impl.IS5Resources.*;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.LongArrayList;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.utils.indexes.ILongElemIndex;

/**
 * Карта значений по длинному целочисленному ключу на индексе доступа {@link ILongElemIndex}
 *
 * @author mvk
 * @param <E> тип значений
 */
class S5LongElemMap<E>
    implements ILongMap<E>, Serializable {

  private static final long serialVersionUID = 157157L;

  private final ILongElemIndex<E> index;

  /**
   * Создание карты значений по длинному целочисленному ключу на индексе доступа {@link ILongElemIndex}
   *
   * @param aIndex {@link ILongElemIndex} индекс доступа к значениям
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException в индексе доступа есть ключи имеющие одинаковые значения
   */
  S5LongElemMap( ILongElemIndex<E> aIndex ) {
    TsNullArgumentRtException.checkNull( aIndex );
    index = aIndex;
    if( index.watermark() > 0 ) {
      // Проверка уникальности ключей
      long prevKey = index.longKey( 0 );
      for( int i = 1, n = index.watermark(); i < n; i++ ) {
        long key = index.longKey( i );
        if( key == prevKey ) {
          throw new TsIllegalArgumentRtException( MSG_ERR_DOUBLE_KEY, Long.valueOf( key ) );
        }
        prevKey = key;
      }
    }
  }

  // --------------------------------------------------------------------------
  // API пакета
  //
  /**
   * Возвращает индекс доступа к значениям с которым работает карта
   *
   * @return {@link ILongElemIndex} индекс доступа к значениям
   */
  ILongElemIndex<E> index() {
    return index;
  }

  // --------------------------------------------------------------------------
  // Реализация интерфейса ILongMap
  //
  @Override
  public boolean hasKey( long aId ) {
    int i = index.findIndex( aId );
    return i >= 0 && index.longKey( i ) == aId;
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
  public E getByKey( long aId ) {
    return index.getValue( aId );
  }

  @Override
  public E findByKey( long aId ) {
    int i = index.findIndex( aId );
    if( i >= 0 && aId == index.longKey( i ) ) {
      return index.value( i );
    }
    return null;
  }

  @Override
  public ILongList keys() {
    ILongListEdit ids = new LongArrayList( index.watermark() );
    for( int i = 0, n = index.watermark(); i < n; i++ ) {
      ids.add( index.longKey( i ) );
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
  public boolean hasKey( Long aKey ) {
    return hasKey( aKey.longValue() );
  }

  @Override
  public E findByKey( Long aKey ) {
    return findByKey( aKey.longValue() );
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
          return index().value( i++ );
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
