package org.toxsoft.uskat.core.utils;

import org.toxsoft.core.tslib.bricks.time.impl.TimedList;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedBundleList;
import org.toxsoft.core.tslib.coll.impl.TsCollectionsUtils;

/**
 * Вспомогательные методы для работы с реализацией {@link TimedList}
 * <p>
 * TODO: решить что с этим делать (где поместить, репозиторий, проект, класс. Может быть в самом {@link TimedList})
 *
 * @author mvk
 */
public class SkTimedListUtils {

  /**
   * Проводит расчет максимального размера фрагмента (bundle capacity) для коллекций {@link ElemLinkedBundleList} для
   * эффективного доступа по индексу.
   *
   * @param aCollectionSize int размер коллекции
   * @return int размер фрагмента
   */
  public static int getBundleCapacity( int aCollectionSize ) {
    return Math.max( TsCollectionsUtils.MIN_BUNDLE_CAPACITY,
        Math.min( TsCollectionsUtils.MAX_BUNDLE_CAPACITY, aCollectionSize ) );
  }
}
