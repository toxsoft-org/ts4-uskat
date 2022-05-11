package org.toxsoft.uskat.sysext.alarms.api.generator;

import java.util.function.Predicate;

import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.uskat.sysext.alarms.api.ISkAlarmDef;

/**
 * Профиль аларма
 *
 * @author mvk
 */
public interface ISkAlarmProfile {

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
  IStridablesList<ISkAlarmDataProvider> providers();

  /**
   * Возвращает признак того, что последняя(прошлая) проверка условия {@link Predicate}&lt;{@link ISkAlarmProfile}&gt;
   * установило состояние аларма
   *
   * @return <b>true</b> установлено состояние аларма; <b>false</b> не установлено состояние аларма
   */
  boolean alarmed();
}
