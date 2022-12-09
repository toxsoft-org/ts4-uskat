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
import org.toxsoft.uskat.alarms.s5.supports.IS5BackendAlarmSingleton;
import org.toxsoft.uskat.alarms.s5.supports.S5BackendAlarmSingleton;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendLocal;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonLocal;

/**
 * Local {@link IBaAlarms} implementation.
 *
 * @author mvk
 */
public final class S5BaAlarmLocal
    extends S5AbstractBackendAddonLocal
    implements IBaAlarms {

  /**
   * Поддержка бекенда службы алармов
   */
  private final IS5BackendAlarmSingleton alarmsSupport;

  /**
   * Данные конфигурации фронтенда для {@link IBaAlarms}
   */
  private final S5BaAlarmData baData = new S5BaAlarmData();

  /**
   * Constructor.
   *
   * @param aOwner {@link IS5BackendLocal} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5BaAlarmLocal( IS5BackendLocal aOwner ) {
    super( aOwner, ISkAlarmServiceHardConstants.BAINF_ALARMS );
    alarmsSupport =
        aOwner.backendSingleton().get( S5BackendAlarmSingleton.BACKEND_ALARMS_ID, IS5BackendAlarmSingleton.class );
    // Установка конфигурации фронтенда
    frontend().frontendData().setBackendAddonData( IBaAlarms.ADDON_ID, baData );
  }

  // ------------------------------------------------------------------------------------
  // BackendAddonBase
  //
  @Override
  public void onBackendMessage( GtMessage aMessage ) {
    // if( aMessage.messageId().equals( S5BaAfterInitMessages.MSG_ID ) ) {
    // }
  }

  @Override
  public void close() {
    // // Список идентификаторов открытых запросов
    // IStringList queryIds;
    // synchronized (baData) {
    // queryIds = new StringArrayList( baData.openQueries.keys() );
    // }
    // // Завершение работы открытых запросов
    // for( String queryId : queryIds ) {
    // queriesSupport.close( frontend(), queryId );
    // }
  }

  // ------------------------------------------------------------------------------------
  // Реализация IBaDataQuality
  //
  @Override
  public IStridablesList<ISkAlarmDef> listAlarmDefs() {
    return alarmsSupport.listAlarmDefs();
  }

  @Override
  public ISkAlarmDef findAlarmDef( String aAlarmDefId ) {
    TsNullArgumentRtException.checkNull( aAlarmDefId );
    return alarmsSupport.findAlarmDef( aAlarmDefId );
  }

  @Override
  public void registerAlarmDef( ISkAlarmDef aAlarmDef ) {
    TsNullArgumentRtException.checkNull( aAlarmDef );
    alarmsSupport.registerAlarmDef( aAlarmDef );
  }

  @Override
  public void setAlarmFilters( IList<ITsCombiFilterParams> aFilters ) {
    TsNullArgumentRtException.checkNull( aFilters );
    synchronized (baData) {
      baData.alarmFilters.setAll( aFilters );
    }
  }

  @Override
  public ISkAlarm generateAlarm( String aAlarmDefId, Skid aAuthorId, Skid aUserId, byte aSublevel,
      ISkAlarmFlacon aAlarmFlacon ) {
    TsNullArgumentRtException.checkNulls( aAlarmDefId, aAuthorId, aUserId, aAlarmFlacon );
    return alarmsSupport.generateAlarm( aAlarmDefId, aAuthorId, aUserId, aSublevel, aAlarmFlacon );
  }

  @Override
  public void addThreadHistoryItem( long aAlarmId, ISkAlarmThreadHistoryItem aItem ) {
    TsNullArgumentRtException.checkNull( aItem );
    alarmsSupport.addAnnounceThreadHistoryItem( aAlarmId, aItem );
  }

  @Override
  public ITimedList<ISkAlarm> queryAlarms( ITimeInterval aTimeInterval, ITsCombiFilterParams aFilter ) {
    TsNullArgumentRtException.checkNulls( aTimeInterval, aFilter );
    return alarmsSupport.queryAlarms( aTimeInterval, aFilter );
  }
}
