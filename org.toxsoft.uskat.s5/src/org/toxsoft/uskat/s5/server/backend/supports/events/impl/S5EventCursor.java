package org.toxsoft.uskat.s5.server.backend.supports.events.impl;

import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.s5.server.backend.supports.events.IS5BackendEventCursor;
import org.toxsoft.uskat.s5.server.backend.supports.events.sequences.IS5EventSequence;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceBlock;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceCursor;

/**
 * Реализация {@link IS5BackendEventCursor}
 *
 * @author mvk
 */
public class S5EventCursor
    extends S5SequenceCursor<SkEvent>
    implements IS5BackendEventCursor {

  /**
   * Создание курсора для последовательности событий
   *
   * @param aSequence {@link IS5EventSequence} последовательность событий
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5EventCursor( IS5EventSequence aSequence ) {
    super( aSequence );
  }

  /**
   * Создание курсора для блока событий
   *
   * @param aBlock {@link IS5SequenceBlock} блок событий
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5EventCursor( IS5SequenceBlock<SkEvent> aBlock ) {
    super( aBlock );
  }

  // ------------------------------------------------------------------------------------
  // S5SequenceCursor
  //
  @Override
  protected SkEvent doGetCurrentValue() {
    IS5SequenceBlock<SkEvent> block = currentBlock();
    return block.getValue( currentValueIndex() );
  }
}
