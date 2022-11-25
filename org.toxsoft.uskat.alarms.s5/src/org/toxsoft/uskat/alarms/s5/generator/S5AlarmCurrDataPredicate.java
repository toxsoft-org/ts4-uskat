package org.toxsoft.uskat.alarms.s5.generator;

import java.util.function.Predicate;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.alarms.lib.ISkAlarm;

/**
 * Условие текущего данного для формирования {@link ISkAlarm}
 *
 * @author mvk
 */
public class S5AlarmCurrDataPredicate
    implements Predicate<IS5AlarmProfile> {

  private final Skid                         objId;
  private final String                       dataId;
  private final IS5AlarmAtomicValuePredicate predicate;

  /**
   * Контруктор
   *
   * @param aObjId {@link Skid} идентификатор объекта
   * @param aDataId String строковый идентификатор данного
   * @param aValuePredicate {@link IS5AlarmAtomicValuePredicate} условие на значение текущего данного
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5AlarmCurrDataPredicate( Skid aObjId, String aDataId, IS5AlarmAtomicValuePredicate aValuePredicate ) {
    TsNullArgumentRtException.checkNulls( aDataId, aValuePredicate );
    objId = aObjId;
    dataId = aDataId;
    predicate = aValuePredicate;
  }

  // ------------------------------------------------------------------------------------
  // Реализация Predicate
  //
  @Override
  public boolean test( IS5AlarmProfile aSkAlarmProfile ) {
    TsNullArgumentRtException.checkNull( aSkAlarmProfile );
    S5AlarmCurrDataProvider provider = (S5AlarmCurrDataProvider)aSkAlarmProfile.providers()
        .getByKey( S5AlarmCurrDataProvider.ALARM_CURRDATA_PROVIDER );
    IAtomicValue value = getProviderValue( provider );
    return predicate.test( value );
  }

  // ------------------------------------------------------------------------------------
  // Внутрение методы
  //
  /**
   * Запрос текущего значения данного у провайдера
   *
   * @param aProvider {@link S5AlarmCurrDataProvider} поставщик данных
   * @return {@link IAtomicValue} текущее значение
   * @throws TsNullArgumentRtException аргумент = null
   */
  private IAtomicValue getProviderValue( S5AlarmCurrDataProvider aProvider ) {
    TsNullArgumentRtException.checkNull( aProvider );
    return aProvider.getCurrDataValue( objId, dataId );
  }
}
