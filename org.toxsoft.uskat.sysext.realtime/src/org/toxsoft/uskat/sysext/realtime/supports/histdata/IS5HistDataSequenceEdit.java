package org.toxsoft.uskat.sysext.realtime.supports.histdata;

import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceEdit;

/**
 * Последовательность значений одного исторического данного с возможностью редактирования
 *
 * @author mvk
 */
public interface IS5HistDataSequenceEdit
    extends IS5SequenceEdit<ITemporalAtomicValue>, IS5HistDataSequence {
  // nop
}
