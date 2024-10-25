package org.toxsoft.uskat.s5.server.backend.addons.objects;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;

import java.util.concurrent.*;

import javax.ejb.*;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.s5.server.backend.addons.*;
import org.toxsoft.uskat.s5.server.backend.supports.objects.*;
import org.toxsoft.uskat.s5.server.sessions.init.*;
import org.toxsoft.uskat.s5.server.sessions.pas.*;

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
public class S5BaObjectsSession
    extends S5AbstractBackendAddonSession
    implements IS5BaObjectsSession, IS5BackendAddonSessionControl {

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
  protected void doAfterInit( S5SessionMessenger aMessenger, IS5SessionInitData aInitData,
      S5SessionInitResult aInitResult ) {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5BaObjectsSession
  //
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public IDtoObject findObject( Skid aSkid ) {
    TsNullArgumentRtException.checkNull( aSkid );
    return objectsSupport.findObject( aSkid );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public IList<IDtoObject> readObjects( IStringList aClassIds ) {
    TsNullArgumentRtException.checkNull( aClassIds );
    return objectsSupport.readObjects( aClassIds );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
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
