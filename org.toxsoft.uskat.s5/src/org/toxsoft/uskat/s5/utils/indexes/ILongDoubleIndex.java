package org.toxsoft.uskat.s5.utils.indexes;

import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Индекс вещественного(double) значения по длинному целочисленному ключу
 *
 * @author mvk
 */
public interface ILongDoubleIndex
    extends ILongKey, IIndex<Long, Double> {

  /**
   * Возвращает значение в индексе
   * <p>
   * Реализация метода является высокопроизводительной
   *
   * @param aIndex int индекс значения
   * @return double значение
   * @throws TsIllegalArgumentRtException неверный индекс значения
   */
  double doubleValue( int aIndex );

  /**
   * Возвращает значение по ключу. Если ключа нет в индексе, то возвращается значение ближайшее к нему.
   * <p>
   * Для неуникальных индексов ({@link #unique()} == false),при наличии нескольких одинаковых ключей, возвращается
   * значение по наименьшему индексу найденного ключа
   *
   * @param aKey long ключ
   * @return double значение. При пустом индексе возвращается {@link Double#MIN_VALUE}
   * @throws TsNullArgumentRtException аргумент = null
   */
  double findValue( long aKey );

  /**
   * Возвращает значение по ключу.
   * <p>
   * Для неуникальных индексов ({@link #unique()} == false),при наличии нескольких одинаковых ключей, возвращается
   * значение по наименьшему индексу найденного ключа
   *
   * @param aKey long ключ
   * @return double значение
   * @throws TsIllegalArgumentRtException ключа нет в индексе
   */
  double getValue( long aKey );
}
