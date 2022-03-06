package org.toxsoft.uskat.s5.utils.indexes;

import org.toxsoft.core.tslib.coll.primtypes.ILongList;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Индекс длинного целочисленного значения по длинному целочисленному ключу с возможностью редактирования
 *
 * @author mvk
 */
public interface ILongLongIndexEdit
    extends ILongLongIndex, IIndexEdit<Long, Long> {

  /**
   * Добавляет в индекс указанный массив ключей и их значений
   * <p>
   * Если индекс уникальный ({@link #unique()} == true) и в индексе уже есть значения под указанным ключами, то они
   * будут замещены новыми значениями.
   *
   * @param aKeys {@link ILongList} ключи индекса
   * @param aValues {@link ILongList} значения индекса
   * @throws TsNullArgumentRtException любой аргумент null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsInternalErrorRtException переполнение индекса
   */
  void add( ILongList aKeys, ILongList aValues );

  /**
   * Добавляет в индекс указанный массив ключей и их значений
   * <p>
   * Если индекс уникальный ({@link #unique()} == true) и в индексе уже есть значения под указанным ключами, то они
   * будут замещены новыми значениями.
   *
   * @param aKeys long[] ключи индекса
   * @param aValues long[] значения индекса
   * @throws TsNullArgumentRtException любой аргумент null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsInternalErrorRtException переполнение индекса
   */
  void add( long[] aKeys, long[] aValues );

  /**
   * Добавляет в индекс ключ и его значение
   * <p>
   * Если индекс уникальный ({@link #unique()} == true) и в индексе уже есть значение под указанным ключом, то оно будет
   * замещено новым значением.
   *
   * @param aKey long ключ
   * @param aValue long значение ключа
   * @throws TsInternalErrorRtException переполнение индекса
   */
  void add( long aKey, long aValue );
}
