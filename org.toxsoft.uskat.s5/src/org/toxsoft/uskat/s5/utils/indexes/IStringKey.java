package org.toxsoft.uskat.s5.utils.indexes;

import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;

/**
 * Строковый ключ индекса доступа
 *
 * @author mvk
 */
public interface IStringKey {

  /**
   * Возвращает ключ в индексе
   * <p>
   * Реализация метода является высокопроизводительной
   *
   * @param aIndex int индекс ключа
   * @return String ключ
   * @throws TsIllegalArgumentRtException неверный индекс ключа
   */
  String key( int aIndex );

  /**
   * Возвращает индекс ключа. Если ключа нет в индексе, то возвращается ближайший к нему.
   * <p>
   * Для неуникальных индексов ({@link IIndex#unique()} == false),при наличии нескольких одинаковых ключей, возвращается
   * наименьший индекс найденного ключа
   *
   * @param aKey String ключ
   * @return int индекс ключа. < 0: пустой индекс
   */
  int findIndex( String aKey );

  /**
   * Возвращает индекс ключа.
   * <p>
   * Для неуникальных индексов ({@link IIndex#unique()} == false),при наличии нескольких одинаковых ключей, возвращается
   * наименьший индекс найденного ключа
   *
   * @param aKey String ключ
   * @return int индекс ключа
   * @throws TsIllegalArgumentRtException ключа нет в индексе
   */
  int getIndex( String aKey );
}
