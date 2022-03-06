package org.toxsoft.uskat.s5.utils.indexes;

import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;

/**
 * Целочисленный ключ индекса доступа
 *
 * @author mvk
 */
public interface IIntKey {

  /**
   * Возвращает ключ в индексе
   * <p>
   * Реализация метода является высокопроизводительной
   *
   * @param aIndex int индекс ключа
   * @return int ключ
   * @throws TsIllegalArgumentRtException неверный индекс ключа
   */
  int intKey( int aIndex );

  /**
   * Возвращает индекс ключа. Если ключа нет в индексе, то возвращается ближайший к нему.
   * <p>
   * Для неуникальных индексов ({@link IIndex#unique()} == false),при наличии нескольких одинаковых ключей, возвращается
   * наименьший индекс найденного ключа
   *
   * @param aKey int ключ
   * @return int индекс ключа. < 0: пустой индекс
   */
  int findIndex( int aKey );

  /**
   * Возвращает индекс ключа.
   * <p>
   * Для неуникальных индексов ({@link IIndex#unique()} == false),при наличии нескольких одинаковых ключей, возвращается
   * наименьший индекс найденного ключа
   *
   * @param aKey int ключ
   * @return int индекс ключа
   * @throws TsIllegalArgumentRtException ключа нет в индексе
   */
  int getIndex( int aKey );
}
