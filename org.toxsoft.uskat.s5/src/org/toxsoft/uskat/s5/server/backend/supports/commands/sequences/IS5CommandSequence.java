package org.toxsoft.uskat.s5.server.backend.supports.commands.sequences;

import org.toxsoft.uskat.s5.server.sequences.IS5Sequence;

import ru.uskat.common.dpu.rt.cmds.IDpuCompletedCommand;

/**
 * Последовательность команд (история) одного конкретного объекта s5.
 *
 * @author mvk
 */
public interface IS5CommandSequence
    extends IS5Sequence<IDpuCompletedCommand> {
  // nop
}
