package org.toxsoft.uskat.alarms.lib.filters;

import static org.toxsoft.core.tslib.bricks.filter.std.IStdTsFiltersConstants.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.impl.AvUtils;
import org.toxsoft.core.tslib.av.math.*;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.filter.*;
import org.toxsoft.core.tslib.bricks.filter.impl.*;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.alarms.lib.ISkAlarm;

/**
 * Фильтр по полю {@link ISkAlarm#message()}.
 *
 * @author mvk
 */
public final class SkAlarmFilterByMessage
    implements ITsFilter<ISkAlarm> {

  /**
   * Идентификатор типа фильтра {@link ITsSingleFilterFactory#id()},
   */
  public static final String TYPE_ID = STD_FILTERID_ID_PREFIX + ".AlarmFilterByMessage"; //$NON-NLS-1$

  /**
   * Фабрика создания фильтра из значений параметров.
   */
  public static final ITsSingleFilterFactory<ISkAlarm> FACTORY =
      new AbstractTsSingleFilterFactory<>( TYPE_ID, ISkAlarm.class ) {

        @Override
        protected ITsFilter<ISkAlarm> doCreateFilter( IOptionSet aParams ) {
          String opId = aParams.getStr( PID_OP );
          EAvCompareOp op = EAvCompareOp.findById( opId );
          IAtomicValue constant = aParams.getValue( PID_CONSTANT );
          return new SkAlarmFilterByMessage( op, constant );
        }
      };

  private static final String PID_OP       = "op";       //$NON-NLS-1$
  private static final String PID_CONSTANT = "constant"; //$NON-NLS-1$

  private final EAvCompareOp op;
  private final IAtomicValue constant;

  /**
   * Конструктор.
   *
   * @param aOp {@link EAvCompareOp} - способ сравнения
   * @param aConst {@link IAtomicValue} - константа для сравнения имеющая тип {@link EAtomicType#STRING}
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public SkAlarmFilterByMessage( EAvCompareOp aOp, IAtomicValue aConst ) {
    TsNullArgumentRtException.checkNulls( aOp, aConst );
    TsIllegalArgumentRtException.checkFalse( aConst.atomicType() == EAtomicType.STRING );
    op = aOp;
    constant = aConst;
  }

  /**
   * Создает набор параметров {@link ITsCombiFilterParams} для создания фильтра фабрикой {@link #FACTORY}.
   *
   * @param aOp {@link EAvCompareOp} - способ сравнения
   * @param aConst {@link IAtomicValue} - константа для сравнения имеющая тип {@link EAtomicType#STRING}
   * @return {@link ITsCombiFilterParams} - параметры для создания фильтра фабрикой
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static ITsCombiFilterParams makeFilterParams( EAvCompareOp aOp, IAtomicValue aConst ) {
    TsNullArgumentRtException.checkNulls( aOp, aConst );
    TsIllegalArgumentRtException.checkFalse( aConst.atomicType() == EAtomicType.STRING );
    ITsSingleFilterParams sp = TsSingleFilterParams.create( TYPE_ID, //
        PID_OP, aOp.id(), //
        PID_CONSTANT, aConst //
    );
    ITsCombiFilterParams p = TsCombiFilterParams.createSingle( sp );
    return p;
  }

  // ------------------------------------------------------------------------------------
  // API
  //
  /**
   * Возвращает способ сравнения.
   *
   * @return {@link EAvCompareOp} - способ сравнения
   */
  public EAvCompareOp op() {
    return op;
  }

  /**
   * Возвращает константа для сравнения.
   *
   * @return {@link IAtomicValue} - константа для сравнения
   */
  public IAtomicValue constant() {
    return constant;
  }

  // ------------------------------------------------------------------------------------
  // ITsFilter
  //
  @Override
  public boolean accept( ISkAlarm aAlarm ) {
    IAvComparator c = AvComparatorStrict.INSTANCE;
    IAtomicValue defId = AvUtils.avStr( aAlarm.message() );
    return c.avCompare( defId, op, constant );
  }

}
