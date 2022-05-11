package org.toxsoft.uskat.sysext.alarms.impl;

import static org.toxsoft.uskat.sysext.alarms.impl.ISkResources.*;

import org.toxsoft.uskat.sysext.alarms.api.ISkAlarm;
import org.toxsoft.uskat.sysext.alarms.api.filters.ISkAlarmFilterByDefId;

import ru.toxsoft.tslib.error.TsIllegalArgumentRtException;
import ru.toxsoft.tslib.error.TsNullArgumentRtException;
import ru.toxsoft.tslib.polyfilter.*;
import ru.toxsoft.tslib.polyfilter.impl.SingleFilterParams;
import ru.toxsoft.tslib.polyfilter.stdfilters.strid.*;

/**
 * Реализация {@link ISkAlarmFilterByDefId}.
 *
 * @author goga
 */
public class SkAlarmFilterByDefId
    extends StdStridFilter
    implements ISkAlarmFilterByDefId {

  private static final long serialVersionUID = 157157L;

  /**
   * Экземпляр-синглтон фабрики фильтра.
   */
  @SuppressWarnings( "hiding" )
  public static final ISingleFilterFactory FACTORY =
      new Factory( ISkAlarmFilterByDefId.FILTER_ID, STR_N_FILTER_BY_ALARM_DEF_ID, STR_D_FILTER_BY_ALARM_DEF_ID ) {

        private static final long serialVersionUID = 157157L;

        @Override
        protected ISingleFilter doCreateFilter( ISingleFilterParams aFilterInfo ) {
          return new SkAlarmFilterByDefId( aFilterInfo );
        }

      };

  /**
   * Конструктор из параметров единичного фильтра {@link ISingleFilterParams}.
   *
   * @param aParams {@link ISingleFilterParams} - параметры фильтра
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException идентифкатор фильтра аргумента не равен {@link IStdStridFilter#FILTER_ID}
   */
  public SkAlarmFilterByDefId( ISingleFilterParams aParams ) {
    super( ISkAlarmFilterByDefId.FILTER_ID, aParams );
  }

  /**
   * Конструктор копирования.
   *
   * @param aSource {@link ISkAlarmFilterByDefId} - источник
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException идентифкатор фильтра аргумента не равен {@link IStdStridFilter#FILTER_ID}
   */
  public SkAlarmFilterByDefId( ISkAlarmFilterByDefId aSource ) {
    this( TsNullArgumentRtException.checkNull( aSource ).params() );
    params().params().replaceOpSet( aSource.params().params() );
  }

  /**
   * Создает фильтр заданного вида с пустыми параметрами.
   * <p>
   * Перед использованием фидьтра следует задать параметры, необходимые для указанного вида фильтра.
   *
   * @param aFilterKind {@link EStridFilterKind} - вид фильтра
   * @throws TsNullArgumentRtException аргумент = null
   */
  public SkAlarmFilterByDefId( EStridFilterKind aFilterKind ) {
    this( prepareSfp( aFilterKind ) );

  }

  private static ISingleFilterParams prepareSfp( EStridFilterKind aKind ) {
    SingleFilterParams sfp = new SingleFilterParams( ISkAlarmFilterByDefId.FILTER_ID );
    FILTER_KIND.setValue( sfp.params(), aKind );
    return sfp;
  }

  @Override
  public boolean accept( Object aElement ) {
    if( aElement == null ) {
      throw new TsNullArgumentRtException();
    }
    if( aElement instanceof ISkAlarm ) {
      ISkAlarm skAlarm = (ISkAlarm)aElement;
      return super.accept( skAlarm.alarmDefId() );
    }
    throw new TsIllegalArgumentRtException( ERR_NON_ALARM_INPUT_OBJ, ISkAlarmFilterByDefId.FILTER_ID );
  }

}
