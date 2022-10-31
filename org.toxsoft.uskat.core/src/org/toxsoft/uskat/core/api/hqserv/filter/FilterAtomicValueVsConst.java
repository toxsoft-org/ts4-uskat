package org.toxsoft.uskat.core.api.hqserv.filter;

import static org.toxsoft.core.tslib.bricks.filter.std.IStdTsFiltersConstants.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.math.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.filter.*;
import org.toxsoft.core.tslib.bricks.filter.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Filter compares {@link IAtomicValue} against given constant.
 * <p>
 * Method {@link #accept(IAtomicValue)} throws only {@link TsNullArgumentRtException}. In case if {@link EAtomicType} of
 * value and constant does not allows comparison method simply returns false.
 * <p>
 * When comparing, checked value is left operand and the constant is the right operand of comparison.
 *
 * @author hazard157
 */
public final class FilterAtomicValueVsConst
    implements ITsFilter<IAtomicValue> {

  /**
   * Filter type ID {@link ITsSingleFilterFactory#id()},
   */
  public static final String TYPE_ID = STD_FILTERID_ID_PREFIX + ".AtomicValueVsConst"; //$NON-NLS-1$

  /**
   * The filter factory singleton.
   */
  public static final ITsSingleFilterFactory<IAtomicValue> FACTORY =
      new AbstractTsSingleFilterFactory<>( TYPE_ID, IAtomicValue.class ) {

        @Override
        protected ITsFilter<IAtomicValue> doCreateFilter( IOptionSet aParams ) {
          String opId = aParams.getStr( PID_OP );
          EAvCompareOp op = EAvCompareOp.findById( opId );
          IAtomicValue constant = aParams.getValue( PID_CONSTANT );
          return new FilterAtomicValueVsConst( op, constant );
        }
      };

  private static final String PID_OP       = "op";       //$NON-NLS-1$
  private static final String PID_CONSTANT = "constant"; //$NON-NLS-1$

  private final EAvCompareOp op;
  private final IAtomicValue constant;

  /**
   * Constructor.
   *
   * @param aOp {@link EAvCompareOp} - comparison operation kind
   * @param aConst {@link IAtomicValue} - constant to compare to the value
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public FilterAtomicValueVsConst( EAvCompareOp aOp, IAtomicValue aConst ) {
    TsNullArgumentRtException.checkNulls( aOp, aConst );
    op = aOp;
    constant = aConst;
  }

  /**
   * Creates filter parameters set {@link ITsCombiFilterParams} for {@link #FACTORY} to create the filter instance.
   *
   * @param aOp {@link EAvCompareOp} - comparison operation kind
   * @param aConst {@link IAtomicValue} - constant to compare to the value
   * @return {@link ITsCombiFilterParams} - created instance of the filter parameters
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static ITsCombiFilterParams makeFilterParams( EAvCompareOp aOp, IAtomicValue aConst ) {
    TsNullArgumentRtException.checkNulls( aOp, aConst );
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
   * Returns the comparison operation kind.
   *
   * @return {@link EAvCompareOp} - the comparison operation kind
   */
  public EAvCompareOp op() {
    return op;
  }

  /**
   * Returns the constant to compare to the value.
   *
   * @return {@link IAtomicValue} - the constant to compare
   */
  public IAtomicValue constant() {
    return constant;
  }

  // ------------------------------------------------------------------------------------
  // ITsFilter
  //

  @Override
  public boolean accept( IAtomicValue aObj ) {
    IAvComparator c = AvComparatorStrict.INSTANCE;
    if( !c.canCompare( aObj, op, constant ).isError() ) {
      return c.avCompare( aObj, op, constant );
    }
    return false;
  }

}
