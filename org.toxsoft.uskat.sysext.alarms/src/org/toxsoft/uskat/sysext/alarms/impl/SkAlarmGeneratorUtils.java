package org.toxsoft.uskat.sysext.alarms.impl;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.sysext.alarms.api.ISkAlarmGenerator;
import org.toxsoft.uskat.sysext.alarms.api.ISkAlarmService;
import org.toxsoft.uskat.sysext.alarms.api.generator.ISkAlarmDataProvider;
import org.toxsoft.uskat.sysext.alarms.api.generator.currdata.SkAlarmCurrDataProvider;

import ru.uskat.core.api.objserv.ISkObjectService;
import ru.uskat.core.api.rtdata.ISkRtDataService;
import ru.uskat.core.api.users.ISkUser;
import ru.uskat.core.api.users.ISkUserService;

/**
 * Точка входа в пакет
 *
 * @author mvk
 */
public class SkAlarmGeneratorUtils {

  /**
   * Создать генератор алармов из текущих данных
   *
   * @param aSkAlarmService {@link ISkAlarmService} служба алармов
   * @param aObjectService {@link ISkObjectService} служба управления объектами
   * @param aUserService {@link ISkUserService} служба управления пользователями
   * @param aRtDataService {@link ISkRtDataService} служба управления данными реального времени
   * @return {@link ISkAlarmGenerator} генератор алармов
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static ISkAlarmGenerator createGenerator( ISkAlarmService aSkAlarmService, ISkObjectService aObjectService,
      ISkUserService aUserService, ISkRtDataService aRtDataService ) {
    TsNullArgumentRtException.checkNulls( aSkAlarmService, aObjectService, aUserService, aRtDataService );
    // Пользователь: служба алармов
    ISkUser user = TsNullArgumentRtException.checkNull( aUserService.find( SkAlarmService.ALARM_USER_LOGIN ) );
    // Поставщик текущих данных для алармов
    ISkAlarmDataProvider provider = new SkAlarmCurrDataProvider( aRtDataService );
    // Создание генераторов алармов с авто завершением работы поставщика и автоматической регистрацией алармов
    return createGenerator( user.skid(), aSkAlarmService, new StridablesList<>( provider ), true );
  }

  /**
   * Создать генератор алармов с несколькими поставщиками данных для алармов
   *
   * @param aUserId {@link Skid} идентификатор пользователя системы формирующего алармы
   * @param aSkAlarmService {@link ISkAlarmService} служба алармов
   * @param aProviders {@link IStridablesList}&lt;{@link ISkAlarmDataProvider}&gt; поставщики данных для формирования
   *          алармов
   * @param aProvidersAutoClose boolean <b>true</b> завершать работу поставщиков данных при завершении работы
   *          генератора;<b>false</b> не завершать работу поставщиков данных
   * @return {@link ISkAlarmGenerator} генератор алармов
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static ISkAlarmGenerator createGenerator( Skid aUserId, ISkAlarmService aSkAlarmService,
      IStridablesList<ISkAlarmDataProvider> aProviders, boolean aProvidersAutoClose ) {
    return new SkAlarmGenerator( aUserId, aSkAlarmService, aProviders, aProvidersAutoClose );
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
