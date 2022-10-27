package org.toxsoft.uskat.s5.server.sequences.impl;

import static org.toxsoft.core.tslib.bricks.time.impl.TimeUtils.*;

import java.io.Serializable;

import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.bricks.time.impl.TimeInterval;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceFragmentInfo;

/**
 * Реализация {@link IS5SequenceFragmentInfo}
 *
 * @author mvk
 */
public class S5SequenceFragmentInfo
    implements IS5SequenceFragmentInfo, Serializable {

  private static final long serialVersionUID = 157157L;

  private final String  tableName;
  private final Gwid    gwid;
  private ITimeInterval interval;
  private int           count;
  private int           afterCount;

  /**
   * Конструктор
   *
   * @param aTableName String имя таблицы хранения данного
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aInterval {@link ITimeInterval} интервал на котором найдены фрагменты
   * @param aFragmentCount int количество найденных фрагментов. < 0: неопределенно
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SequenceFragmentInfo( String aTableName, Gwid aGwid, ITimeInterval aInterval, int aFragmentCount ) {
    this( aTableName, aGwid, TsNullArgumentRtException.checkNull( aInterval ).startTime(), aInterval.endTime(),
        aFragmentCount );
  }

  /**
   * Конструктор
   *
   * @param aTableName String имя таблицы хранения данного
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aStartTime long метка времени (мсек с начала эпохи) начала интервала в котором находятся фрагменты.
   *          Включительно
   * @param aEndTime long метка времени (мсек с начала эпохи) завершения интервала в котором находятся фрагменты.
   *          Включительно
   * @param aFragmentCount int количество найденных фрагментов. < 0: неопределенно
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aStartTime > aEndTime
   */
  public S5SequenceFragmentInfo( String aTableName, Gwid aGwid, long aStartTime, long aEndTime, int aFragmentCount ) {
    this( aTableName, aGwid, aStartTime, aEndTime, aFragmentCount, -1 );
  }

  /**
   * Конструктор
   *
   * @param aTableName String имя таблицы хранения данного
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aStartTime long метка времени (мсек с начала эпохи) начала интервала в котором находятся фрагменты.
   *          Включительно
   * @param aEndTime long метка времени (мсек с начала эпохи) завершения интервала в котором находятся фрагменты.
   *          Включительно
   * @param aFragmentCount int количество найденных фрагментов. < 0: неопределенно
   * @param aFragmentAfterCount int количество фрагментов найденных ПОСЛЕ интервала. < 0: неопределенно
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aStartTime > aEndTime
   */
  public S5SequenceFragmentInfo( String aTableName, Gwid aGwid, long aStartTime, long aEndTime, int aFragmentCount,
      int aFragmentAfterCount ) {
    TsNullArgumentRtException.checkNulls( aTableName, aGwid );
    TsNullArgumentRtException.checkTrue( aStartTime > aEndTime );
    tableName = aTableName;
    gwid = aGwid;
    setInterval( aStartTime, aEndTime );
    setFragmentCount( aFragmentCount );
    setFragmentAfterCount( aFragmentAfterCount );
  }

  /**
   * Конструктор копирования
   *
   * @param aSource {@link IS5SequenceFragmentInfo} источник
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5SequenceFragmentInfo( IS5SequenceFragmentInfo aSource ) {
    TsNullArgumentRtException.checkNull( aSource );
    tableName = aSource.tableName();
    gwid = aSource.gwid();
    ITimeInterval newInterval = aSource.interval();
    setInterval( newInterval.startTime(), newInterval.endTime() );
    setFragmentCount( aSource.fragmentCount() );
    setFragmentAfterCount( aSource.fragmentAfterCount() );
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Устанавливает интервал в котором находятся фрагменты последовательности
   *
   * @param aStartTime long метка времени (мсек с начала эпохи) начала интервала в котором находятся фрагменты.
   *          Включительно
   * @param aEndTime long метка времени (мсек с начала эпохи) завершения интервала в котором находятся фрагменты.
   *          Включительно
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException aStartTime > aEndTime
   */
  public void setInterval( long aStartTime, long aEndTime ) {
    TsNullArgumentRtException.checkTrue( aStartTime > aEndTime );
    interval = new TimeInterval( aStartTime, aEndTime );
  }

  /**
   * Устанавливает количество фрагментов в интервале
   *
   * @param aCount int количество фрагментов. < 0: неопределенно
   */
  public void setFragmentCount( int aCount ) {
    count = aCount;
  }

  /**
   * Устанавливает количество фрагментов найденных ПОСЛЕ интервала {@link #interval()}
   *
   * @param aCount int количество фрагментов. < 0: неопределенно
   */
  public void setFragmentAfterCount( int aCount ) {
    afterCount = aCount;
  }

  // ------------------------------------------------------------------------------------
  // /Реализация IS5SequenceFragmentInfo
  //
  @Override
  public String tableName() {
    return tableName;
  }

  @Override
  public Gwid gwid() {
    return gwid;
  }

  @Override
  public ITimeInterval interval() {
    return interval;
  }

  @Override
  public int fragmentCount() {
    return count;
  }

  @Override
  public int fragmentAfterCount() {
    return afterCount;
  }

  // ------------------------------------------------------------------------------------
  // /Реализация IS5SequenceFragmentInfo
  //
  @Override
  public int compareTo( ITimeInterval o ) {
    if( o == null ) {
      throw new NullPointerException();
    }
    int c = Long.compare( interval.startTime(), o.startTime() );
    if( c == 0 ) {
      c = Long.compare( interval.endTime(), o.endTime() );
    }
    return c;
  }

  // ------------------------------------------------------------------------------------
  // Переопределение Object
  //
  @Override
  public String toString() {
    long startTime = interval.startTime();
    long endTime = interval.endTime();
    return tableName + ':' + gwid.toString() + '[' + timestampToString( startTime ) + '-' + timestampToString( endTime )
        + ']';
  }

  @Override
  public int hashCode() {
    long startTime = interval.startTime();
    long endTime = interval.endTime();
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + tableName.hashCode();
    result = TsLibUtils.PRIME * result + gwid.hashCode();
    result = TsLibUtils.PRIME * result + (int)(startTime ^ (startTime >>> 32));
    result = TsLibUtils.PRIME * result + (int)(endTime ^ (endTime >>> 32));
    result = TsLibUtils.PRIME * result + (count ^ (count >>> 32));
    return result;
  }

  @Override
  public boolean equals( Object aObject ) {
    if( this == aObject ) {
      return true;
    }
    if( aObject == null ) {
      return false;
    }
    if( aObject.getClass() != IS5SequenceFragmentInfo.class ) {
      return false;
    }
    IS5SequenceFragmentInfo other = (IS5SequenceFragmentInfo)aObject;
    if( !tableName.equals( other.tableName() ) ) {
      return false;
    }
    if( !gwid.equals( other.gwid() ) ) {
      return false;
    }
    ITimeInterval otherInterval = other.interval();
    long startTime = interval.startTime();
    long endTime = interval.endTime();
    if( startTime != otherInterval.startTime() ) {
      return false;
    }
    if( endTime != otherInterval.endTime() ) {
      return false;
    }
    if( count != other.fragmentCount() ) {
      return false;
    }
    return true;
  }
}
