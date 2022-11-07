package org.toxsoft.uskat.core.api.hqserv.filter;

import static org.toxsoft.core.tslib.bricks.filter.std.IStdTsFiltersConstants.*;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.math.*;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.filter.*;
import org.toxsoft.core.tslib.bricks.filter.impl.*;
import org.toxsoft.core.tslib.bricks.strid.impl.StridUtils;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.evserv.SkEvent;

/**
 * Фильтр сравнения одного из параметров {@link SkEvent#paramValues()} с константой.
 * <p>
 * Отсутствие в {@link SkEvent#paramValues()} параметра {@link #paramId()} рассматривается как значение
 * {@link IAtomicValue#NULL}.
 *
 * @author hazard157
 */
public final class FilterEventParamVsConst
    implements ITsFilter<SkEvent> {

  /**
   * Идентификатор типа фильтра {@link ITsSingleFilterFactory#id()},
   */
  public static final String TYPE_ID = STD_FILTERID_ID_PREFIX + ".EventParamVsConst"; //$NON-NLS-1$

  /**
   * Фабрика создания фильтра из значений параметров.
   */
  public static final ITsSingleFilterFactory<SkEvent> FACTORY =
      new AbstractTsSingleFilterFactory<>( TYPE_ID, SkEvent.class ) {

        @Override
        protected ITsFilter<SkEvent> doCreateFilter( IOptionSet aParams ) {
          String paramId = aParams.getStr( PID_PARAM_ID );
          String opId = aParams.getStr( PID_OP );
          EAvCompareOp op = EAvCompareOp.findById( opId );
          IAtomicValue constant = aParams.getValue( PID_CONSTANT );
          return new FilterEventParamVsConst( paramId, op, constant );
        }
      };

  private static final String PID_PARAM_ID = "paramId";  //$NON-NLS-1$
  private static final String PID_OP       = "op";       //$NON-NLS-1$
  private static final String PID_CONSTANT = "constant"; //$NON-NLS-1$

  private final String       paramId;
  private final EAvCompareOp op;
  private final IAtomicValue constant;

  /**
   * Конструктор.
   *
   * @param aParamId String - идентификатор (ИД-путь) проверяемого параметра
   * @param aOp {@link EAvCompareOp} - способ сравнения
   * @param aConst {@link IAtomicValue} - константа для сравнения
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException идентификатор не ИД-путь
   */
  public FilterEventParamVsConst( String aParamId, EAvCompareOp aOp, IAtomicValue aConst ) {
    TsNullArgumentRtException.checkNulls( aOp, aConst );
    paramId = StridUtils.checkValidIdPath( aParamId );
    op = aOp;
    constant = aConst;
  }

  /**
   * Создает набор параметров {@link ITsCombiFilterParams} для создания фильтра фабрикой {@link #FACTORY}.
   *
   * @param aParamId String - идентификатор (ИД-путь) проверяемого параметра события
   * @param aOp {@link EAvCompareOp} - способ сравнения
   * @param aConst {@link IAtomicValue} - константа для сравнения
   * @return {@link ITsCombiFilterParams} - параметры для создания фильтра фабрикой
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException идентификатор не ИД-путь
   */
  public static ITsCombiFilterParams makeFilterParams( String aParamId, EAvCompareOp aOp, IAtomicValue aConst ) {
    StridUtils.checkValidIdPath( aParamId );
    TsNullArgumentRtException.checkNulls( aOp, aConst );
    ITsSingleFilterParams sp = TsSingleFilterParams.create( TYPE_ID, //
        PID_PARAM_ID, aParamId, //
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
   * Возвращает идентификатор проверяемого параметра.
   *
   * @return String - идентификатор (ИД-путь) проверяемого параметра
   */
  public String paramId() {
    return paramId;
  }

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
  public boolean accept( SkEvent aEvent ) {
    IAvComparator c = AvComparatorStrict.INSTANCE;
    IAtomicValue paramVal = aEvent.paramValues().getValue( paramId, IAtomicValue.NULL );
    return c.avCompare( paramVal, op, constant );
  }

}
