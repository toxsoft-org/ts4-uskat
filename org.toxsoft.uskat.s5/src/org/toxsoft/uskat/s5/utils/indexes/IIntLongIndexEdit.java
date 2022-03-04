package org.toxsoft.uskat.s5.utils.indexes;

import org.toxsoft.core.tslib.coll.primtypes.IIntList;
import org.toxsoft.core.tslib.coll.primtypes.ILongList;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Индекс длинного целочисленного значения по целочисленному ключу с возможностью редактирования
 *
 * @author mvk
 */
public interface IIntLongIndexEdit
    extends IIntLongIndex, IIndexEdit<Integer, Long> {

  /**
   * Добавляет в индекс указанный массив ключей и их значений
   * <p>
   * Если индекс уникальный ({@link #unique()} == true) и в индексе уже есть значения под указанным ключами, то они
   * будут замещены новыми значениями.
   *
   * @param aKeys {@link IIntList} ключи индекса
   * @param aValues {@link ILongList} значения индекса
   * @throws TsNullArgumentRtException любой аргумент null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsInternalErrorRtException переполнение индекса
   */
  void add( IIntList aKeys, ILongList aValues );

  /**
   * Добавляет в индекс указанный массив ключей и их значений
   * <p>
   * Если индекс уникальный ({@link #unique()} == true) и в индексе уже есть значения под указанным ключами, то они
   * будут замещены новыми значениями.
   *
   * @param aKeys int[] ключи индекса
   * @param aValues long[] значения индекса
   * @throws TsNullArgumentRtException любой аргумент null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsInternalErrorRtException переполнение индекса
   */
  void add( int[] aKeys, long[] aValues );

  /**
   * Добавляет в индекс ключ и его значение
   * <p>
   * Если индекс уникальный ({@link #unique()} == true) и в индексе уже есть значение под указанным ключом, то оно будет
   * замещено новым значением.
   *
   * @param aKey int ключ
   * @param aValue long значение ключа
   * @throws TsInternalErrorRtException переполнение индекса
   */
  void add( int aKey, long aValue );
}
