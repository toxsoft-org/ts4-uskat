package org.toxsoft.uskat.s5.server.backend.addons.objects;

import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.objserv.IDtoObject;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.api.IBaObjects;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendLocal;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonLocal;
import org.toxsoft.uskat.s5.server.backend.supports.objects.IS5BackendObjectsSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.objects.S5BackendObjectsSingleton;

/**
 * Local {@link IBaObjects} implementation.
 *
 * @author mvk
 */
class S5BaObjectsLocal
    extends S5AbstractBackendAddonLocal
    implements IBaObjects {

  /**
   * Поддержка сервера для чтения/записи объектов системы
   */
  private final IS5BackendObjectsSingleton objectsSupport;

  /**
   * Constructor.
   *
   * @param aOwner {@link IS5BackendLocal} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5BaObjectsLocal( IS5BackendLocal aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_CLASSES );
    // Синглтон поддержки чтения/записи системного описания
    objectsSupport =
        aOwner.backendSingleton().get( S5BackendObjectsSingleton.BACKEND_OBJECTS_ID, IS5BackendObjectsSingleton.class );
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
  // IBaObjects
  //
  @Override
  public IDtoObject findObject( Skid aSkid ) {
    TsNullArgumentRtException.checkNull( aSkid );
    return objectsSupport.findObject( aSkid );
  }

  @Override
  public IList<IDtoObject> readObjects( IStringList aClassIds ) {
    TsNullArgumentRtException.checkNull( aClassIds );
    return objectsSupport.readObjects( aClassIds );
  }

  @Override
  public IList<IDtoObject> readObjectsByIds( ISkidList aSkids ) {
    TsNullArgumentRtException.checkNull( aSkids );
    return objectsSupport.readObjectsByIds( aSkids );
  }

  @Override
  public void writeObjects( ISkidList aRemoveSkids, IList<IDtoObject> aUpdateObjects ) {
    TsNullArgumentRtException.checkNulls( aRemoveSkids, aUpdateObjects );
    objectsSupport.writeObjects( frontend(), aRemoveSkids, aUpdateObjects, true );
  }
}
