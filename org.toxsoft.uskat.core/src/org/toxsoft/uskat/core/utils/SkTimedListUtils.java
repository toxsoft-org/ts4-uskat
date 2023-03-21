package org.toxsoft.uskat.core.utils;

import java.util.Iterator;

import org.toxsoft.core.tslib.bricks.time.ITemporal;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.bricks.time.impl.TimedList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

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

  /**
   * Объединяет списки темпоральных значений в один список
   *
   * @param aInputs {@link IList}&lt;{@link ITimedList}&gt; входные списки темпоральных значений.
   * @param <T> тип занчений
   * @return {@link ITimedList} выходной список
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static <T extends ITemporal<T>> TimedList<T> uniteTimeporalLists( IList<ITimedList<T>> aInputs ) {
    TsNullArgumentRtException.checkNull( aInputs );
    int size = 0;
    ElemArrayList<TemporalListWrapper<T>> wrappers = new ElemArrayList<>();
    for( ITimedList<T> list : aInputs ) {
      size = list.size();
      wrappers.add( new TemporalListWrapper<>( list ) );
    }
    int bundleSize = getBundleCapacity( size );
    TimedList<T> retValue = new TimedList<>( bundleSize );
    // Объединение значений по времени
    for( T value = nextValueOrNull( wrappers ); value != null; value = nextValueOrNull( wrappers ) ) {
      retValue.add( value );
    }
    return retValue;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  @SuppressWarnings( "unchecked" )
  private static <T extends ITemporal<T>> T nextValueOrNull( ElemArrayList<TemporalListWrapper<T>> aWrappers ) {
    TsNullArgumentRtException.checkNull( aWrappers );
    int foundIndex = -1;
    T foundValue = null;
    for( int index = 0, n = aWrappers.size(); index < n; index++ ) {
      TemporalListWrapper<T> wrapper = aWrappers.get( index );
      T value = (T)wrapper.value();
      if( value == null ) {
        continue;
      }
      if( foundValue != null && foundValue.timestamp() < value.timestamp() ) {
        continue;
      }
      foundValue = value;
      foundIndex = index;
    }
    if( foundValue != null ) {
      aWrappers.get( foundIndex ).next();
    }
    return foundValue;
  }

  private static class TemporalListWrapper<T extends ITemporal<T>> {

    private final Iterator<T> it;
    private T                 value;

    TemporalListWrapper( ITimedList<T> aList ) {
      TsNullArgumentRtException.checkNull( aList );
      it = aList.iterator();
      next();
    }

    ITemporal<T> value() {
      return value;
    }

    void next() {
      value = null;
      if( it.hasNext() ) {
        value = it.next();
      }
    }
  }

}
