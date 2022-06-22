package org.toxsoft.uskat.core.api.cmdserv;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.events.change.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;

/**
 * The command.
 *
 * @author hazard157
 */
public interface ISkCommand
    extends ITemporal<ISkCommand> {

  /**
   * Returns an unique command instance identifier.
   * <p>
   * This ID is unique among all commands of all time in the particular system.
   *
   * @return String - command instance unique ID (an IDpath)
   */
  String instanceId();

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

  /**
   * Returns command state change history.
   *
   * @return {@link ITimedList}&lt;{@link SkCommandState}&gt; - time-ordered list of state changes
   */
  ITimedList<SkCommandState> statesHistory();

  /**
   * Returns the states history change eventer.
   * <p>
   * Only one event happens to the list - a new element is being added to the end of the list.
   *
   * @return {@link IGenericChangeEventer} - the eventer
   */
  IGenericChangeEventer stateEventer();

  // ------------------------------------------------------------------------------------
  // inline methods for convinience

  /**
   * Returns the current state oof the command.
   *
   * @return {@link SkCommandState} - current state of the command
   */
  default SkCommandState state() {
    return statesHistory().last();
  }

  /**
   * Determines if command has been completed.
   *
   * @return boolean - completed command flag
   */
  default boolean isComplete() {
    return statesHistory().last().state().isComplete();
  }

}
