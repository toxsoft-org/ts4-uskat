package org.toxsoft.uskat.s5.utils.indexes;

import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;

/**
 * Индекс произвольного значения по длинному целочисленному ключу
 *
 * @author mvk
 * @param <V> тип значения
 */
public interface ILongElemIndex<V>
    extends ILongKey, IIndex<Long, V> {

  /**
   * Возвращает значение по ключу. Если ключа нет в индексе, то возвращается значение ближайшее к нему.
   * <p>
   * Для неуникальных индексов ({@link #unique()} == false),при наличии нескольких одинаковых ключей, возвращается
   * значение по наименьшему индексу найденного ключа
   *
   * @param aKey long ключ
   * @return V значение. null: пустой индекс
   */
  V findValue( long aKey );

  /**
   * Возвращает значение по ключу.
   * <p>
   * Для неуникальных индексов ({@link #unique()} == false),при наличии нескольких одинаковых ключей, возвращается
   * значение по наименьшему индексу найденного ключа
   *
   * @param aKey long ключ
   * @return V значение
   * @throws TsIllegalArgumentRtException ключа нет в индексе
   */
  V getValue( long aKey );
}
