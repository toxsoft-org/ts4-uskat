package org.toxsoft.uskat.alarms.s5.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.bricks.filter.ITsCombiFilterParams;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.alarms.lib.*;
import org.toxsoft.uskat.alarms.lib.flacon.ISkAlarmFlacon;
import org.toxsoft.uskat.concurrent.*;

/**
 * Синхронизация доступа к {@link ISkAlarmService} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedAlarmService
    extends S5SynchronizedService<ISkAlarmService>
    implements ISkAlarmService {

  private final S5SynchronizedAlarmEventer eventer;

  /**
   * Конструктор
   *
   * @param aCoreApi {@link S5SynchronizedConnection} защищенное API сервера
   * @throws TsNullArgumentRtException аругмент = null
   * @throws TsItemNotFoundRtException в соединении не найдена служба которую необходимо защитить
   */
  public S5SynchronizedAlarmService( S5SynchronizedCoreApi aCoreApi ) {
    super( (ISkAlarmService)aCoreApi.target().services().getByKey( ISkAlarmService.SERVICE_ID ),
        aCoreApi.nativeLock() );
    eventer = new S5SynchronizedAlarmEventer( target().eventer(), nativeLock() );
    aCoreApi.services().put( ISkAlarmService.SERVICE_ID, this );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ISkAlarmService aPrevTarget, ISkAlarmService aNewTarget,
      ReentrantReadWriteLock aNewLock ) {
    eventer.changeTarget( aNewTarget.eventer(), aNewLock );
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
    lockWrite( this );
    try {
      return target().generateAlarm( aAlarmDefId, aAuthorId, aUserId, aSublevel, aSkAlarmFlacon );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void addThreadHistoryItem( long aAlarmId, ISkAlarmThreadHistoryItem aItem ) {
    lockWrite( this );
    try {
      target().addThreadHistoryItem( aAlarmId, aItem );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ITimedList<ISkAlarm> queryAlarms( ITimeInterval aTimeInterval, ITsCombiFilterParams aFilter ) {
    lockWrite( this );
    try {
      return target().queryAlarms( aTimeInterval, aFilter );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkAlarmEventer eventer() {
    return eventer;
  }

}
