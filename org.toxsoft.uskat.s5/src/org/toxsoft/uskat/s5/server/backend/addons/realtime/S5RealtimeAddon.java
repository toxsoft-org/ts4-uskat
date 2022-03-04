package org.toxsoft.uskat.s5.server.backend.addons.realtime;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.addons.realtime.IS5Resources.*;
import static ru.uskat.backend.addons.realtime.ISkBackendAddonRealtime.*;

import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.uskat.s5.client.remote.S5BackendAddonRemote;
import org.toxsoft.uskat.s5.client.remote.addons.S5RealtimeRemote;
import org.toxsoft.uskat.s5.server.backend.IS5BackendAddonSession;
import org.toxsoft.uskat.s5.server.backend.addons.S5BackendAddon;
import org.toxsoft.uskat.s5.server.sessions.S5BackendAddonLocal;
import org.toxsoft.uskat.s5.server.sessions.S5BackendAddonSession;

import ru.uskat.core.api.cmds.ISkCommandService;
import ru.uskat.core.api.events.ISkEventService;
import ru.uskat.core.api.rtdata.ISkRtDataService;
import ru.uskat.core.devapi.IDevCoreApi;
import ru.uskat.core.impl.*;

/**
 * Расширение бекенда: 'Реальное время'
 *
 * @author mvk
 */
public final class S5RealtimeAddon
    extends S5BackendAddon {

  /**
   * Конструктор
   */
  public S5RealtimeAddon() {
    super( SK_BACKEND_ADDON_ID, STR_N_BACKEND_REALTIME, STR_D_BACKEND_REALTIME );
  }

  // ------------------------------------------------------------------------------------
  // S5BackendAddon
  //
  @Override
  protected IStringMap<AbstractSkService> doCreateServices( IDevCoreApi aCoreApi ) {
    IStringMapEdit<AbstractSkService> retValue = new StringMap<>();
    retValue.put( ISkRtDataService.SERVICE_ID, new SkRtDataService( aCoreApi ) );
    retValue.put( ISkCommandService.SERVICE_ID, new SkCommandService( aCoreApi ) );
    retValue.put( ISkEventService.SERVICE_ID, new SkEventService( aCoreApi ) );
    return retValue;
  }

  @Override
  protected Pair<Class<? extends IS5BackendAddonSession>, Class<? extends S5BackendAddonSession>> doGetSessionClasses() {
    return new Pair<>( IS5RealtimeSession.class, S5RealtimeSession.class );
  }

  @Override
  protected S5BackendAddonLocal doCreateLocalClient( ITsContextRo aArgs ) {
    return new S5RealtimeLocal( aArgs );
  }

  @Override
  protected S5BackendAddonRemote<?> doCreateRemoteClient( ITsContextRo aArgs ) {
    return new S5RealtimeRemote( aArgs );
  }

  @Override
  protected IStringList doSupportSingletonIds() {
    return new StringArrayList( //
        BACKEND_EVENTS_SINGLETON, //
        BACKEND_COMMANDS_SINGLETON, //
        BACKEND_RTDATA_SINGLETON //
    );
  }

}
