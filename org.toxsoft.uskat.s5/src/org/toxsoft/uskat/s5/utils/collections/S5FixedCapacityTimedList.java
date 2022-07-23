package org.toxsoft.uskat.s5.utils.collections;

import java.io.Serializable;
import java.util.*;

import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.TimeInterval;
import org.toxsoft.core.tslib.coll.basis.ITsCollection;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.utils.indexes.ILongElemIndexEdit;
import org.toxsoft.uskat.s5.utils.indexes.impl.S5BinaryIndexUtils;

/**
 * Список с определением максимальной емкости
 * <p>
 * Списки на основе примитивных массивов позволяют избавиться от ошибок сериализации java типа: StackOverflowException
 *
 * @author mvk
 * @param <E> - тип элементов списка
 */
public class S5FixedCapacityTimedList<E extends ITimestampable>
    implements ITimedListEdit<E>, Serializable {

  private static final long serialVersionUID = 157157L;

  private final boolean                            allowDuplicates;
  private final long[]                             timestamps;
  private final ITimestampable[]                   values;
  private final ILongElemIndexEdit<ITimestampable> binIndex;

  /**
   * Счетчик количества изменений, используемый для определения конкурентного изменения списка.
   */
  protected int changeCount = 0;

  /**
   * Создает пустой список с указанием всех параметров.
   *
   * @param aCapacityMax int максимально возможное количество элементов в списке
   * @param aAllowDuplicates <b>true</b> - разрешено хранить одинаковые элементы в списоке;<br>
   *          <b>false</b> - в список нельзя хранить одинаковые элементы.
   * @throws TsIllegalArgumentRtException - если aInitialCapacity < 0
   */
  public S5FixedCapacityTimedList( int aCapacityMax, boolean aAllowDuplicates ) {
    allowDuplicates = aAllowDuplicates;
    timestamps = new long[aCapacityMax];
    values = new ITimestampable[aCapacityMax];
    if( !aAllowDuplicates ) {
      binIndex = S5BinaryIndexUtils.createLongElemIndex( timestamps, values );
      return;
    }
    binIndex = S5BinaryIndexUtils.createUniqueLongElemIndex( timestamps, values );
  }

  /**
   * Служебный конструктор для создания копий списка на интервале.
   *
   * @param aTimestamps массив меток времени значений
   * @param aValues {@link ITimestampable} массив значений
   * @param aAllowDuplicates <b>true</b> - разрешено хранить одинаковые элементы в списоке;<br>
   *          <b>false</b> - в список нельзя хранить одинаковые элементы.
   */
  private S5FixedCapacityTimedList( long[] aTimestamps, ITimestampable[] aValues, boolean aAllowDuplicates ) {
    TsNullArgumentRtException.checkNulls( aTimestamps, aValues );
    TsIllegalArgumentRtException.checkFalse( aTimestamps.length == aValues.length );
    allowDuplicates = aAllowDuplicates;
    timestamps = aTimestamps;
    values = aValues;
    if( !aAllowDuplicates ) {
      // aCheck = false
      binIndex = S5BinaryIndexUtils.restoreLongElemIndex( timestamps, values, false );
      return;
    }
    // aCheck = false
    binIndex = S5BinaryIndexUtils.restoreUniqueLongElemIndex( timestamps, values, false );
  }

  // ------------------------------------------------------------------------------------
  // ITimedListEdit
  //
  @Override
  public ITimeInterval getInterval() {
    if( binIndex.watermark() == 0 ) {
      return ITimeInterval.NULL;
    }
    long startTime = binIndex.longKey( 0 );
    long endTime = binIndex.longKey( binIndex.watermark() - 1 );
    return new TimeInterval( startTime, endTime );
  }

  @Override
  public ITimedListEdit<E> selectInterval( ITimeInterval aTimeInterval ) {
    TsNullArgumentRtException.checkNull( aTimeInterval );
    if( binIndex.watermark() == 0 ) {
      return new S5FixedCapacityTimedList<>( 0, binIndex.unique() );
    }
    int firstIndex = firstIndexOrAfter( aTimeInterval.startTime() );
    int lastIndex = lastIndexOrBefore( aTimeInterval.endTime() );
    int size = lastIndex - firstIndex + 1;
    long[] destTimestamps = new long[size];
    ITimestampable[] destValues = new ITimestampable[size];
    System.arraycopy( timestamps, firstIndex, destTimestamps, 0, size );
    System.arraycopy( values, firstIndex, destValues, 0, size );
    return new S5FixedCapacityTimedList<>( destTimestamps, destValues, allowDuplicates );
  }

  @Override
  public ITimedListEdit<E> selectExtendedInterval( ITimeInterval aTimeInterval ) {
    TsNullArgumentRtException.checkNull( aTimeInterval );
    if( binIndex.watermark() == 0 ) {
      return new S5FixedCapacityTimedList<>( 0, binIndex.unique() );
    }
    int firstIndex = firstIndexOrBefore( aTimeInterval.startTime() );
    int lastIndex = lastIndexOrAfter( aTimeInterval.endTime() );
    int size = lastIndex - firstIndex + 1;
    long[] destTimestamps = new long[size];
    ITimestampable[] destValues = new ITimestampable[size];
    System.arraycopy( timestamps, firstIndex, destTimestamps, 0, size );
    System.arraycopy( values, firstIndex, destValues, 0, size );
    return new S5FixedCapacityTimedList<>( destTimestamps, destValues, allowDuplicates );
  }

  @Override
  public ITimedListEdit<E> selectAfter( long aTimestamp ) {
    return selectExtendedInterval( new TimeInterval( aTimestamp, getInterval().endTime() ) );
  }

  @Override
  public ITimedListEdit<E> selectBefore( long aTimestamp ) {
    return selectExtendedInterval( new TimeInterval( getInterval().startTime(), aTimestamp ) );
  }

  @Override
  public int firstIndexOf( long aTimestamp ) {
    int firstIndex = binIndex.findIndex( aTimestamp );
    // Доводка индекса в прошлое
    while( firstIndex > 0 && binIndex.longKey( firstIndex - 1 ) == aTimestamp ) {
      firstIndex--;
    }
    return (binIndex.longKey( firstIndex ) == aTimestamp ? firstIndex : -1);
  }

  @Override
  public int firstIndexOrBefore( long aTimestamp ) {
    if( binIndex.watermark() == 0 ) {
      return -1;
    }
    int firstIndex = binIndex.findIndex( aTimestamp );
    // Доводка индекса в прошлое
    while( firstIndex > 0 && binIndex.longKey( firstIndex ) >= aTimestamp ) {
      firstIndex--;
    }
    return firstIndex;
  }

  @Override
  public int firstIndexOrAfter( long aTimestamp ) {
    if( binIndex.watermark() == 0 ) {
      return -1;
    }
    int firstIndex = binIndex.findIndex( aTimestamp );
    // Доводка индекса в прошлое
    while( firstIndex > 0 && binIndex.longKey( firstIndex - 1 ) == aTimestamp ) {
      firstIndex--;
    }
    return firstIndex;
  }

  @Override
  public int firstIndexAfter( long aTimestamp ) {
    if( binIndex.watermark() == 0 ) {
      return -1;
    }
    int firstIndex = binIndex.findIndex( aTimestamp );
    // Доводка индекса в будущее
    while( firstIndex + 1 < binIndex.watermark() && binIndex.longKey( firstIndex ) == aTimestamp ) {
      firstIndex++;
    }
    return firstIndex;
  }

  @Override
  public int lastIndexOf( long aTimestamp ) {
    int lastIndex = binIndex.findIndex( aTimestamp );
    // Доводка индекса в будущее
    while( lastIndex + 1 < binIndex.watermark() && binIndex.longKey( lastIndex + 1 ) == aTimestamp ) {
      lastIndex++;
    }
    return (binIndex.longKey( lastIndex ) == aTimestamp ? lastIndex : -1);
  }

  @Override
  public int lastIndexOrBefore( long aTimestamp ) {
    if( binIndex.watermark() == 0 ) {
      return -1;
    }
    int lastIndex = binIndex.findIndex( aTimestamp );
    // Доводка индекса в будущее
    while( lastIndex + 1 < binIndex.watermark() && binIndex.longKey( lastIndex + 1 ) == aTimestamp ) {
      lastIndex++;
    }
    return lastIndex;
  }

  @Override
  public int lastIndexOrAfter( long aTimestamp ) {
    if( binIndex.watermark() == 0 ) {
      return -1;
    }
    int lastIndex = binIndex.findIndex( aTimestamp );
    // Доводка индекса в будущее
    while( lastIndex + 1 < binIndex.watermark() && binIndex.longKey( lastIndex ) <= aTimestamp ) {
      lastIndex++;
    }
    return lastIndex;
  }

  @Override
  public int lastIndexBefore( long aTimestamp ) {
    if( binIndex.watermark() == 0 ) {
      return -1;
    }
    int lastIndex = binIndex.findIndex( aTimestamp );
    // Доводка индекса в прошлое
    while( lastIndex > 0 && binIndex.longKey( lastIndex ) == aTimestamp ) {
      lastIndex--;
    }
    return lastIndex;
  }

  // ------------------------------------------------------------------------------------
  // IListEdit, ITsFastIndexCollectionTag, Serializable
  //
  @SuppressWarnings( "unchecked" )
  @Override
  public E[] toArray( E[] aSrcArray ) {
    TsNullArgumentRtException.checkNull( aSrcArray );
    if( aSrcArray.length < size() ) {
      // Make a new array of a's runtime type, but my contents:
      return (E[])Arrays.copyOf( values, size(), aSrcArray.getClass() );
    }
    System.arraycopy( values, 0, aSrcArray, 0, size() );
    for( int i = binIndex.watermark(); i < aSrcArray.length; i++ ) {
      aSrcArray[i] = null;
    }
    return aSrcArray;
  }

  @Override
  public Object[] toArray() {
    if( binIndex.watermark() == 0 ) {
      return TsLibUtils.EMPTY_ARRAY_OF_OBJECTS;
    }
    return Arrays.copyOf( values, size() );
  }

  @Override
  public int indexOf( E aElem ) {
    TsNullArgumentRtException.checkNull( aElem );
    for( int index = 0, n = size(); index < n; index++ ) {
      if( binIndex.value( index ).equals( aElem ) ) {
        return index;
      }
    }
    return -1;
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public E get( int aIndex ) {
    return (E)binIndex.value( aIndex );
  }

  @Override
  public boolean hasElem( E aElem ) {
    return (indexOf( aElem ) != 0);
  }

  @Override
  public Iterator<E> iterator() {
    return new Iterator<>() {

      int               index               = 0;
      private final int expectedChangeCount = changeCount;

      private void checkForConcurrentModification() {
        if( expectedChangeCount != changeCount ) {
          throw new ConcurrentModificationException();
        }
      }

      @Override
      public boolean hasNext() {
        checkForConcurrentModification();
        return index < size();
      }

      @Override
      public E next() {
        if( hasNext() ) {
          return get( index++ );
        }
        throw new NoSuchElementException();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

    };
  }

  @Override
  public boolean isEmpty() {
    return (size() == 0);
  }

  @Override
  public int size() {
    return binIndex.watermark();
  }

  @Override
  public int remove( E aElem ) {
    throw new TsUnderDevelopmentRtException();
  }

  @Override
  public E removeByIndex( int aIndex ) {
    throw new TsUnderDevelopmentRtException();
  }

  @Override
  public void clear() {
    throw new TsUnderDevelopmentRtException();
  }

  @Override
  public int add( E aElem ) {
    TsNullArgumentRtException.checkNull( aElem );
    int retValue = binIndex.add( aElem.timestamp(), aElem );
    changeCount++;
    return retValue;
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public void addAll( E... aArray ) {
    TsNullArgumentRtException.checkNull( aArray );
    for( E value : aArray ) {
      add( value );
    }
  }

  @Override
  public void addAll( ITsCollection<E> aElemList ) {
    TsNullArgumentRtException.checkNull( aElemList );
    for( E value : aElemList ) {
      add( value );
    }
  }

  @Override
  public void addAll( Collection<E> aElemColl ) {
    TsNullArgumentRtException.checkNull( aElemColl );
    for( E value : aElemColl ) {
      add( value );
    }
  }

  @Override
  public void setAll( ITsCollection<E> aColl ) {
    throw new TsUnderDevelopmentRtException();
  }

  @Override
  public void setAll( Collection<E> aColl ) {
    throw new TsUnderDevelopmentRtException();
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public void setAll( E... aElems ) {
    throw new TsUnderDevelopmentRtException();
  }

  @Override
  public int replaceByTimestamp( E aElem ) {
    throw new TsUnderDevelopmentRtException();
  }

}
