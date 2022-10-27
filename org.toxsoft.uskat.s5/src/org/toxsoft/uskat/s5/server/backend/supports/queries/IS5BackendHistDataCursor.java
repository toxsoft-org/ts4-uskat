package org.toxsoft.uskat.s5.server.backend.supports.queries;

import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceCursor;

/**
 * Курсора хранимых даных
 *
 * @author mvk
 */
public interface IS5BackendHistDataCursor
    extends IS5SequenceCursor<ITemporalAtomicValue> {
  // nop
}
