package org.toxsoft.uskat.sysext.realtime.supports.commands.sequences;

import org.toxsoft.uskat.s5.server.sequences.IS5SequenceEdit;

import ru.uskat.common.dpu.rt.cmds.IDpuCompletedCommand;

/**
 * Последовательность команд (история) одного конкретного объекта s5 с возможностью редактирования
 *
 * @author mvk
 */
public interface IS5CommandSequenceEdit
    extends IS5SequenceEdit<IDpuCompletedCommand>, IS5CommandSequence {
  // nop
}
