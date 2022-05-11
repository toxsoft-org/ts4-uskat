package org.toxsoft.uskat.sysext.alarms.addon;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;

import java.util.concurrent.TimeUnit;

import javax.ejb.*;

import org.toxsoft.core.tslib.bricks.filter.ITsCombiFilterParams;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.IS5BackendAddonRemote;
import org.toxsoft.uskat.s5.server.backend.IS5BackendAddonSession;
import org.toxsoft.uskat.s5.server.sessions.S5BackendAddonSession;
import org.toxsoft.uskat.sysext.alarms.api.*;
import org.toxsoft.uskat.sysext.alarms.api.flacon.ISkAlarmFlacon;
import org.toxsoft.uskat.sysext.alarms.supports.ISkBackendAlarmsSingleton;

/**
 * Сессия реализации расширения бекенда {@link ISkBackendAddonAlarm}.
 *
 * @author mvk
 */
@Stateful
@StatefulTimeout( value = STATEFULL_TIMEOUT, unit = TimeUnit.MILLISECONDS )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
public class SkAlarmSession
    extends S5BackendAddonSession
    implements ISkAlarmRemote, ISkAlarmSession {

  private static final long serialVersionUID = 157157L;

  /**
   * Поддержка бекенда службы тревог
   */
  @EJB
  private ISkBackendAlarmsSingleton support;

  /**
   * Пустой конструктор.
   */
  public SkAlarmSession() {
    super( ISkBackendAddonAlarm.S5_BACKEND_ALARMS_ID, ISkBackendAddonAlarm.S5_BACKEND_ALARMS_NAME );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendAddonSession
  //
  @Override
  protected Class<? extends IS5BackendAddonSession> doGetLocalView() {
    return ISkAlarmSession.class;
  }

  @Override
  protected Class<? extends IS5BackendAddonRemote> doGetRemoteView() {
    return ISkAlarmRemote.class;
  }

  // ------------------------------------------------------------------------------------
  // Реализация ISkBackendAddonAlarm
  //
  @Override
  public IStridablesList<ISkAlarmDef> listAlarmDefs() {
    return support.listAlarmDefs();
  }

  @Override
  public ISkAlarmDef findAlarmDef( String aAlarmDefId ) {
    TsNullArgumentRtException.checkNull( aAlarmDefId );
    return support.findAlarmDef( aAlarmDefId );
  }

  @Override
  public void registerAlarmDef( ISkAlarmDef aAlarmDef ) {
    TsNullArgumentRtException.checkNull( aAlarmDef );
    support.registerAlarmDef( aAlarmDef );
  }

  @Override
  public ISkAlarm generateAlarm( String aAlarmDefId, Skid aAuthorId, Skid aUserId, byte aSublevel,
      ISkAlarmFlacon aSkAlarmFlacon ) {
    TsNullArgumentRtException.checkNulls( aAlarmDefId, aAuthorId, aUserId, aSkAlarmFlacon );
    return support.generateAlarm( aAlarmDefId, aAuthorId, aUserId, aSublevel, aSkAlarmFlacon );
  }

  @Override
  public void addAnnounceThreadHistoryItem( long aAlarmId, ISkAnnounceThreadHistoryItem aItem ) {
    TsNullArgumentRtException.checkNull( aItem );
    support.addAnnounceThreadHistoryItem( aAlarmId, aItem );
  }

  @Override
  public ISkAlarmFlacon getAlarmFlacon( long aAlarmId ) {
    return support.getAlarmFlacon( aAlarmId );
  }

  @Override
  public ITimedList<ISkAnnounceThreadHistoryItem> getAlarmHistory( long aAlarmId ) {
    return support.getAlarmHistory( aAlarmId );
  }

  @Override
  public ITimedList<ISkAlarm> queryAlarms( ITimeInterval aTimeInterval, ITsCombiFilterParams aQueryParams ) {
    TsNullArgumentRtException.checkNulls( aTimeInterval, aQueryParams );
    return support.queryAlarms( aTimeInterval, aQueryParams );
  }

}
