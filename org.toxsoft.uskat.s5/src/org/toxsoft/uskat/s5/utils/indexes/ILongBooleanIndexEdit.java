package org.toxsoft.uskat.s5.utils.indexes;

import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Индекс логического значения по длинному целочисленному ключу с возможностью редактирования
 *
 * @author mvk
 */
public interface ILongBooleanIndexEdit
    extends ILongBooleanIndex, IIndexEdit<Long, Boolean> {

  /**
   * Добавляет в индекс указанный массив ключей и их значений
   *
   * @param aKeys long[] ключи индекса
   * @param aValues boolean[] значения индекса
   * @throws TsNullArgumentRtException любой аргумент null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsInternalErrorRtException переполнение индекса
   */
  void add( long[] aKeys, boolean[] aValues );

  /**
   * Добавляет в индекс ключ и его значение
   *
   * @param aKey long ключ
   * @param aValue boolean значение ключа
   * @throws TsInternalErrorRtException переполнение индекса
   */
  void add( long aKey, boolean aValue );
}
