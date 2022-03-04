package org.toxsoft.uskat.s5.utils.indexes;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Индекс произвольного значения по строковому ключу с возможностью редактирования
 *
 * @author mvk
 * @param <V> тип значения
 */
public interface IStringElemIndexEdit<V>
    extends IStringElemIndex<V>, IIndexEdit<String, V> {

  /**
   * Добавляет в индекс указанный массив ключей и их значений
   * <p>
   * Если индекс уникальный ({@link #unique()} == true) и в индексе уже есть значения под указанным ключами, то они
   * будут замещены новыми значениями.
   *
   * @param aKeys {@link IStringList} ключи индекса
   * @param aValues {@link IList}&ltV&gt; значения индекса
   * @throws TsNullArgumentRtException любой аргумент null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsInternalErrorRtException переполнение индекса
   */
  void add( IStringList aKeys, IList<V> aValues );

  /**
   * Добавляет в индекс указанный массив ключей и их значений
   * <p>
   * Если индекс уникальный ({@link #unique()} == true) и в индексе уже есть значения под указанным ключами, то они
   * будут замещены новыми значениями.
   *
   * @param aKeys String[] ключи индекса
   * @param aValues V[] значения индекса
   * @throws TsNullArgumentRtException любой аргумент null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsInternalErrorRtException переполнение индекса
   */
  @Override
  void add( String[] aKeys, V[] aValues );

  /**
   * Добавляет в индекс ключ и его значение
   * <p>
   * Если индекс уникальный ({@link #unique()} == true) и в индексе уже есть значение под указанным ключом, то оно будет
   * замещено новым значением.
   *
   * @param aKey String ключ
   * @param aValue V значение ключа
   * @throws TsNullArgumentRtException aKey = null
   * @throws TsInternalErrorRtException переполнение индекса
   */
  @Override
  void add( String aKey, V aValue );
}
