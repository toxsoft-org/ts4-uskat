package org.toxsoft.uskat.s5.utils.indexes;

import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;

/**
 * Индекс длинного целочисленного значения по длинному целочисленному ключу
 *
 * @author mvk
 */
public interface ILongLongIndex
    extends ILongKey, IIndex<Long, Long> {

  /**
   * Возвращает значение в индексе
   * <p>
   * Реализация метода является высокопроизводительной
   *
   * @param aIndex int индекс значения
   * @return long значение
   * @throws TsIllegalArgumentRtException неверный индекс значения
   */
  long longValue( int aIndex );

  /**
   * Возвращает значение по ключу. Если ключа нет в индексе, то возвращается значение ближайшее к нему.
   * <p>
   * Для неуникальных индексов ({@link #unique()} == false),при наличии нескольких одинаковых ключей, возвращается
   * значение по наименьшему индексу найденного ключа
   *
   * @param aKey long ключ
   * @return long значение. {@link Long#MIN_VALUE}: пустой индекс
   */
  long findValue( long aKey );

  /**
   * Возвращает значение по ключу.
   * <p>
   * Для неуникальных индексов ({@link #unique()} == false),при наличии нескольких одинаковых ключей, возвращается
   * значение по наименьшему индексу найденного ключа
   *
   * @param aKey long ключ
   * @return long значение
   * @throws TsIllegalArgumentRtException ключа нет в индексе
   */
  long getValue( long aKey );
}
