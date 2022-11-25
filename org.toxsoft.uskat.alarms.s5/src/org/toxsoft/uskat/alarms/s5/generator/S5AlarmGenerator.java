package org.toxsoft.uskat.alarms.s5.generator;

import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.uskat.alarms.s5.generator.IS5Resources.*;

import java.util.function.Predicate;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.bricks.strid.impl.StridUtils;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.alarms.lib.*;
import org.toxsoft.uskat.alarms.lib.flacon.ISkAlarmFlacon;
import org.toxsoft.uskat.alarms.s5.supports.S5AlarmDefEntity;

/**
 * Генератор алармов
 *
 * @author mvk
 */
class S5AlarmGenerator
    implements IS5AlarmGenerator {

  /**
   * Таймаут после запуска сервера в течении которого генератор не формирует алармы
   */
  private static final long SILENT_AFTER_START = 5 * 60 * 1000;

  private final Skid                                      userId;
  private final ISkAlarmService                           skAlarmService;
  private final IStridablesListEdit<IS5AlarmDataProvider> providers;
  private final boolean                                   providersAutoClose;
  private final IListEdit<S5AlarmProfile>                 profiles  = new ElemArrayList<>();
  private final ILogger                                   logger    = getLogger( getClass() );
  private final long                                      startTime = System.currentTimeMillis();

  /**
   * Конструктор генератора
   *
   * @param aUserId {@link Skid} идентификатор пользователя системы формирующего алармы
   * @param aSkAlarmService {@link ISkAlarmService} служба алармов
   * @param aProviders {@link IStridablesList}&lt;{@link IS5AlarmDataProvider}&gt; поставщики данных для формирования
   *          алармов
   * @param aProvidersAutoClose boolean <b>true</b> завершать работу поставщиков данных при завершении работы
   *          генератора;<b>false</b> не завершать работу поставщиков данных
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5AlarmGenerator( Skid aUserId, ISkAlarmService aSkAlarmService, IStridablesList<IS5AlarmDataProvider> aProviders,
      boolean aProvidersAutoClose ) {
    TsNullArgumentRtException.checkNulls( aSkAlarmService, aProviders );
    userId = aUserId;
    skAlarmService = aSkAlarmService;
    providers = new StridablesList<>( aProviders );
    providersAutoClose = aProvidersAutoClose;
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Поставщики данных для формирования алармов
   *
   * @return {@link IStridablesList}&lt;{@link IS5AlarmDataProvider}&gt; поставщики данных
   */
  public IStridablesList<IS5AlarmDataProvider> providers() {
    return providers;
  }

  // ------------------------------------------------------------------------------------
  // IS5AlarmGenerator
  //
  @Override
  public void addAlarm( Skid aAuthorId, ISkAlarmDef aSkAlarmDef, Predicate<IS5AlarmProfile> aPredicate ) {
    TsNullArgumentRtException.checkNulls( aSkAlarmDef, aPredicate );
    // Идентикатор аларма
    String alarmId = aSkAlarmDef.id();
    if( findProfile( aAuthorId, alarmId ) != null ) {
      // Аларм для автора уже зарегистрирован в генераторе
      throw new TsIllegalArgumentRtException( ERR_ALARM_DEF_ALREADY_EXIST, alarmId, aAuthorId );
    }
    // Поиск описания аларма в системе
    ISkAlarmDef skAlarmDef = skAlarmService.listAlarmDefs().findByKey( alarmId );
    if( skAlarmDef == null ) {
      // Регистрация аларма в системе
      skAlarmService.registerAlarmDef( aSkAlarmDef );
      // Журнал
      logger.info( MSG_ADD_ALARM_DEF, alarmId );
    }
    profiles.add( new S5AlarmProfile( this, aSkAlarmDef, aAuthorId, aPredicate ) );
  }

  @Override
  public void addAlarm( String aAlarmId, EAlarmPriority aAlarmPriority, String aMessage, Skid aObjId, String aDataId,
      IS5AlarmAtomicValuePredicate aValuePredicate ) {
    TsNullArgumentRtException.checkNulls( aValuePredicate, aAlarmId, aAlarmPriority, aMessage, aObjId );
    S5AlarmDefEntity alarmDef = new S5AlarmDefEntity( aAlarmId, aMessage );
    alarmDef.setPriority( aAlarmPriority );
    // Условие текущего данного: значение текущего данного должно быть присвоено
    S5AlarmCurrDataPredicate currDataPredicate1 =
        new S5AlarmCurrDataPredicate( aObjId, aDataId, IAtomicValue::isAssigned );
    // Условие текущего данного: условие на значение
    S5AlarmCurrDataPredicate currDataPredicate2 = new S5AlarmCurrDataPredicate( aObjId, aDataId, aValuePredicate );
    // AND условий
    Predicate<IS5AlarmProfile> currDataPredicate = currDataPredicate1.and( currDataPredicate2 );
    // Добавление аларма
    addAlarm( aObjId, alarmDef, currDataPredicate );
  }

  // ------------------------------------------------------------------------------------
  // Методы пакета
  //
  /**
   * Генерирует {@link ISkAlarm}
   *
   * @param aAlarmDefId String идентификтор аларма
   * @param aAuthorId {@link Skid} автор аларма
   * @param aSublevel byte уточнение аларма
   * @param aSkAlarmFlacon {@link ISkAlarmFlacon} флакон
   */
  void generateAlarm( String aAlarmDefId, Skid aAuthorId, byte aSublevel, ISkAlarmFlacon aSkAlarmFlacon ) {
    TsNullArgumentRtException.checkNulls( aAuthorId, aAlarmDefId, aSkAlarmFlacon );
    long currTime = System.currentTimeMillis();
    if( currTime - startTime < SILENT_AFTER_START ) {
      // Запрет формирования аларма после перезапуска
      logger().warning( ERR_SILENT_MODE, aAlarmDefId, Long.valueOf( SILENT_AFTER_START - (currTime - startTime) ) );
      return;
    }
    skAlarmService.generateAlarm( aAlarmDefId, aAuthorId, userId, aSublevel, aSkAlarmFlacon );
  }

  /**
   * Возвращает журнал генератора
   *
   * @return {@link ILogger} журнал
   */
  ILogger logger() {
    return logger;
  }

  // ------------------------------------------------------------------------------------
  // Реализация ICooperativeMultiTaskable
  //
  @Override
  public void doJob() {
    for( S5AlarmProfile alarmProfile : profiles ) {
      alarmProfile.doJob();
    }
    // TODO обработка алармов связанных с интервалами времени или с частотными отклонениями
  }

  // ------------------------------------------------------------------------------------
  // Реализация ICloseable
  //
  @Override
  public void close() {
    for( S5AlarmProfile alarmProfile : profiles ) {
      alarmProfile.close();
    }
    if( providersAutoClose ) {
      for( IS5AlarmDataProvider provider : providers ) {
        provider.close();
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Проводит поиск профиля для указанного аларма
   *
   * @param aAuthorId {@link Skid} идентификтор объекта автора аларма
   * @param aAlarmId String идентификатор аларма
   * @return {@link S5AlarmProfile} профиль аларма. null: аларм не найден
   * @throws TsIllegalArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException идентификатор аларма не ИД-путь
   */
  private S5AlarmProfile findProfile( Skid aAuthorId, String aAlarmId ) {
    StridUtils.checkValidIdPath( aAlarmId );
    for( S5AlarmProfile profile : profiles ) {
      if( profile.alarmAuthorId().equals( aAuthorId ) && profile.skAlarmDef().id().equals( aAlarmId ) ) {
        return profile;
      }
    }
    return null;
  }
}
