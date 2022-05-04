package org.toxsoft.uskat.s5.legacy;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.TimeInterval;
import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Неизменяемая реализация интервала времени {@link IQueryInterval}.
 *
 * @author goga
 */
public final class QueryInterval
    implements IQueryInterval, Serializable {

  private static final long serialVersionUID = 157157L;

  private final EQueryIntervalType type;
  private final TimeInterval       interval;

  /**
   * Конструктор интервала.
   *
   * @param aType {@link EQueryIntervalType} - тип интервала
   * @param aStart long - время начала интеравала (в миллисекундах с начала эпохи)
   * @param aEnd long - время окончания интеравала (в миллисекундах с начала эпохи)
   * @throws TsIllegalArgumentRtException aEnd < aStart
   */
  public QueryInterval( EQueryIntervalType aType, long aStart, long aEnd ) {
    TsNullArgumentRtException.checkNull( aType );
    TimeUtils.checkIntervalArgs( aStart, aEnd );
    type = aType;
    interval = new TimeInterval( aStart, aEnd );
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса ITimeInterval
  //

  @Override
  public long startTime() {
    return interval.startTime();
  }

  @Override
  public long endTime() {
    return interval.endTime();
  }

  @Override
  public long duration() {
    return interval.duration();
  }

  @Override
  public LocalDateTime getStartDatetime() {
    return interval.getStartDatetime();
  }

  @Override
  public LocalDateTime getEndDatetime() {
    return interval.getEndDatetime();
  }

  @Override
  public Duration getDuration() {
    return interval.getDuration();
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IQueryInterval
  //

  @Override
  public EQueryIntervalType type() {
    return type;
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов класса Object
  //

  @SuppressWarnings( "nls" )
  @Override
  public String toString() {
    // mvk:
    // String ss = type.isStartOpen() ? "(" : "[";
    // String se = type.isEndOpen() ? "(" : "[";
    // return String.format( "%s%%2$tF_%2$tT,%3$tF_%3$tT%s", ss, Long.valueOf( start ), Long.valueOf( end ), se );
    String ss = type.isStartOpen() ? "(" : "[";
    String se = type.isEndOpen() ? ")" : "]";
    long start = interval.startTime();
    long end = interval.endTime();
    return String.format( "%s%s%s", ss, TimeUtils.intervalToString( start, end ), se );
  }

  @Override
  public boolean equals( Object aObj ) {
    if( aObj == this ) {
      return true;
    }
    if( aObj instanceof QueryInterval that ) {
      long start = interval.startTime();
      long end = interval.endTime();
      return start == that.startTime() && end == that.endTime() && type.equals( that.type );
    }
    return false;
  }

  @Override
  public int hashCode() {
    long start = interval.startTime();
    long end = interval.endTime();
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + type.hashCode();
    result = TsLibUtils.PRIME * result + (int)(start ^ (start >>> 32));
    result = TsLibUtils.PRIME * result + (int)(end ^ (end >>> 32));
    return result;
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса Comparable
  //

  @Override
  public int compareTo( ITimeInterval o ) {
    if( o == null ) {
      throw new NullPointerException();
    }
    long start = interval.startTime();
    long end = interval.endTime();
    int c = Long.compare( start, o.startTime() );
    if( c == 0 ) {
      c = Long.compare( end, o.endTime() );
    }
    return c;
  }

}
