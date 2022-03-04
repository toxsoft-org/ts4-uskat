package org.toxsoft.uskat.s5.utils.schedules;

import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Вспомогательные методы сервера для работы с расписанием выполняемых задач
 *
 * @author mvk
 */
public class S5ScheduleUtils {

  private static final String ALL  = "*"; //$NON-NLS-1$
  private static final String ZERO = "0"; //$NON-NLS-1$

  /**
   * Создание календаря для выполнения задачи: каждый день в 00:00:00
   *
   * @return {@link S5ScheduleExpression} каледарь выполнения задачи
   */
  public static S5ScheduleExpression createSchedule() {
    return createSchedule( ALL, ZERO, ZERO, ZERO );
  }

  /**
   * Создание календаря для выполнения задачи: в указанные дни в 00:00:00
   * <p>
   * Подробности определения аргументов календаря: http://docs.oracle.com/javaee/6/api/javax/ejb/ScheduleExpression.html
   *
   * @param aDaysOfWeek String дни недели
   * @return {@link S5ScheduleExpression} каледарь выполнения задачи
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException недопустимое значение дня
   */
  public static S5ScheduleExpression createSchedule( String aDaysOfWeek ) {
    return createSchedule( aDaysOfWeek, ZERO, ZERO, ZERO );
  }

  /**
   * Создание календаря для выполнения задачи: в указанные дни, в указанные часы, 0 минут 0 секунд
   * <p>
   * Подробности определения аргументов календаря: http://docs.oracle.com/javaee/6/api/javax/ejb/ScheduleExpression.html
   *
   * @param aDaysOfWeek String дни недели
   * @param aHours String часы в сутках
   * @return {@link S5ScheduleExpression} каледарь выполнения задачи
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException недопустимое значение дня, часов
   */
  public static S5ScheduleExpression createSchedule( String aDaysOfWeek, String aHours ) {
    return createSchedule( aDaysOfWeek, aHours, ZERO, ZERO );
  }

  /**
   * Создание календаря для выполнения задачи: в указанные дни, в указанные часы, в указанные минуты, 0 секунд
   * <p>
   * Подробности определения аргументов календаря: http://docs.oracle.com/javaee/6/api/javax/ejb/ScheduleExpression.html
   *
   * @param aDaysOfWeek String дни недели
   * @param aHours String часы в сутках
   * @param aMinutes String минуты в часах
   * @return {@link S5ScheduleExpression} каледарь выполнения задачи
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException недопустимое значение дня, часов, минут
   */
  public static S5ScheduleExpression createSchedule( String aDaysOfWeek, String aHours, String aMinutes ) {
    return createSchedule( aDaysOfWeek, aHours, aMinutes, ZERO );
  }

  /**
   * Создание календаря для выполнения задачи: в указанные дни, в указанные часы, в указанные минуты, в указанные
   * секунды
   * <p>
   * Подробности определения аргументов календаря: http://docs.oracle.com/javaee/6/api/javax/ejb/ScheduleExpression.html
   *
   * @param aDaysOfWeek String дни недели
   * @param aHours String часы в сутках
   * @param aMinutes String минуты в часах
   * @param aSeconds String секунды в минутах
   * @return {@link S5ScheduleExpression} каледарь выполнения задачи
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException недопустимое значение дня, часов, минут, секунд
   */
  public static S5ScheduleExpression createSchedule( String aDaysOfWeek, String aHours, String aMinutes,
      String aSeconds ) {
    TsNullArgumentRtException.checkNulls( aDaysOfWeek, aHours, aMinutes, aSeconds );
    // TODO: сделать проверку аргументов (по javadoc ScheduleExpression я не увидел проверок)
    S5ScheduleExpression se = new S5ScheduleExpression();
    se.dayOfWeek( aDaysOfWeek );
    se.hour( aHours );
    se.minute( aMinutes );
    se.second( aSeconds );
    return se;
  }

}
