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
import org.toxsoft.uskat.core.api.cmdserv.IDtoCompletedCommand;
import org.toxsoft.uskat.core.api.cmdserv.ISkCommand;

/**
 * Фильтр сравнения одного из аргументов {@link ISkCommand#argValues()} с константой.
 * <p>
 * Отсутствие в {@link ISkCommand#argValues()} аргумента {@link #argId()} рассматривается как значение
 * {@link IAtomicValue#NULL}.
 *
 * @author hazard157
 */
public final class FilterCommandArgVsConst
    implements ITsFilter<IDtoCompletedCommand> {

  /**
   * Идентификатор типа фильтра {@link ITsSingleFilterFactory#id()},
   */
  public static final String TYPE_ID = STD_FILTERID_ID_PREFIX + ".CmdArgVsConst"; //$NON-NLS-1$

  /**
   * Фабрика создания фильтра из значений параметров.
   */
  public static final ITsSingleFilterFactory<IDtoCompletedCommand> FACTORY =
      new AbstractTsSingleFilterFactory<>( TYPE_ID, IDtoCompletedCommand.class ) {

        @Override
        protected ITsFilter<IDtoCompletedCommand> doCreateFilter( IOptionSet aParams ) {
          String argId = aParams.getStr( PID_ARG_ID );
          String opId = aParams.getStr( PID_OP );
          EAvCompareOp op = EAvCompareOp.findById( opId );
          IAtomicValue constant = aParams.getValue( PID_CONSTANT );
          return new FilterCommandArgVsConst( argId, op, constant );
        }
      };

  private static final String PID_ARG_ID   = "argId";    //$NON-NLS-1$
  private static final String PID_OP       = "op";       //$NON-NLS-1$
  private static final String PID_CONSTANT = "constant"; //$NON-NLS-1$

  private final String       argId;
  private final EAvCompareOp op;
  private final IAtomicValue constant;

  /**
   * Конструктор.
   *
   * @param aArgId String - идентификатор (ИД-путь) проверяемого аргумента команды
   * @param aOp {@link EAvCompareOp} - способ сравнения
   * @param aConst {@link IAtomicValue} - константа для сравнения
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException идентификатор не ИД-путь
   */
  public FilterCommandArgVsConst( String aArgId, EAvCompareOp aOp, IAtomicValue aConst ) {
    TsNullArgumentRtException.checkNulls( aOp, aConst );
    argId = StridUtils.checkValidIdPath( aArgId );
    op = aOp;
    constant = aConst;
  }

  /**
   * Создает набор параметров {@link ITsCombiFilterParams} для создания фильтра фабрикой {@link #FACTORY}.
   *
   * @param aArgId String - идентификатор (ИД-путь) проверяемого аргумента команды
   * @param aOp {@link EAvCompareOp} - способ сравнения
   * @param aConst {@link IAtomicValue} - константа для сравнения
   * @return {@link ITsCombiFilterParams} - параметры для создания фильтра фабрикой
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException идентификатор не ИД-путь
   */
  public static ITsCombiFilterParams makeFilterParams( String aArgId, EAvCompareOp aOp, IAtomicValue aConst ) {
    StridUtils.checkValidIdPath( aArgId );
    TsNullArgumentRtException.checkNulls( aOp, aConst );
    ITsSingleFilterParams sp = TsSingleFilterParams.create( TYPE_ID, //
        PID_ARG_ID, aArgId, //
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
   * Возвращает идентификатор проверяемого аргумента команды.
   *
   * @return String - идентификатор (ИД-путь) проверяемого аргумента.
   */
  public String argId() {
    return argId;
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
  public boolean accept( IDtoCompletedCommand aCommand ) {
    IAvComparator c = AvComparatorStrict.INSTANCE;
    IAtomicValue argVal = aCommand.cmd().argValues().getValue( argId, IAtomicValue.NULL );
    return c.avCompare( argVal, op, constant );
  }

}
