package org.toxsoft.uskat.alarms.s5.generator;

import java.util.function.Predicate;

import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.uskat.alarms.lib.ISkAlarmDef;

/**
 * Профиль аларма
 *
 * @author mvk
 */
public interface IS5AlarmProfile {

  /**
   * Идентификатор объекта, автор аларма
   *
   * @return {@link Skid} идентификатор объекта-автора
   */
  Skid alarmAuthorId();

  /**
   * Возвращает описание аларма
   *
   * @return {@link ISkAlarmDef} описание аларма
   */
  ISkAlarmDef skAlarmDef();

  /**
   * Возвращает список зарегистрированных поставщиков данных для формирования аларма
   *
   * @return {@link IStridablesList} список зарегистрированных поставщиков
   */
  IStridablesList<IS5AlarmDataProvider> providers();

  /**
   * Возвращает признак того, что последняя(прошлая) проверка условия {@link Predicate}&lt;{@link IS5AlarmProfile}&gt;
   * установило состояние аларма
   *
   * @return <b>true</b> установлено состояние аларма; <b>false</b> не установлено состояние аларма
   */
  boolean alarmed();
}
