package org.toxsoft.uskat.sysext.alarms.impl;

import static org.toxsoft.uskat.sysext.alarms.impl.ISkResources.*;

import org.toxsoft.uskat.sysext.alarms.api.ISkAlarm;
import org.toxsoft.uskat.sysext.alarms.api.filters.ISkAlarmFilterByLevel;

import ru.toxsoft.tslib.datavalue.math.EAvCompareOp;
import ru.toxsoft.tslib.error.TsIllegalArgumentRtException;
import ru.toxsoft.tslib.error.TsNullArgumentRtException;
import ru.toxsoft.tslib.polyfilter.*;
import ru.toxsoft.tslib.polyfilter.impl.AbstractSingleFilter;
import ru.toxsoft.tslib.polyfilter.impl.AbstractSingleFilterFactory;
import ru.toxsoft.tslib.polyfilter.stdfilters.strid.IStdStridFilter;

/**
 * Реализация {@link ISkAlarmFilterByLevel}.
 *
 * @author goga
 */
public class SkAlarmFilterByLevel
    extends AbstractSingleFilter
    implements ISkAlarmFilterByLevel {

  private static final long serialVersionUID = 157157L;

  /**
   * Экземпляр-синглтон фабрики фильтра.
   */
  public static final ISingleFilterFactory FACTORY = new AbstractSingleFilterFactory( FILTER_ID,
      STR_N_FILTER_BY_ALARM_DEF_ID, STR_D_FILTER_BY_ALARM_DEF_ID, COMPARE_OP, LEVEL_CONST ) {

    private static final long serialVersionUID = 157157L;

    @Override
    protected ISingleFilter doCreateFilter( ISingleFilterParams aFilterInfo ) {
      return new SkAlarmFilterByLevel( aFilterInfo );
    }

  };

  /**
   * Конструктор из параметров единичного фильтра {@link ISingleFilterParams}.
   *
   * @param aParams {@link ISingleFilterParams} - параметры фильтра
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException идентифкатор фильтра аргумента не равен {@link IStdStridFilter#FILTER_ID}
   */
  public SkAlarmFilterByLevel( ISingleFilterParams aParams ) {
    super( aParams );
    TsIllegalArgumentRtException.checkFalse( aParams.typeId().equals( FILTER_ID ) );
  }

  /**
   * Конструктор копирования.
   *
   * @param aSource {@link ISkAlarmFilterByLevel} - источник
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException идентифкатор фильтра аргумента не равен {@link IStdStridFilter#FILTER_ID}
   */
  public SkAlarmFilterByLevel( ISkAlarmFilterByLevel aSource ) {
    this( TsNullArgumentRtException.checkNull( aSource ).params() );
    params().params().replaceOpSet( aSource.params().params() );
  }

  @Override
  public boolean accept( Object aElement ) {
    if( aElement == null ) {
      throw new TsNullArgumentRtException();
    }
    if( aElement instanceof ISkAlarm ) {
      ISkAlarm skAlarm = (ISkAlarm)aElement;
      return compareOp().compareInt( skAlarm.level(), levelConst() );
    }
    throw new TsIllegalArgumentRtException( ERR_NON_ALARM_INPUT_OBJ, FILTER_ID );
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса ISkAlarmFilterByLevel
  //

  @Override
  public EAvCompareOp compareOp() {
    return COMPARE_OP.getValue( params().params() );
  }

  @Override
  public int levelConst() {
    return LEVEL_CONST.getValue( params().params() ).asInt();
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов Object
  //

  @SuppressWarnings( "nls" )
  @Override
  public String toString() {
    return getClass().getSimpleName() + ": x " + compareOp().nmName() + " " + levelConst();
  }

}
