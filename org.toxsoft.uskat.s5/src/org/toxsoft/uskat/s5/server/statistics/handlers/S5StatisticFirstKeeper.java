package org.toxsoft.uskat.s5.server.statistics.handlers;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.statistics.IS5StatisticInterval;

/**
 * Обработчик статистики по одному параметру: хранитель первого значения на интервале
 */
public final class S5StatisticFirstKeeper
    extends S5StatisticHandler {

  private static final long serialVersionUID = 157157L;

  private IAtomicValue first;

  /**
   * Конструктор
   *
   * @param aId String идентификатор параметра
   * @param aType {@link EAtomicType} атомарный тип поступающих значений
   * @param aInterval {@link IS5StatisticInterval} интервал статистической обработки
   * @param aInitValue {@link IAtomicValue} начальное статистическое значение при создании обработчика.
   *          {@link IAtomicValue#NULL}: нет значения по умолчанию
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5StatisticFirstKeeper( String aId, EAtomicType aType, IS5StatisticInterval aInterval,
      IAtomicValue aInitValue ) {
    super( aId, aType, aInterval, aInitValue );
  }

  // ------------------------------------------------------------------------------------
  // S5StatisticHandler
  //
  @Override
  protected boolean doOnValue( IAtomicValue aValue ) {
    switch( aValue.atomicType() ) {
      case INTEGER:
      case FLOATING:
      case STRING:
      case TIMESTAMP:
      case BOOLEAN:
      case VALOBJ:
        if( first != IAtomicValue.NULL ) {
          return false;
        }
        first = aValue;
        return true;
      case NONE:
        throw new TsInternalErrorRtException();
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
  }

  @Override
  protected IAtomicValue doCalcValue() {
    IAtomicValue retValue = first;
    return retValue;
  }

  @Override
  protected void doReset() {
    first = IAtomicValue.NULL;
  }
}
