package org.toxsoft.uskat.s5.utils.indexes;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.ILongList;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Индекс произвольного значения по длинному целочисленному ключу с возможностью редактирования
 *
 * @author mvk
 * @param <V> тип значения
 */
public interface ILongElemIndexEdit<V>
    extends ILongElemIndex<V>, IIndexEdit<Long, V> {

  /**
   * Добавляет в индекс указанный массив ключей и их значений
   * <p>
   * Если индекс уникальный ({@link #unique()} == true) и в индексе уже есть значения под указанным ключами, то они
   * будут замещены новыми значениями.
   *
   * @param aKeys {@link ILongList} ключи индекса
   * @param aValues {@link IList}&ltV&gt; значения индекса
   * @throws TsNullArgumentRtException любой аргумент null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsInternalErrorRtException переполнение индекса
   */
  void add( ILongList aKeys, IList<V> aValues );

  /**
   * Добавляет в индекс указанный массив ключей и их значений
   * <p>
   * Если индекс уникальный ({@link #unique()} == true) и в индексе уже есть значения под указанным ключами, то они
   * будут замещены новыми значениями.
   *
   * @param aKeys long[] ключи индекса
   * @param aValues V[] значения индекса
   * @throws TsNullArgumentRtException любой аргумент null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsInternalErrorRtException переполнение индекса
   */
  void add( long[] aKeys, V[] aValues );

  /**
   * Добавляет в индекс ключ и его значение
   * <p>
   * Если индекс уникальный ({@link #unique()} == true) и в индексе уже есть значение под указанным ключом, то оно будет
   * замещено новым значением.
   *
   * @param aKey long ключ
   * @param aValue V значение ключа
   * @return int индекс под которым было добавлено значение
   * @throws TsInternalErrorRtException переполнение индекса
   */
  int add( long aKey, V aValue );
}
