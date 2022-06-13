package org.toxsoft.uskat.core.api.cmdserv;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;

/**
 * DPU команды (без состояния).
 *
 * @author goga
 */
public interface IDtoCommand
    extends ITimestampable {

  /**
   * Возвращает уникальный в системе <b>идентификатор экземпляра</b> команды.
   * <p>
   * Идентификатор уникален среди всех экземпляров команд за все время работы сстемы.
   *
   * @return String - идентификатор (ИД-путь) экземпляра команды
   */
  String id();

  /**
   * Returns the command GWID including the destination object skid and command identifier.
   *
   * @return String - the event concrete GWID of kind {@link EGwidKind#GW_CMD}
   */
  Gwid cmdGwid();

  /**
   * Returns command author object identifier.
   *
   * @return {@link Skid} - the command author object
   */
  Skid authorSkid();

  /**
   * Returns the command arguments values.
   *
   * @return {@link IOptionSet} - the command arguments values
   */
  IOptionSet argValues();

}
