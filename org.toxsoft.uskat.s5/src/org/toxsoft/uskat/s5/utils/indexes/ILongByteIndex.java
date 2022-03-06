package org.toxsoft.uskat.s5.utils.indexes;

import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Индекс байтового значения по длинному целочисленному ключу
 *
 * @author mvk
 */
public interface ILongByteIndex
    extends ILongKey, IIndex<Long, Byte> {

  /**
   * Возвращает значение в индексе
   * <p>
   * Реализация метода является высокопроизводительной
   *
   * @param aIndex int индекс значения
   * @return byte значение
   * @throws TsIllegalArgumentRtException неверный индекс значения
   */
  byte byteValue( int aIndex );

  /**
   * Возвращает значение по ключу. Если ключа нет в индексе, то возвращается значение ближайшее к нему.
   * <p>
   * Для неуникальных индексов ({@link #unique()} == false),при наличии нескольких одинаковых ключей, возвращается
   * значение по наименьшему индексу найденного ключа
   *
   * @param aKey long ключ
   * @return byte значение. При пустом индексе возвращается 0
   * @throws TsNullArgumentRtException аргумент = null
   */
  byte findValue( long aKey );

  /**
   * Возвращает значение по ключу.
   * <p>
   * Для неуникальных индексов ({@link #unique()} == false),при наличии нескольких одинаковых ключей, возвращается
   * значение по наименьшему индексу найденного ключа
   *
   * @param aKey long ключ
   * @return byte значение
   * @throws TsIllegalArgumentRtException ключа нет в индексе
   */
  byte getValue( long aKey );
}
