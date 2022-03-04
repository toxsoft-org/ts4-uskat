package org.toxsoft.uskat.s5.utils.indexes;

import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;

/**
 * Индекс произвольного значения по строковому ключу
 *
 * @author mvk
 * @param <V> тип значения
 */
public interface IStringElemIndex<V>
    extends IStringKey, IIndex<String, V> {

  /**
   * Возвращает значение по ключу. Если ключа нет в индексе, то возвращается значение ближайшее к нему.
   * <p>
   * Для неуникальных индексов ({@link #unique()} == false),при наличии нескольких одинаковых ключей, возвращается
   * значение по наименьшему индексу найденного ключа
   *
   * @param aKey String ключ
   * @return V значение. null: пустой индекс
   */
  @Override
  V findValue( String aKey );

  /**
   * Возвращает значение по ключу.
   * <p>
   * Для неуникальных индексов ({@link #unique()} == false),при наличии нескольких одинаковых ключей, возвращается
   * значение по наименьшему индексу найденного ключа
   *
   * @param aKey String ключ
   * @return V значение
   * @throws TsIllegalArgumentRtException ключа нет в индексе
   */
  @Override
  V getValue( String aKey );
}
