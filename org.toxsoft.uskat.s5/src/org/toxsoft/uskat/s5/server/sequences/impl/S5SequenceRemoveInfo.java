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
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceRemoveInfo;

/**
 * Реализация {@link IS5SequenceRemoveInfo}
 *
 * @author mvk
 */
public class S5SequenceRemoveInfo
    implements IS5SequenceRemoveInfo, Serializable {

  private static final long serialVersionUID = 157157L;

  private final String  tableName;
  private final Gwid    gwid;
  private ITimeInterval interval;

  /**
   * Конструктор
   *
   * @param aTableName String имя таблицы хранения данного
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aInterval {@link ITimeInterval} интервал на котором найдены фрагменты
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SequenceRemoveInfo( String aTableName, Gwid aGwid, ITimeInterval aInterval ) {
    this( aTableName, aGwid, TsNullArgumentRtException.checkNull( aInterval ).startTime(), aInterval.endTime() );
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
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aStartTime > aEndTime
   */
  public S5SequenceRemoveInfo( String aTableName, Gwid aGwid, long aStartTime, long aEndTime ) {
    TsNullArgumentRtException.checkNulls( aTableName, aGwid );
    TsNullArgumentRtException.checkTrue( aStartTime > aEndTime );
    tableName = aTableName;
    gwid = aGwid;
    setInterval( aStartTime, aEndTime );
  }

  /**
   * Конструктор копирования
   *
   * @param aSource {@link IS5SequenceRemoveInfo} источник
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5SequenceRemoveInfo( IS5SequenceRemoveInfo aSource ) {
    TsNullArgumentRtException.checkNull( aSource );
    tableName = aSource.tableName();
    gwid = aSource.gwid();
    ITimeInterval newInterval = aSource.interval();
    setInterval( newInterval.startTime(), newInterval.endTime() );
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
    return true;
  }
}
