package org.toxsoft.uskat.s5.utils.indexes;

import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Индекс булевского значения по длинному целочисленному ключу
 *
 * @author mvk
 */
public interface ILongBooleanIndex
    extends ILongKey, IIndex<Long, Boolean> {

  /**
   * Возвращает значение в индексе
   * <p>
   * Реализация метода является высокопроизводительной
   *
   * @param aIndex int индекс значения
   * @return boolean значение
   * @throws TsIllegalArgumentRtException неверный индекс значения
   */
  boolean booleanValue( int aIndex );

  /**
   * Возвращает значение по ключу. Если ключа нет в индексе, то возвращается значение ближайшее к нему.
   * <p>
   * Для неуникальных индексов ({@link #unique()} == false),при наличии нескольких одинаковых ключей, возвращается
   * значение по наименьшему индексу найденного ключа
   *
   * @param aKey long ключ
   * @return boolean значение. При пустом индексе возвращается {@link Boolean#FALSE}
   * @throws TsNullArgumentRtException аргумент = null
   */
  boolean findValue( long aKey );

  /**
   * Возвращает значение по ключу.
   * <p>
   * Для неуникальных индексов ({@link #unique()} == false),при наличии нескольких одинаковых ключей, возвращается
   * значение по наименьшему индексу найденного ключа
   *
   * @param aKey long ключ
   * @return boolean значение
   * @throws TsIllegalArgumentRtException ключа нет в индексе
   */
  boolean getValue( long aKey );
}
