package org.toxsoft.uskat.alarms.s5.addons;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;

import java.util.concurrent.TimeUnit;

import javax.ejb.*;

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
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonSessionControl;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonSession;
import org.toxsoft.uskat.s5.server.sessions.init.IS5SessionInitData;
import org.toxsoft.uskat.s5.server.sessions.init.S5SessionInitResult;
import org.toxsoft.uskat.s5.server.sessions.pas.S5SessionMessenger;

/**
 * Сессия реализации расширения бекенда {@link IBaAlarms}.
 *
 * @author mvk
 */
@Stateful
@StatefulTimeout( value = STATEFULL_TIMEOUT, unit = TimeUnit.MILLISECONDS )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
@SuppressWarnings( "unused" )
public class S5BaAlarmSession
    extends S5AbstractBackendAddonSession
    implements IS5BaAlarmSession, IS5BackendAddonSessionControl {

  private static final long serialVersionUID = 157157L;

  /**
   * Поддержка бекенда службы алармов
   */
  @EJB
  private IS5BackendAlarmSingleton alarmsSupport;

  /**
   * Пустой конструктор.
   */
  public S5BaAlarmSession() {
    super( ISkAlarmServiceHardConstants.BAINF_ALARMS );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendAddonSession
  //
  @Override
  protected Class<? extends IS5BaAlarmSession> doGetSessionView() {
    return IS5BaAlarmSession.class;
  }

  @Override
  protected void doAfterInit( S5SessionMessenger aMessenger, IS5SessionInitData aInitData,
      S5SessionInitResult aInitResult ) {
    S5BaAlarmData baData = new S5BaAlarmData();
    frontend().frontendData().setBackendAddonData( IBaAlarms.ADDON_ID, baData );
  }

  @Override
  protected void doBeforeClose() {
    S5BaAlarmData baData = frontend().frontendData().findBackendAddonData( IBaAlarms.ADDON_ID, S5BaAlarmData.class );
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
  // IS5BaAlarmSession
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
    S5BaAlarmData baData = frontend().frontendData().findBackendAddonData( IBaAlarms.ADDON_ID, S5BaAlarmData.class );
    synchronized (baData) {
      baData.alarmFilters.setAll( aFilters );
      writeSessionData();
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
