package org.toxsoft.uskat.s5.utils.collections;

import static org.toxsoft.uskat.s5.utils.collections.IS5Resources.*;

import java.io.Serializable;
import java.util.*;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Оболочка над java-картой для создания редактируемоей карты {@link IMapEdit}.
 * <p>
 * Для сравнения значений и ключей K, и элементов E используется метод {@link Object#equals(Object)}.
 *
 * @author mvk
 * @version $id$
 * @param <K> - тип ключей в карте
 * @param <E> - тип хранимых элементов, значений в карте
 */
public class WrapperMap<K, E>
    implements IMapEdit<K, E>, Serializable {

  private static final long serialVersionUID = 157157L;

  private final Map<K, E> source;
  private final IList<K>  keysFilter;

  /**
   * Конструктор
   *
   * @param aSource {@link Map}&lt;K,E&gt; исходная java-карта
   * @throws TsNullArgumentRtException аргумент = null
   */
  public WrapperMap( Map<K, E> aSource ) {
    TsNullArgumentRtException.checkNull( aSource );
    source = aSource;
    keysFilter = null;
  }

  /**
   * Конструктор
   * <p>
   * Фильтр ключей карты имеет значение только для методов:
   * <ul>
   * <li>{@link #iterator()};</li>
   * <li>{@link #size()};</li>
   * <li>{@link #isEmpty()};</li>
   * <li>{@link #keys()};</li>
   * <li>{@link #values()};</li>
   * </ul>
   * позволяя уменьшать выборку ключей/значений из больший карт.
   *
   * @param aSource {@link Map}&lt;K,E&gt; исходная java-карта
   * @param aKeysFilter {@link IList}&lt;K&gt; фильтр ключей карты
   * @throws TsNullArgumentRtException аргумент = null
   */
  public WrapperMap( Map<K, E> aSource, IList<K> aKeysFilter ) {
    TsNullArgumentRtException.checkNulls( aSource, aKeysFilter );
    source = aSource;
    keysFilter = aKeysFilter;
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IMap<K,E>
  //
  @Override
  public boolean hasKey( K aKey ) {
    TsNullArgumentRtException.checkNull( aKey );
    return source.containsKey( aKey );
  }

  @Override
  public boolean hasElem( E aElem ) {
    TsNullArgumentRtException.checkNull( aElem );
    return source.containsValue( aElem );
  }

  @Override
  public E getByKey( K aKey ) {
    TsNullArgumentRtException.checkNull( aKey );
    E retValue = source.get( aKey );
    if( retValue == null ) {
      // Элемент запрошенный по ключу не найден
      throw new TsItemNotFoundRtException( ERR_ITEM_NOT_FOUND, this.getClass().getSimpleName(), aKey );
    }
    return retValue;
  }

  @Override
  public E findByKey( K aKey ) {
    TsNullArgumentRtException.checkNull( aKey );
    return source.get( aKey );
  }

  @Override
  public IList<K> keys() {
    if( keysFilter != null ) {
      // Фильтрация ключей карты
      Set<K> keys = source.keySet();
      IListEdit<K> retValue = new ElemArrayList<>( keysFilter.size() );
      for( K key : keysFilter ) {
        if( keys.contains( key ) ) {
          retValue.add( key );
        }
      }
      return retValue;
    }
    return new ElemArrayList<>( source.keySet() );
  }

  @Override
  public IList<E> values() {
    if( keysFilter != null ) {
      // Фильтрация значений карты
      Set<K> keys = source.keySet();
      IListEdit<E> retValue = new ElemArrayList<>( keysFilter.size() );
      for( K key : keysFilter ) {
        if( keys.contains( key ) ) {
          retValue.add( source.get( key ) );
        }
      }
      return retValue;
    }
    return new ElemArrayList<>( source.values() );
  }

  @Override
  public Iterator<E> iterator() {
    return values().iterator();
  }

  @Override
  public boolean isEmpty() {
    return (size() == 0);
  }

  @Override
  public int size() {
    if( keysFilter != null ) {
      // Фильтрация значений карты
      Set<K> keys = source.keySet();
      int retValue = 0;
      for( K key : keysFilter ) {
        if( keys.contains( key ) ) {
          retValue++;
        }
      }
      return retValue;
    }
    return source.size();
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IMapEdit<K,E>
  //
  @Override
  public void clear() {
    source.clear();
  }

  @Override
  public E put( K aKey, E aElem ) {
    TsNullArgumentRtException.checkNulls( aKey, aElem );
    return source.put( aKey, aElem );
  }

  @Override
  public E removeByKey( K aKey ) {
    TsNullArgumentRtException.checkNull( aKey );
    return source.remove( aKey );
  }

  @Override
  public void putAll( IMap<K, ? extends E> aSrc ) {
    TsNullArgumentRtException.checkNull( aSrc );
    for( K key : aSrc.keys() ) {
      source.put( key, aSrc.getByKey( key ) );
    }
  }

  @Override
  public void setAll( IMap<K, ? extends E> aSrc ) {
    TsNullArgumentRtException.checkNull( aSrc );
    clear();
    putAll( aSrc );
  }

  @Override
  public E[] toArray( E[] aSrcArray ) {
    return source.values().toArray( aSrcArray );
  }

  @Override
  public Object[] toArray() {
    return source.values().toArray();
  }
}
