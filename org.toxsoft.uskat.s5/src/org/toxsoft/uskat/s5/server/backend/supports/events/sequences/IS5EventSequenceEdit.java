package org.toxsoft.uskat.s5.server.backend.supports.events.sequences;

import org.toxsoft.uskat.s5.server.sequences.IS5SequenceEdit;

import ru.uskat.common.dpu.rt.events.SkEvent;

/**
 * Последовательность событий одного конкретного объекта s5 с возможностью редактирования
 *
 * @author mvk
 */
public interface IS5EventSequenceEdit
    extends IS5SequenceEdit<SkEvent>, IS5EventSequence {
  // nop
}
