package org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences;

import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.IS5HistDataBlockReader;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceBlock;

/**
 * Блок последовательности хранимых данных
 *
 * @author mvk
 */
public interface IS5HistDataBlock
    extends IS5SequenceBlock<ITemporalAtomicValue>, IS5HistDataBlockReader {
  // nop
}
