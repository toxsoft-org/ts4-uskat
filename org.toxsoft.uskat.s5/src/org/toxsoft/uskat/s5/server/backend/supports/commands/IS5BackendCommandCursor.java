package org.toxsoft.uskat.s5.server.backend.supports.commands;

import org.toxsoft.uskat.core.api.cmdserv.IDtoCompletedCommand;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceCursor;

/**
 * Курсор последовательности команд
 *
 * @author mvk
 */
public interface IS5BackendCommandCursor
    extends IS5SequenceCursor<IDtoCompletedCommand> {
  // nop
}
