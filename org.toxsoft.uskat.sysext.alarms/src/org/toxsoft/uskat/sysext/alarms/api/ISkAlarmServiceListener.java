package org.toxsoft.uskat.sysext.alarms.api;

/**
 * Слушатель изменений в службе тревог.
 *
 * @author goga
 */
public interface ISkAlarmServiceListener {

  /**
   * Вызывается при генерации (создании) тревоги
   *
   * @param aSkAlarm {@link ISkAlarm} - новая тревога
   */
  void onAlarm( ISkAlarm aSkAlarm );

  /**
   * Вызывается при изменении состояния обработки тревоги.
   *
   * @param aSkAlarm {@link ISkAlarm} - тревога, чье состояние изменилось
   * @param aStateItem IAlarmStateHistoryItem - новое состояние
   */
  void onAlarmStateChanged( ISkAlarm aSkAlarm, ISkAnnounceThreadHistoryItem aStateItem );

}
