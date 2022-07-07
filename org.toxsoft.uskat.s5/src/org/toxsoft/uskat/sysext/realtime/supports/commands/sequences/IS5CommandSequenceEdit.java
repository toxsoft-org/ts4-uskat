package org.toxsoft.uskat.sysext.realtime.supports.commands.sequences;

import org.toxsoft.uskat.core.api.cmdserv.IDtoCompletedCommand;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceEdit;

/**
 * Последовательность команд (история) одного конкретного объекта s5 с возможностью редактирования
 *
 * @author mvk
 */
public interface IS5CommandSequenceEdit
    extends IS5SequenceEdit<IDtoCompletedCommand>, IS5CommandSequence {
  // nop
}
