package org.toxsoft.uskat.s5.utils.indexes;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Индексация доступа к значениям по ключу с возможностью редактирования
 *
 * @author mvk
 * @param <K> тип ключа
 * @param <V> тип значения
 */
public interface IIndexEdit<K extends Comparable<K>, V>
    extends IIndex<K, V> {

  /**
   * Очистить индекс
   * <p>
   * После очистки, количество элементов в индексе = 0 (watermark = 0 )
   * <p>
   * Метод является высокопроизводительным
   */
  void clear();

  /**
   * Добавляет в индекс указанный массив ключей и их значений
   * <p>
   * Если индекс уникальный ({@link #unique()} == true) и в индексе уже есть значения под указанным ключами, то они
   * будут замещены новыми значениями.
   *
   * @param aKeys {@link IList}&lt;K&gt; ключи индекса
   * @param aValues {@link IList}&lt;V&gt; значения индекса
   * @throws TsNullArgumentRtException любой аргумент null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsInternalErrorRtException переполнение индекса
   */
  void add( IList<K> aKeys, IList<V> aValues );

  /**
   * Добавляет в индекс указанный массив ключей и их значений
   * <p>
   * Если индекс уникальный ({@link #unique()} == true) и в индексе уже есть значения под указанным ключами, то они
   * будут замещены новыми значениями.
   *
   * @param aKeys int[] ключи индекса
   * @param aValues int[] значения индекса
   * @throws TsNullArgumentRtException любой аргумент null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsInternalErrorRtException переполнение индекса
   */
  void add( K[] aKeys, V[] aValues );

  /**
   * Добавляет в индекс ключ и его значение
   * <p>
   * Если индекс уникальный ({@link #unique()} == true) и в индексе уже есть значение под указанным ключом, то оно будет
   * замещено новым значением.
   *
   * @param aKey K ключ
   * @param aValue V значение ключа
   * @throws TsNullArgumentRtException aKey = null
   * @throws TsNullArgumentRtException aValue = null при индексе с примитивными значениями
   * @throws TsInternalErrorRtException переполнение индекса
   */
  void add( K aKey, V aValue );

  /**
   * Устанавливает количество элементов в индексе
   *
   * @param aWatermark int текущее количество элементов в индексе
   * @throws TsIllegalArgumentRtException aWaterMark < 0 или aWaterMark > {@link #capacity()}
   */
  void setWatermark( int aWatermark );

}
