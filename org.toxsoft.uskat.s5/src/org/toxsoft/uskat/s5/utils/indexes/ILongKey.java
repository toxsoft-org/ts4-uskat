package org.toxsoft.uskat.s5.utils.indexes;

import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;

/**
 * Длинный целочисленный ключ индекса доступа
 *
 * @author mvk
 */
public interface ILongKey {

  // TODO: вынести capacity, watermark в базовый интерфейс
  /**
   * Возвращает максимально возможное количество элементов в индексе
   *
   * @return int размер индекса
   */
  int capacity();

  /**
   * Возвращает текущее количество элементов в индексе
   * <p>
   * При создании нового индекса watermark = 0. watermark увеличивается на 1 после каждого добавления пары ключ-значение
   * в индекс. При восстановлении индекса watermark = {@link #capacity()}.
   *
   * @return int текущее количество элементов в индексе
   */
  int watermark();

  /**
   * Возвращает ключ в индексе
   * <p>
   * Реализация метода является высокопроизводительной
   *
   * @param aIndex int индекс ключа
   * @return long ключ
   * @throws TsIllegalArgumentRtException неверный индекс ключа
   */
  long longKey( int aIndex );

  /**
   * Возвращает индекс ключа. Если ключа нет в индексе, то возвращается ближайший к нему.
   * <p>
   * Для неуникальных индексов ({@link IIndex#unique()} == false),при наличии нескольких одинаковых ключей, возвращается
   * наименьший индекс найденного ключа
   *
   * @param aKey long ключ
   * @return int индекс ключа. < 0: пустой индекс
   */
  int findIndex( long aKey );

  /**
   * Возвращает индекс ключа.
   * <p>
   * Для неуникальных индексов ({@link IIndex#unique()} == false),при наличии нескольких одинаковых ключей, возвращается
   * наименьший индекс найденного ключа
   *
   * @param aKey long ключ
   * @return int индекс ключа
   * @throws TsIllegalArgumentRtException ключа нет в индексе
   */
  int getIndex( long aKey );

}
