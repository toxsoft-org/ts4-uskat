package org.toxsoft.uskat.s5.server.backend.supports.events.sequences;

import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceEdit;

/**
 * Последовательность событий одного конкретного объекта s5 с возможностью редактирования
 *
 * @author mvk
 */
public interface IS5EventSequenceEdit
    extends IS5SequenceEdit<SkEvent>, IS5EventSequence {
  // nop
}
