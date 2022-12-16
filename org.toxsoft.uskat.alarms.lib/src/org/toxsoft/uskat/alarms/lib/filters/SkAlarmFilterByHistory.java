package org.toxsoft.uskat.alarms.lib.filters;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.bricks.filter.std.IStdTsFiltersConstants.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.math.*;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.filter.*;
import org.toxsoft.core.tslib.bricks.filter.impl.*;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.alarms.lib.ISkAlarm;
import org.toxsoft.uskat.alarms.lib.ISkAlarmThreadHistoryItem;

/**
 * Фильтр по полю {@link ISkAlarm#history()}.
 *
 * @author mvk
 */
public final class SkAlarmFilterByHistory
    implements ITsFilter<ISkAlarm> {

  /**
   * Идентификатор типа фильтра {@link ITsSingleFilterFactory#id()},
   */
  public static final String TYPE_ID = STD_FILTERID_ID_PREFIX + ".AlarmFilterByHistory"; //$NON-NLS-1$

  /**
   * Фабрика создания фильтра из значений параметров.
   */
  public static final ITsSingleFilterFactory<ISkAlarm> FACTORY =
      new AbstractTsSingleFilterFactory<>( TYPE_ID, ISkAlarm.class ) {

        @Override
        protected ITsFilter<ISkAlarm> doCreateFilter( IOptionSet aParams ) {
          EAvCompareOp sizeOp = EAvCompareOp.findById( aParams.getStr( PID_SIZE_OP ) );
          IAtomicValue sizeConstant = aParams.getValue( PID_SIZE_CONSTANT );
          boolean testAll = aParams.getBool( PID_TEST_ALL );
          EAvCompareOp timeOp = EAvCompareOp.findById( aParams.getStr( PID_TIME_OP ) );
          IAtomicValue timeConstant = aParams.getValue( PID_TIME_CONSTANT );
          EAvCompareOp threadOp = EAvCompareOp.findById( aParams.getStr( PID_THREAD_OP ) );
          IAtomicValue threadConstant = aParams.getValue( PID_THREAD_CONSTANT );
          int paramValuesSize = (aParams.size() - 6) / 3;
          ParamValue[] paramValues = new ParamValue[paramValuesSize];
          for( int index = 0; index < paramValuesSize; index++ ) {
            String id = aParams.getStr( PID_PARAM_ID + index );
            EAvCompareOp op = EAvCompareOp.getById( aParams.getStr( PID_PARAM_OP + index ) );
            IAtomicValue constant = aParams.getValue( PID_PARAM_CONSTANT + index );
            paramValues[index] = new ParamValue( id, op, constant );
          }
          return new SkAlarmFilterByHistory( sizeOp, sizeConstant, testAll, timeOp, timeConstant, threadOp,
              threadConstant, paramValues );
        }
      };

  private static final String PID_SIZE_OP       = "sizeOp";       //$NON-NLS-1$
  private static final String PID_SIZE_CONSTANT = "sizeConstant"; //$NON-NLS-1$

  private static final String PID_TEST_ALL        = "testAll";        //$NON-NLS-1$
  private static final String PID_TIME_OP         = "timeOp";         //$NON-NLS-1$
  private static final String PID_TIME_CONSTANT   = "timeConstant";   //$NON-NLS-1$
  private static final String PID_THREAD_OP       = "threadOp";       //$NON-NLS-1$
  private static final String PID_THREAD_CONSTANT = "threadConstant"; //$NON-NLS-1$

  private static final String PID_PARAM_ID       = "paramId";       //$NON-NLS-1$
  private static final String PID_PARAM_OP       = "paramOp";       //$NON-NLS-1$
  private static final String PID_PARAM_CONSTANT = "paramConstant"; //$NON-NLS-1$

  private final EAvCompareOp sizeOp;
  private final IAtomicValue sizeConstant;
  private final boolean      testAll;
  private final EAvCompareOp timeOp;
  private final IAtomicValue timeConstant;
  private final EAvCompareOp threadOp;
  private final IAtomicValue threadConstant;
  private final ParamValue[] paramValues;

  /**
   * Фильтрация по параметру {@link ISkAlarmThreadHistoryItem#params()}
   */
  public static class ParamValue {

    private final String       id;
    private final EAvCompareOp op;
    private final IAtomicValue constant;

    /**
     * Конструктор
     *
     * @param aId String идентификатор параметра
     * @param aOp {@link EAvCompareOp} операция сравнения
     * @param aConstant {@link IAtomicValue} константа для сравнения
     * @throws TsNullArgumentRtException любой аргумент = null
     */
    public ParamValue( String aId, EAvCompareOp aOp, IAtomicValue aConstant ) {
      TsNullArgumentRtException.checkNulls( aId, aOp, aConstant );
      id = aId;
      op = aOp;
      constant = aConstant;
    }

    /**
     * @return String Идентификатор параметра
     */
    public String id() {
      return id;
    }

    /**
     * @return {@link EAvCompareOp} операция сравнения
     */
    public EAvCompareOp op() {
      return op;
    }

    /**
     * @return {@link IAtomicValue} константа сравнения
     */
    public IAtomicValue constant() {
      return constant;
    }
  }

  /**
   * Конструктор.
   *
   * @param aSizeOp {@link EAvCompareOp} - способ сравнения количества элементов в истории {@link ISkAlarm#history()}.
   * @param aSizeConst {@link IAtomicValue} - константа для сравнения количества элементов в истории имеющая тип
   *          {@link EAtomicType#INTEGER}
   * @param aTestAll boolean <b>true</b> тестировать все элементы истории (логическое И); <b>false</b> тестировать до
   *          первого проходящего фильтр (логическое ИЛИ).
   * @param aTimeOp {@link EAvCompareOp} - способ сравнения времени.
   * @param aTimeConst {@link IAtomicValue} - константа для сравнения времени имеющая тип {@link EAtomicType#TIMESTAMP}
   * @param aThreadOp {@link EAvCompareOp} - способ сравнения идентификатора нитки
   * @param aThreadConst {@link IAtomicValue} - константа для сравнения идентификатора нитки имеющая тип
   *          {@link EAtomicType#STRING}
   * @param aParamValues {@link ParamValue}[] список параметров фильтрации по параметрам
   *          {@link ISkAlarmThreadHistoryItem#params()}.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private SkAlarmFilterByHistory( EAvCompareOp aSizeOp, IAtomicValue aSizeConst, boolean aTestAll, EAvCompareOp aTimeOp,
      IAtomicValue aTimeConst, EAvCompareOp aThreadOp, IAtomicValue aThreadConst, ParamValue[] aParamValues ) {
    TsNullArgumentRtException.checkNulls( aTimeOp, aTimeConst, aThreadOp, aThreadConst, aParamValues );
    TsIllegalArgumentRtException.checkFalse( aSizeConst.atomicType() == EAtomicType.INTEGER );
    TsIllegalArgumentRtException.checkFalse( aTimeConst.atomicType() == EAtomicType.TIMESTAMP );
    TsIllegalArgumentRtException.checkFalse( aThreadConst.atomicType() == EAtomicType.STRING );
    sizeOp = aSizeOp;
    sizeConstant = aSizeConst;
    testAll = aTestAll;
    timeOp = aTimeOp;
    timeConstant = aTimeConst;
    threadOp = aThreadOp;
    threadConstant = aThreadConst;
    paramValues = aParamValues;
  }

  /**
   * Создает набор параметров {@link ITsCombiFilterParams} для создания фильтра фабрикой {@link #FACTORY}.
   *
   * @param aSizeOp {@link EAvCompareOp} - способ сравнения количества элементов в истории {@link ISkAlarm#history()}.
   * @param aSizeConst {@link IAtomicValue} - константа для сравнения количества элементов в истории имеющая тип
   *          {@link EAtomicType#INTEGER}
   * @param aTimeOp {@link EAvCompareOp} - способ сравнения времени
   * @param aTestAll boolean <b>true</b> тестировать все элементы истории (логическое И); <b>false</b> тестировать до
   *          первого проходящего фильтр (логическое ИЛИ).
   * @param aTimeConst {@link IAtomicValue} - константа для сравнения времени имеющая тип {@link EAtomicType#TIMESTAMP}
   * @param aThreadOp {@link EAvCompareOp} - способ сравнения идентификатора нитки
   * @param aThreadConst {@link IAtomicValue} - константа для сравнения идентификатора нитки имеющая тип
   *          {@link EAtomicType#STRING}
   * @param aParamValues {@link ParamValue} параметры и значения в порядке: Имя Параметра
   * @return {@link ITsCombiFilterParams} - параметры для создания фильтра фабрикой
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static ITsCombiFilterParams makeFilterParams( EAvCompareOp aSizeOp, IAtomicValue aSizeConst, boolean aTestAll,
      EAvCompareOp aTimeOp, IAtomicValue aTimeConst, EAvCompareOp aThreadOp, IAtomicValue aThreadConst,
      ParamValue... aParamValues ) {
    TsNullArgumentRtException.checkNulls( aSizeOp, aSizeConst, aTimeOp, aTimeConst, aThreadOp, aThreadConst,
        aParamValues );
    TsIllegalArgumentRtException.checkFalse( aSizeConst.atomicType() == EAtomicType.INTEGER );
    TsIllegalArgumentRtException.checkFalse( aTimeConst.atomicType() == EAtomicType.TIMESTAMP );
    TsIllegalArgumentRtException.checkFalse( aThreadConst.atomicType() == EAtomicType.STRING );
    TsSingleFilterParams sp = TsSingleFilterParams.create( TYPE_ID, //
        PID_SIZE_OP, aSizeOp.id(), //
        PID_SIZE_CONSTANT, aSizeConst, //
        PID_TEST_ALL, avBool( aTestAll ), //
        PID_TIME_OP, aTimeOp.id(), //
        PID_TIME_CONSTANT, aTimeConst, //
        PID_THREAD_OP, aThreadOp.id(), //
        PID_THREAD_CONSTANT, aThreadConst //
    );
    for( int index = 0, n = aParamValues.length; index < n; index++ ) {
      ParamValue paramValue = aParamValues[index];
      sp.params().setStr( PID_PARAM_ID + index, paramValue.id() );
      sp.params().setStr( PID_PARAM_OP + index, paramValue.op.id() );
      sp.params().setValue( PID_PARAM_CONSTANT + index, paramValue.constant() );
    }
    ITsCombiFilterParams p = TsCombiFilterParams.createSingle( sp );
    return p;
  }

  // ------------------------------------------------------------------------------------
  // ITsFilter
  //
  @Override
  public boolean accept( ISkAlarm aAlarm ) {
    IAvComparator c = AvComparatorStrict.INSTANCE;
    ITimedList<ISkAlarmThreadHistoryItem> history = aAlarm.history();
    if( !c.avCompare( avInt( history.size() ), sizeOp, sizeConstant ) ) {
      return false;
    }
    // Количество элементов прошедших фильтр
    int passedCount = 0;
    nextThreadItem:
    for( ISkAlarmThreadHistoryItem threadItem : history ) {
      if( !c.avCompare( avTimestamp( threadItem.timestamp() ), timeOp, timeConstant ) ) {
        continue;
      }
      if( !c.avCompare( avStr( threadItem.announceThreadId() ), threadOp, threadConstant ) ) {
        continue;
      }
      IOptionSet params = threadItem.params();
      for( ParamValue paramValue : paramValues ) {
        String paramId = paramValue.id();
        if( !params.hasKey( paramId ) ) {
          continue nextThreadItem;
        }
        IAtomicValue value = params.findByKey( paramId );
        if( value == null ) {
          continue nextThreadItem;
        }
        if( !c.avCompare( value, paramValue.op(), paramValue.constant() ) ) {
          continue nextThreadItem;
        }
      }
      // Все фильтры пройдены
      if( !testAll ) {
        return true;
      }
      passedCount++;
    }
    return (passedCount == history.size());
  }

}
