package org.toxsoft.uskat.s5.utils.indexes;

import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Индекс байтового значения по длинному целочисленному ключу с возможностью редактирования
 *
 * @author mvk
 */
public interface ILongByteIndexEdit
    extends ILongByteIndex, IIndexEdit<Long, Byte> {

  /**
   * Добавляет в индекс указанный массив ключей и их значений
   *
   * @param aKeys long[] ключи индекса
   * @param aValues byte[] значения индекса
   * @throws TsNullArgumentRtException любой аргумент null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsInternalErrorRtException переполнение индекса
   */
  void add( long[] aKeys, byte[] aValues );

  /**
   * Добавляет в индекс ключ и его значение
   *
   * @param aKey long ключ
   * @param aValue byte значение ключа
   * @throws TsInternalErrorRtException переполнение индекса
   */
  void add( long aKey, byte aValue );
}
