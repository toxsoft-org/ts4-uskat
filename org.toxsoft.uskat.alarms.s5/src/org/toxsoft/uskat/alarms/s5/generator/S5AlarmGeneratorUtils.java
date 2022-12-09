package org.toxsoft.uskat.alarms.s5.generator;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.alarms.lib.ISkAlarmService;
import org.toxsoft.uskat.core.api.rtdserv.ISkRtdataService;

/**
 * Точка входа в пакет
 *
 * @author mvk
 */
public class S5AlarmGeneratorUtils {

  /**
   * Создать генератор алармов из текущих данных
   *
   * @param aUserId {@link Skid} идентификатор пользователя системы формирующего алармы
   * @param aSkAlarmService {@link ISkAlarmService} служба алармов
   * @param aRtdataService {@link ISkRtdataService} служба управления данными реального времени
   * @param aProvidersAutoClose boolean <b>true</b> завершать работу поставщиков данных при завершении работы
   *          генератора;<b>false</b> не завершать работу поставщиков данных
   * @return {@link IS5AlarmGenerator} генератор алармов
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static IS5AlarmGenerator createGenerator( Skid aUserId, ISkAlarmService aSkAlarmService,
      ISkRtdataService aRtdataService, boolean aProvidersAutoClose ) {
    TsNullArgumentRtException.checkNulls( aSkAlarmService, aRtdataService );
    // Поставщик текущих данных для алармов
    IS5AlarmDataProvider provider = new S5AlarmCurrDataProvider( aRtdataService );
    // Создание генераторов алармов с авто завершением работы поставщика и автоматической регистрацией алармов
    return createGenerator( aUserId, aSkAlarmService, new StridablesList<>( provider ), aProvidersAutoClose );
  }

  /**
   * Создать генератор алармов с несколькими поставщиками данных для алармов
   *
   * @param aUserId {@link Skid} идентификатор пользователя системы формирующего алармы
   * @param aSkAlarmService {@link ISkAlarmService} служба алармов
   * @param aProviders {@link IStridablesList}&lt;{@link IS5AlarmDataProvider}&gt; поставщики данных для формирования
   *          алармов
   * @param aProvidersAutoClose boolean <b>true</b> завершать работу поставщиков данных при завершении работы
   *          генератора;<b>false</b> не завершать работу поставщиков данных
   * @return {@link IS5AlarmGenerator} генератор алармов
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static IS5AlarmGenerator createGenerator( Skid aUserId, ISkAlarmService aSkAlarmService,
      IStridablesList<IS5AlarmDataProvider> aProviders, boolean aProvidersAutoClose ) {
    return new S5AlarmGenerator( aUserId, aSkAlarmService, aProviders, aProvidersAutoClose );
  }

  // ------------------------------------------------------------------------------------
  // Вспомогательные методы для формирования условий возникновений алармов
  //
  /**
   * Проверяет установлен ли бит в целом числе
   *
   * @param aValue {@link IAtomicValue} атомарное значение целого типа
   * @param aBitNo int номер бита
   * @return <b>true</b> бит установлен; <b>false</b> бит не установлен
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static boolean hasBit( IAtomicValue aValue, int aBitNo ) {
    long mask = (long)Math.pow( 2, aBitNo );
    return ((aValue.asLong() & mask) != 0);
  }

  /**
   * Проверяет эквивалетность указанных параметров
   *
   * @param aValue1 {@link IAtomicValue} атомарное значение 1
   * @param aValue2 {@link IAtomicValue} атомарное значение 2
   * @return <b>true</b> значения эквивалентны; <b>false</b> значения не эквивалентны
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static boolean equals( IAtomicValue aValue1, IAtomicValue aValue2 ) {
    TsNullArgumentRtException.checkNulls( aValue1, aValue2 );
    return (aValue1.equals( aValue2 ));
  }

  /**
   * Проверяет НЕэквивалетность указанных параметров
   *
   * @param aValue1 {@link IAtomicValue} атомарное значение 1
   * @param aValue2 {@link IAtomicValue} атомарное значение 2
   * @return <b>true</b> бит установлен; <b>false</b> бит не установлен
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static boolean notEquals( IAtomicValue aValue1, IAtomicValue aValue2 ) {
    TsNullArgumentRtException.checkNulls( aValue1, aValue2 );
    return !aValue1.equals( aValue2 );
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //

}
