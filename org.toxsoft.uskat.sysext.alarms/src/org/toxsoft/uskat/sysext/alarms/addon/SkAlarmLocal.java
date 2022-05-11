package org.toxsoft.uskat.sysext.alarms.addon;

import static org.toxsoft.uskat.sysext.alarms.addon.ISkResources.*;

import org.toxsoft.core.tslib.bricks.filter.ITsCombiFilterParams;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.client.local.S5LocalBackend;
import org.toxsoft.uskat.s5.server.backend.IS5BackendCoreSingleton;
import org.toxsoft.uskat.s5.server.sessions.S5BackendAddonLocal;
import org.toxsoft.uskat.sysext.alarms.api.*;
import org.toxsoft.uskat.sysext.alarms.api.flacon.ISkAlarmFlacon;
import org.toxsoft.uskat.sysext.alarms.supports.ISkBackendAlarmsSingleton;
import org.toxsoft.uskat.sysext.alarms.supports.SkBackendAlarmsSingleton;

/**
 * Реализация локального доступа к расширению backend {@link ISkBackendAddonAlarm}.
 *
 * @author mvk
 */
public final class SkAlarmLocal
    extends S5BackendAddonLocal
    implements ISkBackendAddonAlarm {

  private static final long serialVersionUID = 157157L;

  /**
   * Поддержка бекенда службы качества данных
   */
  private ISkBackendAlarmsSingleton support;

  /**
   * Пустой конструктор.
   */
  public SkAlarmLocal() {
    super( S5_BACKEND_ALARMS_ID, STR_N_BACKEND_ALARMS );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов
  //
  @Override
  public void doAfterInit( S5LocalBackend aOwner, IS5BackendCoreSingleton aBackend ) {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация ISkBackendAddonAlarm
  //

  @Override
  public IStridablesList<ISkAlarmDef> listAlarmDefs() {
    return support().listAlarmDefs();
  }

  @Override
  public ISkAlarmDef findAlarmDef( String aAlarmDefId ) {
    TsNullArgumentRtException.checkNull( aAlarmDefId );
    return support().findAlarmDef( aAlarmDefId );
  }

  @Override
  public void registerAlarmDef( ISkAlarmDef aSkAlarmDef ) {
    TsNullArgumentRtException.checkNull( aSkAlarmDef );
    support().registerAlarmDef( aSkAlarmDef );
  }

  @Override
  public ISkAlarm generateAlarm( String aAlarmDefId, Skid aAuthorId, Skid aUserId, byte aSublevel,
      ISkAlarmFlacon aSkAlarmFlacon ) {
    TsNullArgumentRtException.checkNulls( aAlarmDefId, aAuthorId, aUserId, aSkAlarmFlacon );
    return support().generateAlarm( aAlarmDefId, aAuthorId, aUserId, aSublevel, aSkAlarmFlacon );
  }

  @Override
  public void addAnnounceThreadHistoryItem( long aAlarmId, ISkAnnounceThreadHistoryItem aItem ) {
    TsNullArgumentRtException.checkNull( aItem );
    support().addAnnounceThreadHistoryItem( aAlarmId, aItem );
  }

  @Override
  public ISkAlarmFlacon getAlarmFlacon( long aAlarmId ) {
    return support().getAlarmFlacon( aAlarmId );
  }

  @Override
  public ITimedList<ISkAnnounceThreadHistoryItem> getAlarmHistory( long aAlarmId ) {
    return support().getAlarmHistory( aAlarmId );
  }

  @Override
  public ITimedList<ISkAlarm> queryAlarms( ITimeInterval aTimeInterval, ITsCombiFilterParams aQueryParams ) {
    TsNullArgumentRtException.checkNulls( aTimeInterval, aQueryParams );
    return support().queryAlarms( aTimeInterval, aQueryParams );
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Возвращает поддержку расширения бекенда
   *
   * @return {@link ISkBackendAlarmsSingleton} синглетон реализации поддержки
   */
  private ISkBackendAlarmsSingleton support() {
    if( support == null ) {
      support = supports().get( SkBackendAlarmsSingleton.BACKEND_ALARMS_ID, ISkBackendAlarmsSingleton.class );
    }
    return support;
  }
}
