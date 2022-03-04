package org.toxsoft.uskat.s5.utils.indexes;

import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;

/**
 * Индекс произвольного значения по целочисленному ключу
 *
 * @author mvk
 * @param <V> тип значения
 */
public interface IIntElemIndex<V>
    extends IIntKey, IIndex<Integer, V> {

  /**
   * Возвращает значение по ключу. Если ключа нет в индексе, то возвращается значение ближайшее к нему.
   * <p>
   * Для неуникальных индексов ({@link #unique()} == false),при наличии нескольких одинаковых ключей, возвращается
   * значение по наименьшему индексу найденного ключа
   *
   * @param aKey int ключ
   * @return V значение. null: пустой индекс
   */
  V findValue( int aKey );

  /**
   * Возвращает значение по ключу.
   * <p>
   * Для неуникальных индексов ({@link #unique()} == false),при наличии нескольких одинаковых ключей, возвращается
   * значение по наименьшему индексу найденного ключа
   *
   * @param aKey int ключ
   * @return V значение
   * @throws TsIllegalArgumentRtException ключа нет в индексе
   */
  V getValue( int aKey );

  /**
   * Возвращает все значения
   *
   * @return V[] все значения ввиде массива
   */
  V[] values();
}
