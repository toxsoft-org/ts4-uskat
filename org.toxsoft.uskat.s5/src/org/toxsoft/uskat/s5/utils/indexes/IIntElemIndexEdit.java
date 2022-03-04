package org.toxsoft.uskat.s5.utils.indexes;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IIntList;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Индекс произвольного значения по целочисленному ключу с возможностью редактирования
 *
 * @author mvk
 * @param <V> тип значения
 */
public interface IIntElemIndexEdit<V>
    extends IIntElemIndex<V>, IIndexEdit<Integer, V> {

  /**
   * Добавляет в индекс указанный массив ключей и их значений
   * <p>
   * Если индекс уникальный ({@link #unique()} == true) и в индексе уже есть значения под указанным ключами, то они
   * будут замещены новыми значениями.
   *
   * @param aKeys {@link IIntList} ключи индекса
   * @param aValues {@link IList}&ltV&gt; значения индекса
   * @throws TsNullArgumentRtException любой аргумент null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsInternalErrorRtException переполнение индекса
   */
  void add( IIntList aKeys, IList<V> aValues );

  /**
   * Добавляет в индекс указанный массив ключей и их значений
   * <p>
   * Если индекс уникальный ({@link #unique()} == true) и в индексе уже есть значения под указанным ключами, то они
   * будут замещены новыми значениями.
   *
   * @param aKeys int[] ключи индекса
   * @param aValues V[] значения индекса
   * @throws TsNullArgumentRtException любой аргумент null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsInternalErrorRtException переполнение индекса
   */
  void add( int[] aKeys, V[] aValues );

  /**
   * Добавляет в индекс ключ и его значение
   * <p>
   * Если индекс уникальный ({@link #unique()} == true) и в индексе уже есть значение под указанным ключом, то оно будет
   * замещено новым значением.
   *
   * @param aKey int ключ
   * @param aValue V значение ключа
   * @throws TsInternalErrorRtException переполнение индекса
   */
  void add( int aKey, V aValue );
}
