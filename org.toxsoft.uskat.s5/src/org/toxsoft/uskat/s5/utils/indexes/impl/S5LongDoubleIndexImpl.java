package org.toxsoft.uskat.s5.utils.indexes.impl;

import static org.toxsoft.uskat.s5.utils.indexes.impl.IS5Resources.*;

import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.utils.indexes.ILongDoubleIndexEdit;

/**
 * Бинарный индекс вещественного(double) значения по длинному целочисленному ключу
 *
 * @author mvk
 */
class S5LongDoubleIndexImpl
    extends S5AbstractIndex<Long, Double>
    implements ILongDoubleIndexEdit {

  private static final long serialVersionUID = 157157L;

  private final long[]   keys;
  private final double[] values;

  /**
   * Конструктор
   *
   * @param aUnique <b>true</b> индекс с уникальными ключами;<b>false</b> индекс с НЕуникальными ключами
   * @param aKeys long[] массив ключей индекса
   * @param aValues double[] массив значений индекса
   * @param aRestore boolean <b>true</b> восстановление индекса; <b>false</b> формирование нового индекса.
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   */
  S5LongDoubleIndexImpl( boolean aUnique, long[] aKeys, double[] aValues, boolean aRestore ) {
    super( aUnique, (aRestore ? aKeys.length : 0) );
    TsNullArgumentRtException.checkNulls( aKeys, aValues );
    checkSize( aKeys.length, aValues.length );
    keys = aKeys;
    values = aValues;
  }

  // ------------------------------------------------------------------------------------
  // Реализация ILongDoubleIndexEdit
  //
  @Override
  public int capacity() {
    return keys.length;
  }

  @Override
  public void check() {
    for( int index = 1, n = watermark; index < n; index++ ) {
      if( keys[index - 1] > keys[index] ) {
        // Нарушение индекса
        Long indexL = Long.valueOf( index );
        String key0 = String.valueOf( keys[index] );
        String key1 = String.valueOf( keys[index - 1] );
        throw new TsIllegalStateRtException( MSG_ERR_INDEX_INTEGRITY, indexL, key0, key1 );
      }
    }
  }

  @Override
  public void add( long[] aKeys, double[] aValues ) {
    TsNullArgumentRtException.checkNulls( aKeys, aValues );
    checkSize( aKeys.length, aValues.length );
    for( int index = 0, n = aKeys.length; index < n; index++ ) {
      add( aKeys[index], aValues[index] );
    }
  }

  @Override
  public void add( Long aKey, Double aValue ) {
    TsNullArgumentRtException.checkNulls( aKey, aValue );
    add( aKey.intValue(), aValue.doubleValue() );
  }

  @Override
  public void add( long aKey, double aValue ) {
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
    long nearestKey = keys[nearest];
    if( unique && nearestKey == aKey ) {
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
    while( nearestKey <= aKey && nearest < watermark ) {
      // Ближайший ключ оказался меньше или равный ключу значения, вставка за ним
      nearest++;
      nearestKey = keys[nearest];
    }
    // Если добавление идет в середину уже сформированного индекса, то необходимо провести сдвиг в "хвост"
    if( nearest < (watermark - 1) || (nearest == (watermark - 1) && nearestKey > aKey) ) {
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
  public Long key( int aIndex ) {
    if( aIndex < 0 || aIndex >= watermark ) {
      Long keyIndex = Long.valueOf( aIndex );
      throw new TsIllegalArgumentRtException( MSG_ERR_INDEX_KEY_OUT, keyIndex, Long.valueOf( watermark ) );
    }
    return Long.valueOf( keys[aIndex] );
  }

  @Override
  public long longKey( int aIndex ) {
    if( aIndex < 0 || aIndex >= watermark ) {
      Long keyIndex = Long.valueOf( aIndex );
      throw new TsIllegalArgumentRtException( MSG_ERR_INDEX_KEY_OUT, keyIndex, Long.valueOf( watermark ) );
    }
    return keys[aIndex];
  }

  @Override
  public Double value( int aIndex ) {
    if( aIndex < 0 || aIndex >= watermark ) {
      Long valueIndex = Long.valueOf( aIndex );
      throw new TsIllegalArgumentRtException( MSG_ERR_INDEX_VALUE_OUT, valueIndex, Long.valueOf( watermark ) );
    }
    return Double.valueOf( values[aIndex] );
  }

  @Override
  public double doubleValue( int aIndex ) {
    if( aIndex < 0 || aIndex >= watermark ) {
      Long valueIndex = Long.valueOf( aIndex );
      throw new TsIllegalArgumentRtException( MSG_ERR_INDEX_VALUE_OUT, valueIndex, Long.valueOf( watermark ) );
    }
    return values[aIndex];
  }

  @Override
  public int findIndex( Long aKey ) {
    return findIndex( aKey.longValue() );
  }

  @Override
  public int findIndex( long aKey ) {
    return binarySearch( aKey, false );
  }

  @Override
  public int getIndex( Long aKey ) {
    return getIndex( aKey.longValue() );
  }

  @Override
  public int getIndex( long aKey ) {
    int index = findIndex( aKey );
    if( index < 0 || keys[index] != aKey ) {
      // Ключ не найден
      String key = String.valueOf( aKey );
      throw new TsIllegalArgumentRtException( MSG_ERR_INDEX_NOT_FOUND, key );
    }
    return index;
  }

  @Override
  public Double findValue( Long aKey ) {
    return Double.valueOf( findValue( aKey.longValue() ) );
  }

  @Override
  public double findValue( long aKey ) {
    int index = findIndex( aKey );
    if( index < 0 ) {
      return Double.MIN_VALUE;
    }
    return values[index];
  }

  @Override
  public Double getValue( Long aKey ) {
    return Double.valueOf( getValue( aKey.longValue() ) );
  }

  @Override
  public double getValue( long aKey ) {
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
   * @param aKey long ключ индекса.
   * @param aStrict boolean <b>true</b> искать полное соответствие ключа; <b>false</b> разрешено ближайшие индексы
   * @return индекса ключа (ключ полностью совпал) или индекс ближайшего (при aStrict = false). < 0: индекс не найден.
   */
  private int binarySearch( long aKey, boolean aStrict ) {
    if( watermark == 0 ) {
      // Пустой индекс
      return -1;
    }
    int lowIndex = 0;
    int highIndex = watermark - 1;
    while( lowIndex <= highIndex ) {
      int middleIndex = lowIndex + (highIndex - lowIndex) / 2;
      long key = keys[middleIndex];
      if( aKey < key ) {
        // Смещение к началу
        highIndex = middleIndex - 1;
        continue;
      }
      if( aKey > key ) {
        // Смещение к концу
        if( lowIndex == watermark - 1 ) {
          // Смещение к концу уже невозможно (выход за границы)
          break;
        }
        lowIndex = middleIndex + 1;
        continue;
      }
      // Точное совпадение. Сдиг на первый элемент
      while( middleIndex > 0 && aKey == keys[middleIndex - 1] ) {
        middleIndex--;
      }
      return middleIndex;
    }
    // Точного совпадения нет. Если разрешено, то возвращаем индекс ближайшего значения
    return (aStrict ? -1 : lowIndex);
  }
}
