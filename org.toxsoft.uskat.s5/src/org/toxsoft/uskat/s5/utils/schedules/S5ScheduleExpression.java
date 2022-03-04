package org.toxsoft.uskat.s5.utils.schedules;

import javax.ejb.ScheduleExpression;

import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Расписание для календаря
 * <p>
 * TODO: не стал бы делать, но в базовом коде (javax.ejb нет нормального переопределения equals)
 *
 * @author mvk
 */
public class S5ScheduleExpression
    extends ScheduleExpression
    implements IScheduleExpression {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор по умолчанию
   */
  public S5ScheduleExpression() {
  }

  /**
   * Конструктор копирования из {@link ScheduleExpression}
   *
   * @param aSource {@link ScheduleExpression} исходное рассписание
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5ScheduleExpression( ScheduleExpression aSource ) {
    TsNullArgumentRtException.checkNull( aSource );
    second( aSource.getSecond() );
    minute( aSource.getMinute() );
    hour( aSource.getHour() );
    dayOfMonth( aSource.getDayOfMonth() );
    month( aSource.getMonth() );
    dayOfWeek( aSource.getDayOfWeek() );
    year( aSource.getYear() );
    timezone( aSource.getTimezone() );
    start( aSource.getStart() );
    end( aSource.getEnd() );
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов класса Object
  //

  @Override
  public String toString() {
    return super.toString();
  }

  @Override
  public boolean equals( Object aObj ) {
    if( aObj == this ) {
      return true;
    }
    if( !(aObj instanceof IScheduleExpression obj) ) {
      return false;
    }
    if( !getDayOfMonth().equals( obj.getDayOfMonth() ) ) {
      return false;
    }
    if( !getDayOfWeek().equals( obj.getDayOfWeek() ) ) {
      return false;
    }
    if( getEnd() == null && obj.getEnd() != null || getEnd() != null && !getEnd().equals( obj.getEnd() ) ) {
      return false;
    }
    if( !getHour().equals( obj.getHour() ) ) {
      return false;
    }
    if( !getMinute().equals( obj.getMinute() ) ) {
      return false;
    }
    if( !getMonth().equals( obj.getMonth() ) ) {
      return false;
    }
    if( !getSecond().equals( obj.getSecond() ) ) {
      return false;
    }
    if( getStart() == null && obj.getStart() != null || getStart() != null && !getStart().equals( obj.getStart() ) ) {
      return false;
    }
    if( getTimezone() == null && obj.getTimezone() != null
        || getTimezone() != null && !getTimezone().equals( obj.getTimezone() ) ) {
      return false;
    }
    if( !getYear().equals( obj.getYear() ) ) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + getDayOfMonth().hashCode();
    result = TsLibUtils.PRIME * result + getDayOfWeek().hashCode();
    result = TsLibUtils.PRIME * result + getEnd().hashCode();
    result = TsLibUtils.PRIME * result + getHour().hashCode();
    result = TsLibUtils.PRIME * result + getMinute().hashCode();
    result = TsLibUtils.PRIME * result + getMonth().hashCode();
    result = TsLibUtils.PRIME * result + getSecond().hashCode();
    result = TsLibUtils.PRIME * result + getStart().hashCode();
    result = TsLibUtils.PRIME * result + getTimezone().hashCode();
    result = TsLibUtils.PRIME * result + getYear().hashCode();
    return result;
  }

}
