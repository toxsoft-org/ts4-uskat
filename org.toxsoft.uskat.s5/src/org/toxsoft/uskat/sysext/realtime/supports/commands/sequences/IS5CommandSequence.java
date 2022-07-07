package org.toxsoft.uskat.sysext.realtime.supports.commands.sequences;

import org.toxsoft.uskat.core.api.cmdserv.IDtoCompletedCommand;
import org.toxsoft.uskat.s5.server.sequences.IS5Sequence;

/**
 * Последовательность команд (история) одного конкретного объекта s5.
 *
 * @author mvk
 */
public interface IS5CommandSequence
    extends IS5Sequence<IDtoCompletedCommand> {
  // nop
}
