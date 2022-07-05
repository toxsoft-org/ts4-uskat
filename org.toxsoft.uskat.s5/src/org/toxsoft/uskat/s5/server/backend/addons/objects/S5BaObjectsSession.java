package org.toxsoft.uskat.s5.server.backend.addons.objects;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;

import java.util.concurrent.TimeUnit;

import javax.ejb.*;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.objserv.IDtoObject;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonSession;
import org.toxsoft.uskat.s5.server.backend.supports.objects.IS5BackendObjectsSingleton;
import org.toxsoft.uskat.s5.server.sessions.init.IS5SessionInitData;
import org.toxsoft.uskat.s5.server.sessions.init.S5SessionInitResult;
import org.toxsoft.uskat.s5.server.sessions.pas.S5SessionCallbackWriter;

/**
 * Реализация сессии расширения бекенда {@link IS5BaObjectsSession}.
 *
 * @author mvk
 */
@Stateful
@StatefulTimeout( value = STATEFULL_TIMEOUT, unit = TimeUnit.MILLISECONDS )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
class S5BaObjectsSession
    extends S5AbstractBackendAddonSession
    implements IS5BaObjectsSession {

  private static final long serialVersionUID = 157157L;

  /**
   * Поддержка сервера для чтения/записи объектов системы
   */
  @EJB
  private IS5BackendObjectsSingleton objectsSupport;

  /**
   * Пустой конструктор.
   */
  public S5BaObjectsSession() {
    super( ISkBackendHardConstant.BAINF_OBJECTS );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendAddonSession
  //
  @Override
  protected Class<? extends IS5BaObjectsSession> doGetSessionView() {
    return IS5BaObjectsSession.class;
  }

  @Override
  protected void doAfterInit( S5SessionCallbackWriter aCallbackWriter, IS5SessionInitData aInitData,
      S5SessionInitResult aInitResult ) {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5BaObjectsSession
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
    TsNullArgumentRtException.checkNulls( aSkids );
    return objectsSupport.readObjectsByIds( aSkids );
  }

  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public void writeObjects( ISkidList aRemoveSkids, IList<IDtoObject> aUpdateObjects ) {
    TsNullArgumentRtException.checkNulls( aRemoveSkids, aUpdateObjects );
    objectsSupport.writeObjects( frontend(), aRemoveSkids, aUpdateObjects, true );
  }
}
