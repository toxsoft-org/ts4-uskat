package org.toxsoft.uskat.sysext.alarms.api.generator.currdata;

import static org.toxsoft.uskat.sysext.alarms.api.generator.currdata.SkAlarmCurrDataProvider.*;

import java.util.function.Predicate;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.sysext.alarms.api.ISkAlarm;
import org.toxsoft.uskat.sysext.alarms.api.generator.ISkAlarmAtomicValuePredicate;
import org.toxsoft.uskat.sysext.alarms.api.generator.ISkAlarmProfile;

/**
 * Условие текущего данного для формирования {@link ISkAlarm}
 *
 * @author mvk
 */
public class SkAlarmCurrDataPredicate
    implements Predicate<ISkAlarmProfile> {

  private final Skid                         objId;
  private final String                       dataId;
  private final ISkAlarmAtomicValuePredicate predicate;

  /**
   * Контруктор
   *
   * @param aObjId {@link Skid} идентификатор объекта
   * @param aDataId String строковый идентификатор данного
   * @param aValuePredicate {@link ISkAlarmAtomicValuePredicate} условие на значение текущего данного
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public SkAlarmCurrDataPredicate( Skid aObjId, String aDataId, ISkAlarmAtomicValuePredicate aValuePredicate ) {
    TsNullArgumentRtException.checkNulls( aDataId, aValuePredicate );
    objId = aObjId;
    dataId = aDataId;
    predicate = aValuePredicate;
  }

  // ------------------------------------------------------------------------------------
  // Реализация Predicate
  //
  @Override
  public boolean test( ISkAlarmProfile aSkAlarmProfile ) {
    TsNullArgumentRtException.checkNull( aSkAlarmProfile );
    SkAlarmCurrDataProvider provider =
        (SkAlarmCurrDataProvider)aSkAlarmProfile.providers().getByKey( ALARM_CURRDATA_PROVIDER );
    IAtomicValue value = getProviderValue( provider );
    return predicate.test( value );
  }

  // ------------------------------------------------------------------------------------
  // Внутрение методы
  //
  /**
   * Запрос текущего значения данного у провайдера
   *
   * @param aProvider {@link SkAlarmCurrDataProvider} поставщик данных
   * @return {@link IAtomicValue} текущее значение
   * @throws TsNullArgumentRtException аргумент = null
   */
  private IAtomicValue getProviderValue( SkAlarmCurrDataProvider aProvider ) {
    TsNullArgumentRtException.checkNull( aProvider );
    return aProvider.getCurrDataValue( objId, dataId );
  }
}
