package org.toxsoft.uskat.sysext.alarms.impl;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.bricks.filter.ITsCombiFilterParams;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.concurrent.S5SynchronizedConnection;
import org.toxsoft.uskat.concurrent.S5SynchronizedService;
import org.toxsoft.uskat.sysext.alarms.api.*;
import org.toxsoft.uskat.sysext.alarms.api.flacon.ISkAlarmFlacon;

/**
 * Синхронизация доступа к {@link ISkAlarmService} (декоратор)
 *
 * @author mvk
 */
public final class SkAlarmSynchronizedAlarmsService
    extends S5SynchronizedService<ISkAlarmService>
    implements ISkAlarmService {

  private final S5SynchronizedAlarmEventsFiringSupport eventer;

  /**
   * Конструктор
   *
   * @param aConnection {@link S5SynchronizedConnection} защищенное соединение
   * @throws TsNullArgumentRtException аругмент = null
   * @throws TsItemNotFoundRtException в соединении не найдена служба которую необходимо защитить
   */
  public SkAlarmSynchronizedAlarmsService( S5SynchronizedConnection aConnection ) {
    this( (ISkAlarmService)aConnection.getUnsynchronizedService( SERVICE_ID ), aConnection.mainLock() );
    aConnection.addService( this );
  }

  /**
   * Конструктор
   *
   * @param aTarget {@link ISkAlarmService} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public SkAlarmSynchronizedAlarmsService( ISkAlarmService aTarget, ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
    eventer = new S5SynchronizedAlarmEventsFiringSupport( target().eventer(), lock() );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ISkAlarmService aPrevTarget, ISkAlarmService aNewTarget,
      ReentrantReadWriteLock aNewLock ) {
    eventer.doChangeTarget( aPrevTarget.eventer(), aNewTarget.eventer(), aNewLock );
  }

  // ------------------------------------------------------------------------------------
  // Реализация ISkAlarmService
  //
  @Override
  public IStridablesList<ISkAlarmDef> listAlarmDefs() {
    lockWrite( this );
    try {
      return target().listAlarmDefs();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkAlarmDef findAlarmDef( String aAlarmDefId ) {
    TsNullArgumentRtException.checkNull( aAlarmDefId );
    lockWrite( this );
    try {
      return target().findAlarmDef( aAlarmDefId );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void registerAlarmDef( ISkAlarmDef aAlarmDef ) {
    TsNullArgumentRtException.checkNull( aAlarmDef );
    lockWrite( this );
    try {
      target().registerAlarmDef( aAlarmDef );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkAlarm generateAlarm( String aAlarmDefId, Skid aAuthorId, Skid aUserId, byte aSublevel,
      ISkAlarmFlacon aSkAlarmFlacon ) {
    TsNullArgumentRtException.checkNulls( aAlarmDefId, aAuthorId, aUserId, aSkAlarmFlacon );
    lockWrite( this );
    try {
      return target().generateAlarm( aAlarmDefId, aAuthorId, aUserId, aSublevel, aSkAlarmFlacon );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void addAnnounceThreadHistoryItem( long aAlarmId, ISkAnnounceThreadHistoryItem aItem ) {
    TsNullArgumentRtException.checkNull( aItem );
    lockWrite( this );
    try {
      target().addAnnounceThreadHistoryItem( aAlarmId, aItem );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkAlarmFlacon getAlarmFlacon( long aAlarmId ) {
    lockWrite( this );
    try {
      return target().getAlarmFlacon( aAlarmId );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ITimedList<ISkAnnounceThreadHistoryItem> getAlarmHistory( long aAlarmId ) {
    lockWrite( this );
    try {
      return target().getAlarmHistory( aAlarmId );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ITimedList<ISkAlarm> queryAlarms( ITimeInterval aTimeInterval, ITsCombiFilterParams aQueryParams ) {
    TsNullArgumentRtException.checkNulls( aTimeInterval, aQueryParams );
    lockWrite( this );
    try {
      return target().queryAlarms( aTimeInterval, aQueryParams );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkAlarmEventsFiringSupport eventer() {
    return eventer;
  }

}
