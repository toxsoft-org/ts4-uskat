package org.toxsoft.uskat.sysext.alarms.addon;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.sysext.alarms.addon.ISkResources.*;
import static ru.uskat.common.ISkHardConstants.*;

import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.uskat.s5.client.remote.S5BackendAddonRemote;
import org.toxsoft.uskat.s5.server.backend.IS5BackendAddonSession;
import org.toxsoft.uskat.s5.server.backend.addons.S5BackendAddon;
import org.toxsoft.uskat.s5.server.sessions.S5BackendAddonLocal;
import org.toxsoft.uskat.s5.server.sessions.S5BackendAddonSession;
import org.toxsoft.uskat.sysext.alarms.api.ISkAlarmService;
import org.toxsoft.uskat.sysext.alarms.impl.SkAlarmService;

import ru.uskat.core.devapi.IDevCoreApi;
import ru.uskat.core.impl.AbstractSkService;

/**
 * Расширение бекенда: 'Тревоги'
 *
 * @author mvk
 */
public final class SkAlarmAddon
    extends S5BackendAddon {

  /**
   * Идентификатор адона.
   */
  public static final String SK_BACKEND_ADDON_ID = SK_ID + ".backend.addon.alarms"; //$NON-NLS-1$

  /**
   * Конструктор
   */
  public SkAlarmAddon() {
    super( SK_BACKEND_ADDON_ID, STR_N_BACKEND_ALARMS, STR_D_BACKEND_ALARMS );
  }

  // ------------------------------------------------------------------------------------
  // S5BackendAddon
  //
  @Override
  protected IStringMap<AbstractSkService> doCreateServices( IDevCoreApi aCoreApi ) {
    IStringMapEdit<AbstractSkService> retValue = new StringMap<>();
    retValue.put( ISkAlarmService.SERVICE_ID, new SkAlarmService( aCoreApi ) );
    return retValue;
  }

  @Override
  protected Pair<Class<? extends IS5BackendAddonSession>, Class<? extends S5BackendAddonSession>> doGetSessionClasses() {
    return super.doGetSessionClasses();
  }

  @Override
  protected S5BackendAddonLocal doCreateLocalClient( ITsContextRo aArgs ) {
    return super.doCreateLocalClient( aArgs );
  }

  @Override
  protected S5BackendAddonRemote<?> doCreateRemoteClient( ITsContextRo aArgs ) {
    return super.doCreateRemoteClient( aArgs );
  }

  @Override
  protected IStringList doSupportSingletonIds() {
    return new StringArrayList( //
        BACKEND_SYSDESCR_SINGLETON, //
        BACKEND_OBJECTS_SINGLETON, //
        BACKEND_LINKS_SINGLETON );
  }

}
