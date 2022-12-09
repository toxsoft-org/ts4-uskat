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
   * @param aAlarm {@link ISkAlarm} - новая тревога
   */
  default void onAlarm( ISkAlarm aAlarm ) {
    // nop
  }

  /**
   * Вызывается при изменении состояния обработки тревоги.
   *
   * @param aAlarm {@link ISkAlarm} - тревога, чье состояние изменилось
   * @param aStateItem IAlarmStateHistoryItem - новое состояние
   */
  default void onAlarmStateChanged( ISkAlarm aAlarm, ISkAlarmThreadHistoryItem aStateItem ) {
    // nop
  }

}
