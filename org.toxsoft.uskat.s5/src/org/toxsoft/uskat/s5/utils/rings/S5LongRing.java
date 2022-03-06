package org.toxsoft.uskat.s5.utils.rings;

import java.util.Arrays;

import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Кольцо длиных целочисленных значений
 *
 * @author mvk
 */
public final class S5LongRing
    extends S5AbstractRing {

  private static final long serialVersionUID = 157157L;

  private final long values[];

  /**
   * Конструктор
   *
   * @param aCapacity int максимально возможное количество элементов кольца ( мощность) кольца
   * @throws TsIllegalArgumentRtException aCapacity <= 0
   */
  public S5LongRing( int aCapacity ) {
    super( aCapacity );
    values = new long[aCapacity];
    clear();
  }

  /**
   * Конструктор копирования
   *
   * @param aSource {@link S5AbstractRing} кольцо-источник
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5LongRing( S5LongRing aSource ) {
    super( aSource );
    values = Arrays.copyOf( aSource.values, aSource.capacity() );
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Возвращает значение по указанному индексу.
   *
   * @param aIndex int индекс значения. 0: последнее добавленное
   * @return long значение
   * @throws TsIllegalArgumentRtException недопустимый индекс
   */
  public long get( int aIndex ) {
    return values[index( aIndex )];
  }

  /**
   * Возвращает минимальное значение в кольце.
   *
   * @return long значение
   * @throws TsIllegalStateRtException в кольце нет данных
   */
  public long min() {
    TsIllegalStateRtException.checkFalse( size() > 0 );
    long retValue = values[index( 0 )];
    for( int index = 1, n = size(); index < n; index++ ) {
      long value = values[index( index )];
      if( value < retValue ) {
        retValue = value;
      }
    }
    return retValue;
  }

  /**
   * Возвращает максимальное значение в кольце.
   *
   * @return long значение
   * @throws TsIllegalStateRtException в кольце нет данных
   */
  public long max() {
    TsIllegalStateRtException.checkFalse( size() > 0 );
    long retValue = values[index( 0 )];
    for( int index = 1, n = size(); index < n; index++ ) {
      long value = values[index( index )];
      if( value > retValue ) {
        retValue = value;
      }
    }
    return retValue;
  }

  /**
   * Возвращает среднее значение в кольце
   *
   * @return long значение
   * @throws TsIllegalStateRtException в кольце нет данных
   */
  public long ave() {
    TsIllegalStateRtException.checkFalse( size() > 0 );
    long summa = 0;
    for( int index = 0, n = size(); index < n; index++ ) {
      summa += values[index( index )];
    }
    return summa / size();
  }

  /**
   * Добавление нового значения в кольцо
   *
   * @param aValue long добавляемое значение
   */
  public void add( long aValue ) {
    values[newIndex()] = aValue;
  }

  /**
   * Очистить кольцо
   */
  @Override
  public void clear() {
    super.clear();
    Arrays.fill( values, 0 );
  }
}
