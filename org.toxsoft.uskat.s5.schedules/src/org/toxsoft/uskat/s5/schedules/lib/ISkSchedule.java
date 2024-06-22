package org.toxsoft.uskat.s5.schedules.lib;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.gw.ugwi.IUgwiList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.core.api.objserv.ISkObject;

/**
 * Расписание службы планирования {@link ISkScheduleService}.
 *
 * @author mvk
 */
public interface ISkSchedule
    extends ISkObject {

  /**
   * The {@link ISkSchedule} class identifier.
   */
  String CLASS_ID = ISkSchedulesHardConstants.CLSID_SCHEDULE;

  /**
   * Returns the alarm object SKID - an unique identifier of the alarm.
   *
   * @return {@link Skid} - the alarm object SKID
   */
  @Override
  Skid skid();

  /**
   * Атрибут: Cекунды.
   * <p>
   * Примеры значений: <b>"*"</b>, <b>"*&#47N"</b> где N - число, <b>"0-59"</b>, <b>"0,1,3-7"</b>.
   * <p>
   * Значение по умолчанию: <b>"0"</b>.
   * <p>
   * Тип: {@link EAtomicType#STRING}<br>
   *
   * @return String секунды
   */
  String seconds();

  /**
   * Атрибут: Минуты.
   * <p>
   * Примеры значений: <b>"*"</b>, <b>"*&#47N"</b> где N - число, <b>"0-59"</b>, <b>"0,1,3-7"</b>.
   * <p>
   * Значение по умолчанию: <b>"0"</b>.
   * <p>
   * Тип: {@link EAtomicType#STRING}<br>
   *
   * @return String минуты
   */
  String minutes();

  /**
   * Атрибут: Часы.
   * <p>
   * Примеры значений: <b>"*"</b>, <b>"*&#47N"</b> где N - число, <b>"0-23"</b>, <b>"0,1,3-7"</b>.
   * <p>
   * Значение по умолчанию: <b>"0"</b>.
   * <p>
   * Тип: {@link EAtomicType#STRING}<br>
   *
   * @return String часы
   */
  String hours();

  /**
   * Атрибут: Дни месяца.
   * <p>
   * Примеры значений: <b>"*"</b>, <b>"*&#47N"</b> где N - число, <b>"1-31"</b>, <b>"0,1,3-7"</b>.
   * <p>
   * Значение по умолчанию: <b>"*"</b>.
   * <p>
   * Тип: {@link EAtomicType#STRING}<br>
   *
   * @return String дни месяца
   */
  String daysOfMonth();

  /**
   * Атрибут: Месяцы.
   * <p>
   * Примеры значений: <b>"*"</b>, <b>"*&#47N"</b> где N - число, <b>"1-12"</b>, <b>"1,3-7"</b>.
   * <p>
   * Значение по умолчанию: <b>"*"</b>.
   * <p>
   * Тип: {@link EAtomicType#STRING}<br>
   *
   * @return String месяцы
   */
  String months();

  /**
   * Атрибут: Дни недели.
   * <p>
   * Примеры значений: <b>"*"</b>, <b>"*&#47N"</b> где N - число, <b>"0-7"</b> где <b>"0"</b> и <b>"7"</b> это
   * воскресенье, <b>"0,1,3-7"</b>.
   * <p>
   * Значение по умолчанию: <b>"*"</b>.
   * <p>
   * Тип: {@link EAtomicType#STRING}<br>
   *
   * @return String дни недели
   */
  String daysOfWeek();

  /**
   * Атрибут: Годы.
   * <p>
   * Примеры значений: <b>"*"</b>, <b>"*&#47N"</b> где N - число, <b>"1900-2100"</b>, <b>"2015,2016,2015-2020"</b>.
   * <p>
   * Значение по умолчанию: <b>"*"</b>.
   * <p>
   * Тип: {@link EAtomicType#STRING}<br>
   *
   * @return String годы
   */
  String years();

  /**
   * Атрибут: Часовой пояс.
   * <p>
   * Значение по умолчанию: пустая строка (системный часовой пояс)
   * <p>
   * Тип: {@link EAtomicType#STRING}<br>
   *
   * @return String часовой пояс
   */
  String timezone();

  /**
   * Атрибут: Метка времени начала работы расписания.
   * <p>
   * Значение по умолчанию: не установлено.
   * <p>
   * Тип: {@link EAtomicType#TIMESTAMP}<br>
   *
   * @return long метка времени
   */
  long start();

  /**
   * Атрибут: Метка времени завершение работы расписания.
   * <p>
   * Значение по умолчанию: не установлено.
   * <p>
   * Тип: {@link EAtomicType#TIMESTAMP}<br>
   *
   * @return long метка времени
   */
  long end();

  /**
   * Атрибут: Список {@link Gwid}-идентификаторов связанных с расписанием.
   * <p>
   * Значение по умолчанию: пустой.
   * <p>
   * Тип: {@link EAtomicType#VALOBJ}<br>
   *
   * @return {@link IUgwiList} список идентификаторов.
   */
  IUgwiList ugwis();

  /**
   * Returns history of the schedule events.
   *
   * @param aInterval {@link IQueryInterval} - the query interval
   * @return {@link ITimedList}&lt;{@link SkEvent}&gt; - the schedule events history
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ITimedList<SkEvent> getHistory( IQueryInterval aInterval );
}
