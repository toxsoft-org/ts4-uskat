package org.toxsoft.uskat.s5.utils.indexes.impl;

import static org.toxsoft.uskat.s5.utils.indexes.impl.IS5Resources.*;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.utils.indexes.IStringElemIndexEdit;

/**
 * Бинарный индекс произвольного значения по целочисленному ключу
 *
 * @author mvk
 * @param <V> тип значений
 */
class StringElemIndexImpl<V>
    extends S5AbstractIndex<String, V>
    implements IStringElemIndexEdit<V> {

  private static final long serialVersionUID = 157157L;

  private final String[] keys;
  private final V[]      values;

  /**
   * Конструктор
   *
   * @param aUnique <b>true</b> индекс с уникальными ключами;<b>false</b> индекс с НЕуникальными ключами
   * @param aKeys String[] массив ключей индекса
   * @param aValues V[] массив значений индекса
   * @param aRestore boolean <b>true</b> восстановление индекса; <b>false</b> формирование нового индекса.
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   */
  StringElemIndexImpl( boolean aUnique, String[] aKeys, V[] aValues, boolean aRestore ) {
    super( aUnique, (aRestore ? aKeys.length : 0) );
    TsNullArgumentRtException.checkNulls( aKeys, aValues );
    checkSize( aKeys.length, aValues.length );
    keys = aKeys;
    values = aValues;
  }

  // ------------------------------------------------------------------------------------
  // Реализация IStringElemIndexEdit
  //
  @Override
  public int capacity() {
    return keys.length;
  }

  @Override
  public void check() {
    for( int index = 1, n = watermark; index < n; index++ ) {
      if( keys[index - 1].compareTo( keys[index] ) > 0 ) {
        // Нарушение индекса
        Integer indexI = Integer.valueOf( index );
        String key0 = String.valueOf( keys[index] );
        String key1 = String.valueOf( keys[index - 1] );
        throw new TsIllegalStateRtException( MSG_ERR_INDEX_INTEGRITY, indexI, key0, key1 );
      }
      if( unique && keys[index - 1] == keys[index] ) {
        // Нарушение уникального индекса
        Integer indexI = Integer.valueOf( index );
        String key0 = String.valueOf( keys[index] );
        String key1 = String.valueOf( keys[index - 1] );
        throw new TsIllegalStateRtException( MSG_ERR_UNIQUE_INDEX_INTEGRITY, indexI, key0, key1 );
      }
    }
  }

  @Override
  public void add( IStringList aKeys, IList<V> aValues ) {
    TsNullArgumentRtException.checkNulls( aKeys, aValues );
    checkSize( aKeys.size(), aValues.size() );
    for( int index = 0, n = aKeys.size(); index < n; index++ ) {
      add( aKeys.get( index ), aValues.get( index ) );
    }
  }

  @Override
  public void add( String[] aKeys, V[] aValues ) {
    TsNullArgumentRtException.checkNulls( aKeys, aValues );
    checkSize( aKeys.length, aValues.length );
    for( int index = 0, n = aKeys.length; index < n; index++ ) {
      add( aKeys[index], aValues[index] );
    }
  }

  @Override
  public void add( String aKey, V aValue ) {
    TsNullArgumentRtException.checkNull( aKey );
    // Признак того что формирование индекса завершено.
    boolean completed = watermark == keys.length;
    if( !unique && completed ) {
      // Переполнение НЕуникального индекса
      Long keysLength = Long.valueOf( keys.length );
      Long watermarkL = Long.valueOf( watermark );
      throw new TsInternalErrorRtException( MSG_ERR_INDEX_OVER, getClass().getSimpleName(), keysLength, watermarkL );
    }
    // Индекс ближайшего элемента в котором будет установлено значение
    int nearest = binarySearch( aKey, false );
    if( nearest < 0 ) {
      // Пустой индекс
      keys[0] = aKey;
      values[0] = aValue;
      watermark++;
      return;
    }
    // Ключ по найденному индексу
    String nearestKey = keys[nearest];
    if( unique && nearestKey.compareTo( aKey ) == 0 ) {
      // На уникальном индексе найденый ключ оказался равным ключу значения. Замещение значения
      values[nearest] = aValue;
      return;
    }
    if( completed ) {
      // Переполнение уникального индекса
      String indexClassName = getClass().getSimpleName();
      Long keysLength = Long.valueOf( keys.length );
      Long watermarkL = Long.valueOf( watermark );
      throw new TsInternalErrorRtException( MSG_ERR_UNIQUE_INDEX_OVER, indexClassName, keysLength, watermarkL );
    }
    while( nearestKey.compareTo( aKey ) <= 0 && nearest < watermark ) {
      // Ближайший ключ оказался меньше или равный ключу значения, вставка за ним
      nearest++;
      nearestKey = keys[nearest];
    }

    // Если добавление идет в середину уже сформированного индекса, то необходимо провести сдвиг в "хвост"
    if( nearest < (watermark - 1) || (nearest == (watermark - 1) && nearestKey.compareTo( aKey ) > 0) ) {
      int shifting = watermark + 1 - nearest - 1;
      if( shifting > 0 ) {
        System.arraycopy( keys, nearest, keys, nearest + 1, shifting );
        System.arraycopy( values, nearest, values, nearest + 1, shifting );
        // System.out.println( "watermark: " + watermark + ", nearest: " + nearest + ", shifting: " + shifting );
      }
    }
    // Установка значения по индексу
    keys[nearest] = aKey;
    values[nearest] = aValue;
    watermark++;
  }

  @Override
  public String key( int aIndex ) {
    if( aIndex < 0 || aIndex >= watermark ) {
      Integer keyIndex = Integer.valueOf( aIndex );
      throw new TsIllegalArgumentRtException( MSG_ERR_INDEX_KEY_OUT, keyIndex, Integer.valueOf( watermark ) );
    }
    return keys[aIndex];
  }

  @Override
  public V value( int aIndex ) {
    if( aIndex < 0 || aIndex >= watermark ) {
      Integer valueIndex = Integer.valueOf( aIndex );
      throw new TsIllegalArgumentRtException( MSG_ERR_INDEX_VALUE_OUT, valueIndex, Integer.valueOf( watermark ) );
    }
    return values[aIndex];
  }

  @Override
  public int findIndex( String aKey ) {
    return binarySearch( aKey, false );
  }

  @Override
  public int getIndex( String aKey ) {
    int index = findIndex( aKey );
    if( index < 0 || keys[index] != aKey ) {
      // Ключ не найден
      String key = String.valueOf( aKey );
      throw new TsIllegalArgumentRtException( MSG_ERR_INDEX_NOT_FOUND, key );
    }
    return index;
  }

  @Override
  public V findValue( String aKey ) {
    int index = findIndex( aKey );
    if( index < 0 ) {
      return null;
    }
    return values[index];
  }

  @Override
  public V getValue( String aKey ) {
    int index = findIndex( aKey );
    if( index < 0 || keys[index] != aKey ) {
      // Ключ не найден
      String key = String.valueOf( aKey );
      throw new TsIllegalArgumentRtException( MSG_ERR_INDEX_NOT_FOUND, key );
    }
    return values[index];
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * С помощью бинарного поиска находит индекс значения по ключу.
   * <p>
   * Клиент должен проверять ключ по найденному индексу: он может быть меньше, больше или равной заданному ключу.
   *
   * @param aKey String ключ индекса.
   * @param aStrict boolean <b>true</b> искать полное соответствие ключа; <b>false</b> разрешено ближайшие индексы
   * @return индекса ключа (ключ полностью совпал) или индекс ближайшего (при aStrict = false). < 0: индекс не найден.
   */
  private int binarySearch( String aKey, boolean aStrict ) {
    if( watermark == 0 ) {
      // Пустой индекс
      return -1;
    }
    int lowIndex = 0;
    int highIndex = watermark - 1;
    while( lowIndex <= highIndex ) {
      int middleIndex = lowIndex + (highIndex - lowIndex) / 2;
      String key = keys[middleIndex];
      if( aKey.compareTo( key ) < 0 ) {
        // Смещение к началу
        highIndex = middleIndex - 1;
        continue;
      }
      if( aKey.compareTo( key ) > 0 ) {
        // Смещение к концу
        if( lowIndex == watermark - 1 ) {
          // Смещение к концу уже невозможно (выход за границы)
          break;
        }
        lowIndex = middleIndex + 1;
        continue;
      }
      // Точное совпадение. Сдиг на первый элемент
      while( middleIndex > 0 && aKey.compareTo( keys[middleIndex - 1] ) == 0 ) {
        middleIndex--;
      }
      return middleIndex;
    }
    // Точного совпадения нет. Если разрешено, то возвращаем индекс ближайшего значения
    return (aStrict ? -1 : lowIndex);
  }
}
