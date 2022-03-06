package org.toxsoft.uskat.s5.utils.rings;

import java.util.Arrays;

import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Кольцо длиных целочисленных значений
 *
 * @author mvk
 */
public final class S5IntegerRing
    extends S5AbstractRing {

  private static final long serialVersionUID = 157157L;

  private final int values[];

  /**
   * Конструктор
   *
   * @param aCapacity int максимально возможное количество элементов кольца ( мощность) кольца
   * @throws TsIllegalArgumentRtException aCapacity <= 0
   */
  public S5IntegerRing( int aCapacity ) {
    super( aCapacity );
    values = new int[aCapacity];
    clear();
  }

  /**
   * Конструктор копирования
   *
   * @param aSource {@link S5AbstractRing} кольцо-источник
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5IntegerRing( S5IntegerRing aSource ) {
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
   * @return int значение
   * @throws TsIllegalArgumentRtException недопустимый индекс
   */
  public int get( int aIndex ) {
    return values[index( aIndex )];
  }

  /**
   * Возвращает минимальное значение в кольце.
   *
   * @return int значение
   * @throws TsIllegalStateRtException в кольце нет данных
   */
  public int min() {
    TsIllegalStateRtException.checkFalse( size() > 0 );
    int retValue = values[index( 0 )];
    for( int index = 1, n = size(); index < n; index++ ) {
      int value = values[index( index )];
      if( value < retValue ) {
        retValue = value;
      }
    }
    return retValue;
  }

  /**
   * Возвращает максимальное значение в кольце.
   *
   * @return int значение
   * @throws TsIllegalStateRtException в кольце нет данных
   */
  public int max() {
    TsIllegalStateRtException.checkFalse( size() > 0 );
    int retValue = values[index( 0 )];
    for( int index = 1, n = size(); index < n; index++ ) {
      int value = values[index( index )];
      if( value > retValue ) {
        retValue = value;
      }
    }
    return retValue;
  }

  /**
   * Возвращает среднее значение в кольце
   *
   * @return int значение
   * @throws TsIllegalStateRtException в кольце нет данных
   */
  public int ave() {
    TsIllegalStateRtException.checkFalse( size() > 0 );
    int summa = 0;
    for( int index = 0, n = size(); index < n; index++ ) {
      summa += values[index( index )];
    }
    return summa / size();
  }

  /**
   * Добавление нового значения в кольцо
   *
   * @param aValue int добавляемое значение
   */
  public void add( int aValue ) {
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
