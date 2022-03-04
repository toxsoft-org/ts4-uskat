package org.toxsoft.uskat.s5.server.statistics.handlers;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.math.AvComparatorStrict;
import org.toxsoft.core.tslib.av.math.EAvCompareOp;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.statistics.IS5StatisticInterval;

/**
 * Обработчик статистики по одному параметру: поиск минимума на интервале
 */
public final class S5StatisticMinimumSeeker
    extends S5StatisticHandler {

  private static final long serialVersionUID = 157157L;

  private IAtomicValue minimum;

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
  public S5StatisticMinimumSeeker( String aId, EAtomicType aType, IS5StatisticInterval aInterval,
      IAtomicValue aInitValue ) {
    super( aId, aType, aInterval, aInitValue );
    if( aType != EAtomicType.INTEGER && //
        aType != EAtomicType.FLOATING && //
        aType != EAtomicType.STRING && //
        aType != EAtomicType.TIMESTAMP ) {
      throw new TsIllegalArgumentRtException();
    }
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
        if( minimum != IAtomicValue.NULL && //
            !AvComparatorStrict.INSTANCE.avCompare( aValue, EAvCompareOp.LT, minimum ) ) {
          return false;
        }
        minimum = aValue;
        return true;
      case BOOLEAN:
      case NONE:
      case VALOBJ:
        throw new TsInternalErrorRtException();
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
  }

  @Override
  protected IAtomicValue doCalcValue() {
    IAtomicValue retValue = minimum;
    return retValue;
  }

  @Override
  protected void doReset() {
    minimum = IAtomicValue.NULL;
  }
}
