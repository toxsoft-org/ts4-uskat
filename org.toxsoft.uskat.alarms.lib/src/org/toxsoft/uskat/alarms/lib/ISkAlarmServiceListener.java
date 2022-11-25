package org.toxsoft.uskat.alarms.lib;

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
  default void onAlarm( ISkAlarm aSkAlarm ) {
    // nop
  }

  /**
   * Вызывается при изменении состояния обработки тревоги.
   *
   * @param aSkAlarm {@link ISkAlarm} - тревога, чье состояние изменилось
   * @param aStateItem IAlarmStateHistoryItem - новое состояние
   */
  default void onAlarmStateChanged( ISkAlarm aSkAlarm, ISkAlarmThreadHistoryItem aStateItem ) {
    // nop
  }

}
