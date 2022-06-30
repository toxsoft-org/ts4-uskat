package org.toxsoft.uskat.s5.server.backend.supports.events.impl;

import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.s5.server.backend.supports.events.sequences.IS5EventSequenceEdit;
import org.toxsoft.uskat.s5.server.sequences.ISequenceBlock;
import org.toxsoft.uskat.s5.server.sequences.ISequenceBlockEdit;
import org.toxsoft.uskat.s5.server.sequences.impl.S5Sequence;

/**
 * Реализация последовательности событий одного объекта
 *
 * @author mvk
 */
public class S5EventSequence
    extends S5Sequence<SkEvent>
    implements IS5EventSequenceEdit {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор (используется для при загрузке блоков из dbms)
   *
   * @param aFactory {@link S5EventSequenceFactory} фабрика последовательностей событий
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aInterval {@link IQueryInterval} интервал времени последовательности, подробности в {@link #interval()}
   * @param aEvents {@link IList}&lt;{@link ISequenceBlock}&gt; список блоков представляющих последовательность
   * @throw {@link TsNullArgumentRtException} любой аргумент = null
   */
  public S5EventSequence( S5EventSequenceFactory aFactory, Gwid aGwid, IQueryInterval aInterval,
      Iterable<ISequenceBlockEdit<SkEvent>> aEvents ) {
    super( aFactory, aGwid, aInterval, aEvents );
  }

}
