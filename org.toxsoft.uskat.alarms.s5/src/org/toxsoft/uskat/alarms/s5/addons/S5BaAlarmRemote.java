package org.toxsoft.uskat.alarms.s5.addons;

import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.filter.ITsCombiFilterParams;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.alarms.lib.*;
import org.toxsoft.uskat.alarms.lib.flacon.ISkAlarmFlacon;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendRemote;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonRemote;

/**
 * Remote {@link IBaAlarms} implementation.
 *
 * @author mvk
 */
public final class S5BaAlarmRemote
    extends S5AbstractBackendAddonRemote<IS5BaAlarmSession>
    implements IBaAlarms {

  /**
   * Данные конфигурации фронтенда для {@link IBaAlarms}
   */
  private final S5BaAlarmData baData = new S5BaAlarmData();

  /**
   * Constructor.
   *
   * @param aOwner {@link IS5BackendRemote} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5BaAlarmRemote( IS5BackendRemote aOwner ) {
    super( aOwner, ISkAlarmServiceHardConstants.BAINF_ALARMS, IS5BaAlarmSession.class );
    // Установка конфигурации фронтенда
    frontend().frontendData().setBackendAddonData( IBaAlarms.ADDON_ID, baData );
  }

  // ------------------------------------------------------------------------------------
  // BackendAddonBase
  //
  @Override
  public void onBackendMessage( GtMessage aMessage ) {
    // nop
  }

  @Override
  public void close() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация IBaAlarms
  //
  @Override
  public IStridablesList<ISkAlarmDef> listAlarmDefs() {
    return session().listAlarmDefs();
  }

  @Override
  public ISkAlarmDef findAlarmDef( String aAlarmDefId ) {
    TsNullArgumentRtException.checkNull( aAlarmDefId );
    return session().findAlarmDef( aAlarmDefId );
  }

  @Override
  public void registerAlarmDef( ISkAlarmDef aAlarmDef ) {
    TsNullArgumentRtException.checkNull( aAlarmDef );
    session().registerAlarmDef( aAlarmDef );
  }

  @Override
  public void setAlarmFilters( IList<ITsCombiFilterParams> aFilters ) {
    TsNullArgumentRtException.checkNull( aFilters );
    session().setAlarmFilters( aFilters );
  }

  @Override
  public ISkAlarm generateAlarm( String aAlarmDefId, Skid aAuthorId, Skid aUserId, byte aSublevel,
      ISkAlarmFlacon aAlarmFlacon ) {
    TsNullArgumentRtException.checkNulls( aAlarmDefId, aAuthorId, aUserId, aAlarmFlacon );
    return session().generateAlarm( aAlarmDefId, aAuthorId, aUserId, aSublevel, aAlarmFlacon );
  }

  @Override
  public void addThreadHistoryItem( long aAlarmId, ISkAlarmThreadHistoryItem aItem ) {
    TsNullArgumentRtException.checkNull( aItem );
    session().addThreadHistoryItem( aAlarmId, aItem );
  }

  @Override
  public ITimedList<ISkAlarm> queryAlarms( ITimeInterval aTimeInterval, ITsCombiFilterParams aFilter ) {
    TsNullArgumentRtException.checkNulls( aTimeInterval, aFilter );
    return session().queryAlarms( aTimeInterval, aFilter );
  }

}
