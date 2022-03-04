package org.toxsoft.uskat.s5.utils.rings;

import java.util.Arrays;

import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Кольцо значений значений с плавающей запятой
 *
 * @author mvk
 */
public final class S5DoubleRing
    extends S5AbstractRing {

  private static final long serialVersionUID = 157157L;

  private final double values[];

  /**
   * Конструктор
   *
   * @param aCapacity int максимально возможное количество элементов кольца ( мощность) кольца
   * @throws TsIllegalArgumentRtException aCapacity <= 0
   */
  public S5DoubleRing( int aCapacity ) {
    super( aCapacity );
    values = new double[aCapacity];
    clear();
  }

  /**
   * Конструктор копирования
   *
   * @param aSource {@link S5AbstractRing} кольцо-источник
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5DoubleRing( S5DoubleRing aSource ) {
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
   * @return double значение
   * @throws TsIllegalArgumentRtException недопустимый индекс
   */
  public double get( int aIndex ) {
    return values[index( aIndex )];
  }

  /**
   * Возвращает минимальное значение в кольце.
   *
   * @return double значение
   * @throws TsIllegalStateRtException в кольце нет данных
   */
  public double min() {
    TsIllegalStateRtException.checkFalse( size() > 0 );
    double retValue = values[index( 0 )];
    for( int index = 1, n = size(); index < n; index++ ) {
      double value = values[index( index )];
      if( value < retValue ) {
        retValue = value;
      }
    }
    return retValue;
  }

  /**
   * Возвращает максимальное значение в кольце.
   *
   * @return double значение
   * @throws TsIllegalStateRtException в кольце нет данных
   */
  public double max() {
    TsIllegalStateRtException.checkFalse( size() > 0 );
    double retValue = values[index( 0 )];
    for( int index = 1, n = size(); index < n; index++ ) {
      double value = values[index( index )];
      if( value > retValue ) {
        retValue = value;
      }
    }
    return retValue;
  }

  /**
   * Возвращает среднее значение в кольце
   *
   * @return double значение
   * @throws TsIllegalStateRtException в кольце нет данных
   */
  public double ave() {
    TsIllegalStateRtException.checkFalse( size() > 0 );
    double summa = 0;
    for( int index = 0, n = size(); index < n; index++ ) {
      summa += values[index( index )];
    }
    return summa / size();
  }

  /**
   * Добавление нового значения в кольцо
   *
   * @param aValue double добавляемое значение
   */
  public void add( double aValue ) {
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
