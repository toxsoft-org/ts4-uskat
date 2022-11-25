package org.toxsoft.uskat.alarms.lib;

import org.toxsoft.core.tslib.bricks.strid.IStridable;

/**
 * Описание тревоги.
 *
 * @author goga, dima
 */
public interface ISkAlarmDef
    extends IStridable {

  /**
   * Возвращает приоритет (важность) тревоги.
   *
   * @return {@link EAlarmPriority} - приоритет (важность) тревоги
   */
  EAlarmPriority priority();

  /**
   * @return типовое сообщение аларма
   */
  String message();

  // пока все нижеприведенные TODO игнорируем - непонятно даже, понядобятся ли они?
  // TODO способы извещения
  // TODO способы отображения
  // TODO описать способы (авто)генерации ???
  // TODO описать срез на момент генерации ???

}
