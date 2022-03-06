package org.toxsoft.uskat.s5.utils.indexes;

import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;

/**
 * Индекс целочисленного значения по длинному целочисленному ключу
 *
 * @author mvk
 */
public interface ILongIntIndex
    extends ILongKey, IIndex<Long, Integer> {

  /**
   * Возвращает значение в индексе
   * <p>
   * Реализация метода является высокопроизводительной
   *
   * @param aIndex int индекс значения
   * @return int значение
   * @throws TsIllegalArgumentRtException неверный индекс значения
   */
  int intValue( int aIndex );

  /**
   * Возвращает значение по ключу. Если ключа нет в индексе, то возвращается значение ближайшее к нему.
   * <p>
   * Для неуникальных индексов ({@link #unique()} == false),при наличии нескольких одинаковых ключей, возвращается
   * значение по наименьшему индексу найденного ключа
   *
   * @param aKey long ключ
   * @return int значение. {@link Integer#MIN_VALUE}: пустой индекс
   */
  int findValue( long aKey );

  /**
   * Возвращает значение по ключу.
   * <p>
   * Для неуникальных индексов ({@link #unique()} == false),при наличии нескольких одинаковых ключей, возвращается
   * значение по наименьшему индексу найденного ключа
   *
   * @param aKey long ключ
   * @return int значение
   * @throws TsIllegalArgumentRtException ключа нет в индексе
   */
  int getValue( long aKey );
}
