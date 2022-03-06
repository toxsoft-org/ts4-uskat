package org.toxsoft.uskat.s5.server.backend.addons.batch;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.addons.batch.IS5Resources.*;
import static ru.uskat.backend.addons.batchops.ISkBackendAddonBatchOperations.*;

import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.uskat.s5.client.remote.S5BackendAddonRemote;
import org.toxsoft.uskat.s5.client.remote.addons.S5BatchOperationsRemote;
import org.toxsoft.uskat.s5.server.backend.IS5BackendAddonSession;
import org.toxsoft.uskat.s5.server.backend.addons.S5BackendAddon;
import org.toxsoft.uskat.s5.server.sessions.S5BackendAddonLocal;
import org.toxsoft.uskat.s5.server.sessions.S5BackendAddonSession;

import ru.uskat.core.devapi.IDevCoreApi;
import ru.uskat.core.impl.AbstractSkService;

/**
 * Расширение бекенда: 'Пакетные операции'
 *
 * @author mvk
 */
public final class S5BatchOperationsAddon
    extends S5BackendAddon {

  /**
   * Конструктор
   */
  public S5BatchOperationsAddon() {
    super( SK_BACKEND_ADDON_ID, STR_N_BACKEND_BATCH, STR_D_BACKEND_BATCH );
  }

  // ------------------------------------------------------------------------------------
  // S5BackendAddon
  //
  @Override
  protected IStringMap<AbstractSkService> doCreateServices( IDevCoreApi aCoreApi ) {
    IStringMapEdit<AbstractSkService> retValue = new StringMap<>();
    retValue.put( ISkBatchOperationService.SERVICE_ID, new SkBatchOperationService( aCoreApi ) );
    return retValue;
  }

  @Override
  protected Pair<Class<? extends IS5BackendAddonSession>, Class<? extends S5BackendAddonSession>> doGetSessionClasses() {
    return new Pair<>( IS5BatchOperationsSession.class, S5BatchOperationsSession.class );
  }

  @Override
  protected S5BackendAddonLocal doCreateLocalClient( ITsContextRo aArgs ) {
    return new S5BatchOperationsLocal();
  }

  @Override
  protected S5BackendAddonRemote<?> doCreateRemoteClient( ITsContextRo aArgs ) {
    return new S5BatchOperationsRemote();
  }

  @Override
  protected IStringList doSupportSingletonIds() {
    return new StringArrayList( //
        BACKEND_SYSDESCR_SINGLETON, //
        BACKEND_OBJECTS_SINGLETON, //
        BACKEND_LINKS_SINGLETON, //
        BACKEND_LOBS_SINGLETON //
    );
  }

}
