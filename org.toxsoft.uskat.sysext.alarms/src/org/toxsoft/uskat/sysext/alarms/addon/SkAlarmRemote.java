package org.toxsoft.uskat.sysext.alarms.addon;

import org.toxsoft.core.tslib.bricks.filter.ITsCombiFilterParams;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.client.remote.S5BackendAddonRemote;
import org.toxsoft.uskat.sysext.alarms.api.*;
import org.toxsoft.uskat.sysext.alarms.api.flacon.ISkAlarmFlacon;

/**
 * Реализация удаленного доступа к расширению бекенда {@link ISkBackendAddonAlarm}
 *
 * @author mvk
 */
public final class SkAlarmRemote
    extends S5BackendAddonRemote<ISkAlarmRemote>
    implements ISkAlarmRemote {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор
   */
  public SkAlarmRemote() {
    super( ISkBackendAddonAlarm.S5_BACKEND_ALARMS_ID, ISkBackendAddonAlarm.S5_BACKEND_ALARMS_ID, ISkAlarmRemote.class );
  }

  // ------------------------------------------------------------------------------------
  // Реализация ISkAlarmRemote
  //
  @Override
  public IStridablesList<ISkAlarmDef> listAlarmDefs() {
    return remote().listAlarmDefs();
  }

  @Override
  public ISkAlarmDef findAlarmDef( String aAlarmDefId ) {
    TsNullArgumentRtException.checkNull( aAlarmDefId );
    return remote().findAlarmDef( aAlarmDefId );
  }

  @Override
  public void registerAlarmDef( ISkAlarmDef aSkAlarmDef ) {
    TsNullArgumentRtException.checkNull( aSkAlarmDef );
    remote().registerAlarmDef( aSkAlarmDef );
  }

  @Override
  public ISkAlarm generateAlarm( String aAlarmDefId, Skid aAuthorId, Skid aUserId, byte aSublevel,
      ISkAlarmFlacon aSkAlarmFlacon ) {
    TsNullArgumentRtException.checkNulls( aAlarmDefId, aAuthorId, aUserId, aSkAlarmFlacon );
    return remote().generateAlarm( aAlarmDefId, aAuthorId, aUserId, aSublevel, aSkAlarmFlacon );
  }

  @Override
  public void addAnnounceThreadHistoryItem( long aAlarmId, ISkAnnounceThreadHistoryItem aItem ) {
    TsNullArgumentRtException.checkNull( aItem );
    remote().addAnnounceThreadHistoryItem( aAlarmId, aItem );
  }

  @Override
  public ISkAlarmFlacon getAlarmFlacon( long aAlarmId ) {
    return remote().getAlarmFlacon( aAlarmId );
  }

  @Override
  public ITimedList<ISkAnnounceThreadHistoryItem> getAlarmHistory( long aAlarmId ) {
    return remote().getAlarmHistory( aAlarmId );
  }

  @Override
  public ITimedList<ISkAlarm> queryAlarms( ITimeInterval aTimeInterval, ITsCombiFilterParams aQueryParams ) {
    TsNullArgumentRtException.checkNulls( aTimeInterval, aQueryParams );
    return remote().queryAlarms( aTimeInterval, aQueryParams );
  }

}
