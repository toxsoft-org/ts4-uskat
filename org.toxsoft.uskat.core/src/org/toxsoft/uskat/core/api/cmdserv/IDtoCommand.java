package org.toxsoft.uskat.core.api.cmdserv;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;

/**
 * Information about sent command.
 *
 * @author hazard157
 */
public interface IDtoCommand
    extends ITimestampable {

  /**
   * Returns an unique command instance identifier.
   * <p>
   * This ID is unique among all commands of all time in the particular system.
   *
   * @return String - command instance unique ID (an IDpath)
   */
  String id();

  /**
   * Returns the command GWID including the destination object skid and command identifier.
   *
   * @return String - the concrete GWID of kind {@link EGwidKind#GW_CMD}
   */
  Gwid cmdGwid();

  /**
   * Returns command author object identifier.
   *
   * @return {@link Skid} - the command author object SKID
   */
  Skid authorSkid();

  /**
   * Returns the command arguments values.
   *
   * @return {@link IOptionSet} - the command arguments values
   */
  IOptionSet argValues();

}
