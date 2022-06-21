package org.toxsoft.uskat.core.api.cmdserv;

import org.toxsoft.core.tslib.bricks.events.change.*;
import org.toxsoft.core.tslib.bricks.time.*;

/**
 * The command.
 * <p>
 * Timestamp {@link #timestamp()} returns timestamp of {@link #cmd()}.
 *
 * @author hazard157
 */
public interface ISkCommand
    extends ITemporal<ISkCommand> {

  /**
   * Returns information about the sent command.
   *
   * @return {@link IDtoCommand} - the command information
   */
  IDtoCommand cmd();

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

  default SkCommandState state() {
    return statesHistory().last();
  }

  default boolean isComplete() {
    return statesHistory().last().state().isComplete();
  }

}
