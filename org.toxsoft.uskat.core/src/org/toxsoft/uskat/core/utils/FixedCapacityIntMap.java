package org.toxsoft.uskat.core.utils;

import java.io.*;
import java.util.*;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.wrappers.*;
import org.toxsoft.core.tslib.coll.wrappers.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Неполноценная карта фиксированной емкости.
 * <p>
 * Реализация сделана для оптимизации передачи в сериализированном виде и для ускоренного доступа к значениям по ключу.
 * Основной (и единственный) сценарии использования:
 * <ul>
 * <li>вычисляется количество передаваемых пар int/&lt;E&gt;;</li>
 * <li>создается экземпляр карты с нужной максимальной емкостью;</li>
 * <li>в экземпляр заносятся все значения <b>только одним</b> методом - {@link #put(int, Object)};</li>
 * <li>созданная карта отправляется по сети, для получателя, который ее видит нередактируемым {@link IIntMapEdit};</li>
 * <li>если есть гарантия, что среди пар int/&lt;E&gt; не повторяющихся ключей, можно использовать метод
 * {@link #add(int, Object)}, который чрезвичайно быстрый, но неправильный, не гарантирует уникальность ключей.</li>
 * <li>получатель должен только видеть карту как нередактируемый {@link IIntMap};</li>
 * </ul>
 * <p>
 * Из методов редактирования поддерживает только добавление элементов методом {@link #put(int, Object)}, сотальные
 * методы редактирования приводят к исключению {@link TsUnsupportedFeatureRtException}.
 *
 * @author goga
 * @param <E> - тип эелементов в карте
 */
public class FixedCapacityIntMap<E>
    implements IIntMapEdit<E>, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * FIXME для ускорения доступа по ключу следует хранить ключи в сортированном виде и осуществлять двичный поиск
   */

  final int[]    keys;
  final Object[] values;

  int size = 0;

  /**
   * Конструктор.
   *
   * @param aCapacity int - максимальная емкость
   * @throws TsIllegalStateRtException емкость < 0
   */
  public FixedCapacityIntMap( int aCapacity ) {
    TsIllegalArgumentRtException.checkTrue( aCapacity < 0 );
    keys = new int[aCapacity];
    values = new Object[aCapacity];
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IIntMap
  //

  @Override
  public boolean hasKey( Integer aKey ) {
    TsNullArgumentRtException.checkNull( aKey );
    return hasKey( aKey.intValue() );
  }

  @Override
  public E findByKey( Integer aKey ) {
    TsNullArgumentRtException.checkNull( aKey );
    return findByKey( aKey.intValue() );
  }

  @Override
  public Iterator<E> iterator() {
    return new Iterator<>() {

      int index = 0;

      @Override
      public boolean hasNext() {
        return index <= size;
      }

      @SuppressWarnings( "unchecked" )
      @Override
      public E next() {
        return (E)values[index++];
      }
    };
  }

  @Override
  public boolean isEmpty() {
    return size == 0;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean hasKey( int aId ) {
    for( int i = 0; i < size; i++ ) {
      if( keys[i] == aId ) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean hasElem( E aElem ) {
    TsNullArgumentRtException.checkNull( aElem );
    for( int i = 0; i < size; i++ ) {
      if( values[i].equals( aElem ) ) {
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public E getByKey( int aId ) {
    for( int i = 0; i < size; i++ ) {
      if( keys[i] == aId ) {
        return (E)values[i];
      }
    }
    throw new TsItemNotFoundRtException();
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public E findByKey( int aId ) {
    for( int i = 0; i < size; i++ ) {
      if( keys[i] == aId ) {
        return (E)values[i];
      }
    }
    return null;
  }

  @Override
  public IIntList keys() {
    return new IntArrayWrapper( keys );
  }

  @SuppressWarnings( { "unchecked", "rawtypes" } )
  @Override
  public IList<E> values() {
    return (IList)new ElemArrayWrapper<>( values );
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IIntMapEdit
  //

  @Override
  public void clear() {
    size = 0;
    Arrays.fill( values, null );
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public E put( int aId, E aElem ) {
    TsNullArgumentRtException.checkNull( aElem );
    for( int i = 0; i < size; i++ ) {
      if( keys[i] == aId ) {
        Object oldVal = values[i];
        values[i] = aElem;
        return (E)oldVal;
      }
    }
    TsIllegalStateRtException.checkTrue( size == keys.length );
    keys[size] = aId;
    values[size] = aElem;
    ++size;
    return null;
  }

  @Override
  public E removeByKey( int aId ) {
    throw new TsUnsupportedFeatureRtException();
  }

  @Override
  public void putAll( IIntMap<? extends E> aSrc ) {
    throw new TsUnsupportedFeatureRtException();
  }

  @Override
  public void setAll( IIntMap<? extends E> aSrc ) {
    throw new TsUnsupportedFeatureRtException();
  }

  // ------------------------------------------------------------------------------------
  // API класса
  //

  /**
   * БЕЗ ПРОВЕРКИ на наличие ключа добавляет элемент в карту.
   * <p>
   * Внимание: этот <b>очень быстрый</b> метод только для оптимизации! Использовать можно, когда есть уверенность, что
   * добавляемого ключа нет в карте.
   *
   * @param aId int - ключ
   * @param aElem &lt;E&gt; - значение
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException карта полная
   */
  public void add( int aId, E aElem ) {
    TsNullArgumentRtException.checkNull( aElem );
    TsIllegalStateRtException.checkTrue( size == keys.length );
    keys[size] = aId;
    values[size] = aElem;
    ++size;
  }

  @Override
  public E[] toArray( E[] aSrcArray ) {
    throw new TsUnderDevelopmentRtException();
  }

  @Override
  public Object[] toArray() {
    throw new TsUnderDevelopmentRtException();
  }

  @Override
  public E put( Integer aKey, E aElem ) {
    throw new TsUnderDevelopmentRtException();
  }

  @Override
  public E removeByKey( Integer aKey ) {
    throw new TsUnderDevelopmentRtException();
  }

}
