package org.toxsoft.uskat.s5.server.backend.supports.commands.impl;

import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.cmdserv.IDtoCompletedCommand;
import org.toxsoft.uskat.s5.server.backend.supports.commands.sequences.IS5CommandSequenceEdit;
import org.toxsoft.uskat.s5.server.backend.supports.commands.sequences.S5CommandCursor;
import org.toxsoft.uskat.s5.server.sequences.*;
import org.toxsoft.uskat.s5.server.sequences.impl.S5Sequence;

/**
 * Реализация последовательности команд одного объекта
 *
 * @author mvk
 */
public class S5CommandSequence
    extends S5Sequence<IDtoCompletedCommand>
    implements IS5CommandSequenceEdit {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор (используется для при загрузке блоков из dbms)
   *
   * @param aFactory {@link S5CommandSequenceFactory} фабрика последовательностей событий
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aInterval {@link IQueryInterval} интервал времени последовательности, подробности в {@link #interval()}
   * @param aCommands {@link IList}&lt;{@link IS5SequenceBlock}&gt; список блоков представляющих последовательность
   * @throw {@link TsNullArgumentRtException} любой аргумент = null
   */
  public S5CommandSequence( S5CommandSequenceFactory aFactory, Gwid aGwid, IQueryInterval aInterval,
      Iterable<IS5SequenceBlockEdit<IDtoCompletedCommand>> aCommands ) {
    super( aFactory, aGwid, aInterval, aCommands );
  }

  // ------------------------------------------------------------------------------------
  // IS5Sequence
  //
  @Override
  public IS5SequenceCursor<IDtoCompletedCommand> createCursor() {
    return new S5CommandCursor( this );
  }
}
