package org.toxsoft.uskat.sysext.alarms.impl;

import static org.toxsoft.uskat.sysext.alarms.impl.ISkResources.*;

import java.util.function.Predicate;

import org.toxsoft.core.tslib.bricks.ICooperativeMultiTaskable;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.sysext.alarms.api.ISkAlarm;
import org.toxsoft.uskat.sysext.alarms.api.ISkAlarmDef;
import org.toxsoft.uskat.sysext.alarms.api.flacon.ISkAlarmFlacon;
import org.toxsoft.uskat.sysext.alarms.api.generator.ISkAlarmDataProvider;
import org.toxsoft.uskat.sysext.alarms.api.generator.ISkAlarmDataProvider.IAlarmDataProviderListener;
import org.toxsoft.uskat.sysext.alarms.api.generator.ISkAlarmProfile;

/**
 * Профайл для формирования {@link ISkAlarm}
 * <p>
 * TODO: реализовать логику, включение/выключение аларма по таймату заданному через конструктор???
 * <p>
 * TODO обработка алармов связанных с интервалами времени или с частотными отклонениями
 *
 * @author mvk
 */
class SkAlarmProfile
    implements ISkAlarmProfile, IAlarmDataProviderListener, ICooperativeMultiTaskable, ICloseable {

  private final SkAlarmGenerator           generator;
  private final ISkAlarmDef                skAlarmDef;
  private final Skid                       authorId;
  private final Predicate<ISkAlarmProfile> predicate;
  private boolean                          alarmed;

  /**
   * Конструктор
   *
   * @param aGenerator {@link SkAlarmGenerator} генератор алармов
   * @param aSkAlarmDef {@link ISkAlarmDef} описание аларма
   * @param aAuthorId {@link Skid} идентификтор объекта автора аларма
   * @param aPredicate {@link Predicate}&lt;{@link SkAlarmProfile}&gt; условие формирования аларма
   */
  SkAlarmProfile( SkAlarmGenerator aGenerator, ISkAlarmDef aSkAlarmDef, Skid aAuthorId,
      Predicate<ISkAlarmProfile> aPredicate ) {
    TsNullArgumentRtException.checkNulls( aGenerator, aSkAlarmDef, aPredicate );
    generator = aGenerator;
    skAlarmDef = aSkAlarmDef;
    authorId = aAuthorId;
    predicate = aPredicate;
    for( ISkAlarmDataProvider provider : generator.providers() ) {
      provider.addProviderListener( this );
    }
    // Сразу протестируем условие чтобы инициализировать состояние поставщиков. Одновременно с этим мы снимаем проблему
    // их поздней инициализации когда она может быть провалена (нет данных в системе)
    testAlarm();
  }

  // ------------------------------------------------------------------------------------
  // Реализация ISkAlarmProfile
  //
  @Override
  public Skid alarmAuthorId() {
    return authorId;
  }

  @Override
  public ISkAlarmDef skAlarmDef() {
    return skAlarmDef;
  }

  @Override
  public IStridablesList<ISkAlarmDataProvider> providers() {
    return generator.providers();
  }

  @Override
  public boolean alarmed() {
    return alarmed;
  }

  // ------------------------------------------------------------------------------------
  // Реализация IAlarmDataProviderListener
  //
  @Override
  public void onUpdate( ISkAlarmDataProvider aProvider ) {
    testAlarm();
  }

  // ------------------------------------------------------------------------------------
  // Реализация ICooperativeMultiTaskable
  //
  @Override
  public void doJob() {
    // TODO обработка алармов связанных с интервалами времени или с частотными отклонениями
  }

  // ------------------------------------------------------------------------------------
  // Реализация ICloseable
  //
  @Override
  public void close() {
    for( ISkAlarmDataProvider provider : generator.providers() ) {
      provider.removeProviderListener( this );
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Проводит тестирование условия и если необходимо формирует {@link ISkAlarm}
   */
  private void testAlarm() {
    boolean prevAlarmed = alarmed;
    boolean currAlarmed = predicate.test( this );
    if( !alarmed && currAlarmed ) {
      String alarmDefId = skAlarmDef.id();
      byte sublevel = 0; // ??? вынести в конструктор SkAlarmProfile???
      ISkAlarmFlacon skAlarmFlacon = ISkAlarmFlacon.NULL; // ???
      generator.generateAlarm( alarmDefId, authorId, sublevel, skAlarmFlacon );
    }
    alarmed = currAlarmed;
    // Журнал
    if( alarmed && !prevAlarmed ) {
      // Включение аларма
      generator.logger().info( MSG_ALARM_ON, skAlarmDef, authorId );
    }
    if( !alarmed && prevAlarmed ) {
      // Выключение аларма
      generator.logger().info( MSG_ALARM_OFF, skAlarmDef, authorId );
    }
  }
}
