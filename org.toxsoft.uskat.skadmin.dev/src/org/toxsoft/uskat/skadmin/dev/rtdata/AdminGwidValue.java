package org.toxsoft.uskat.skadmin.dev.rtdata;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.time.impl.TemporalValueBase;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Именованное(через gwid) значение
 *
 * @author mvk
 */
final class AdminGwidValue
    extends TemporalValueBase<IAtomicValue>
    implements ITemporalAtomicValue {

  private static final long serialVersionUID = 157157L;

  private final Gwid gwid;

  /**
   * Конструктор.
   *
   * @param aGwid {@link Gwid} - идентификатор данного которому принадлежит значение
   * @param aTimestamp long метка времени значения
   * @param aValue {@link IAtomicValue} - атомарное значение
   * @throws TsNullArgumentRtException aValue = null
   */
  AdminGwidValue( Gwid aGwid, long aTimestamp, IAtomicValue aValue ) {
    super( aTimestamp, aValue );
    gwid = TsNullArgumentRtException.checkNull( aGwid );
  }

  /**
   * Конструктор.
   *
   * @param aGwid {@link Gwid} - идентификатор данного которому принадлежит значение
   * @param aTemporalValue {@link ITemporalAtomicValue} - атомарное значение с меткой времени
   * @throws TsNullArgumentRtException aValue = null
   */
  AdminGwidValue( Gwid aGwid, ITemporalAtomicValue aTemporalValue ) {
    super( aTemporalValue.timestamp(), aTemporalValue.value() );
    gwid = TsNullArgumentRtException.checkNull( aGwid );
  }

  /**
   * Возвращает идентификатор данного которому принадлежит значение
   *
   * @return {@link Gwid} идентификатор данного
   */
  Gwid gwid() {
    return gwid;
  }
}
