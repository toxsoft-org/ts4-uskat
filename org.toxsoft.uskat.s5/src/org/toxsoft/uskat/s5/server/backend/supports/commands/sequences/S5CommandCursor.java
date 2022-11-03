package org.toxsoft.uskat.s5.server.backend.supports.commands.sequences;

import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.cmdserv.IDtoCompletedCommand;
import org.toxsoft.uskat.s5.server.backend.supports.commands.IS5BackendCommandCursor;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceBlock;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceCursor;

/**
 * Реализация {@link IS5BackendCommandCursor}
 *
 * @author mvk
 */
public class S5CommandCursor
    extends S5SequenceCursor<IDtoCompletedCommand>
    implements IS5BackendCommandCursor {

  /**
   * Создание курсора для последовательности команд
   *
   * @param aSequence {@link IS5CommandSequence} последовательность команд
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5CommandCursor( IS5CommandSequence aSequence ) {
    super( aSequence );
  }

  /**
   * Создание курсора для блока команд
   *
   * @param aBlock {@link IS5SequenceBlock} блок команд
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5CommandCursor( IS5SequenceBlock<IDtoCompletedCommand> aBlock ) {
    super( aBlock );
  }

  // ------------------------------------------------------------------------------------
  // S5SequenceCursor
  //
  @Override
  protected IDtoCompletedCommand doGetCurrentValue() {
    IS5SequenceBlock<IDtoCompletedCommand> block = currentBlock();
    return block.getValue( currentValueIndex() );
  }
}
