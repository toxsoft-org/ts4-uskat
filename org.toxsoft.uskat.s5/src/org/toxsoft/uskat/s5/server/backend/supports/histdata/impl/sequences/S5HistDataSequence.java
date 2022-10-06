package org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences;

import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.IS5HistDataSequenceEdit;
import org.toxsoft.uskat.s5.server.sequences.ISequenceBlock;
import org.toxsoft.uskat.s5.server.sequences.ISequenceBlockEdit;
import org.toxsoft.uskat.s5.server.sequences.impl.S5Sequence;

/**
 * Реализация последовательности исторических данных объекта
 *
 * @author mvk
 */
public class S5HistDataSequence
    extends S5Sequence<ITemporalAtomicValue>
    implements IS5HistDataSequenceEdit {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор (используется для при загрузке блоков из dbms)
   *
   * @param aFactory {@link S5HistDataSequenceFactory} фабрика последовательностей значений
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aInterval {@link IQueryInterval} интервал времени последовательности, подробности в {@link #interval()}
   * @param aBlocks {@link IList}&lt;{@link ISequenceBlock}&gt; список блоков представляющих последовательность
   * @throw {@link TsNullArgumentRtException} любой аргумент = null
   */
  public S5HistDataSequence( S5HistDataSequenceFactory aFactory, Gwid aGwid, IQueryInterval aInterval,
      Iterable<ISequenceBlockEdit<ITemporalAtomicValue>> aBlocks ) {
    super( aFactory, aGwid, aInterval, aBlocks );
  }
}
