package org.toxsoft.uskat.s5.server.statistics.handlers;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.statistics.IS5StatisticInterval;

/**
 * Обработчик статистики по одному параметру: сумматор
 */
public final class S5StatisticSummator
    extends S5StatisticHandler {

  private static final long serialVersionUID = 157157L;

  private double summa;

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
  public S5StatisticSummator( String aId, EAtomicType aType, IS5StatisticInterval aInterval, IAtomicValue aInitValue ) {
    super( aId, aType, aInterval, aInitValue );
    if( aType != EAtomicType.INTEGER && aType != EAtomicType.FLOATING ) {
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
        int intValue = aValue.asInt();
        if( intValue == 0 ) {
          return false;
        }
        summa = summa + intValue;
        return true;
      case FLOATING:
        double doubleValue = aValue.asFloat();
        if( doubleValue == 0 ) {
          return false;
        }
        summa = summa + doubleValue;
        return true;
      case BOOLEAN:
      case NONE:
      case STRING:
      case TIMESTAMP:
      case VALOBJ:
        throw new TsInternalErrorRtException();
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
  }

  @Override
  protected IAtomicValue doCalcValue() {
    IAtomicValue retValue = IAtomicValue.NULL;
    if( type() == EAtomicType.INTEGER ) {
      retValue = avInt( (int)summa );
    }
    if( type() == EAtomicType.FLOATING ) {
      retValue = avFloat( summa );
    }
    return retValue;
  }

  @Override
  protected void doReset() {
    summa = 0;
  }
}
