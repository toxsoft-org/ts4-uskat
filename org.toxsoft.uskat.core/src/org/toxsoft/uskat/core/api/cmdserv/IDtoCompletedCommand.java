package org.toxsoft.uskat.core.api.cmdserv;

import org.toxsoft.core.tslib.bricks.time.*;

/**
 * Information about completed command.
 * <p>
 * Obviously this DTO contains exactly the same information as {@link ISkCommand}.
 * <p>
 * Timestamp {@link #timestamp()} returns timestamp of {@link #cmd()}.
 *
 * @author hazard157
 */
public interface IDtoCompletedCommand
    extends ITemporal<IDtoCompletedCommand> {

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

}
