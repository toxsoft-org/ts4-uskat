package org.toxsoft.uskat.s5.server.backend.addons.classes;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;

import java.util.concurrent.*;

import javax.ejb.*;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.s5.server.backend.addons.*;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.*;
import org.toxsoft.uskat.s5.server.sessions.init.*;
import org.toxsoft.uskat.s5.server.sessions.pas.*;

/**
 * Реализация сессии расширения бекенда {@link IS5BaClassesSession}.
 *
 * @author mvk
 */
@Stateful
@StatefulTimeout( value = STATEFULL_TIMEOUT, unit = TimeUnit.MILLISECONDS )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
public class S5BaClassesSession
    extends S5AbstractBackendAddonSession
    implements IS5BaClassesSession, IS5BackendAddonSessionControl {

  private static final long serialVersionUID = 157157L;

  /**
   * Поддержка сервера для чтения/записи системного описания
   */
  @EJB
  private IS5BackendSysDescrSingleton sysdescrSupport;

  /**
   * Пустой конструктор.
   */
  public S5BaClassesSession() {
    super( ISkBackendHardConstant.BAINF_CLASSES );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendAddonSession
  //
  @Override
  protected Class<? extends IS5BaClassesSession> doGetSessionView() {
    return IS5BaClassesSession.class;
  }

  @Override
  protected void doAfterInit( S5SessionMessenger aMessanger, IS5SessionInitData aInitData,
      S5SessionInitResult aInitResult ) {
    aInitResult.setBackendAddonData( IBaClasses.ADDON_ID, new S5BaClassesData() );
    aInitResult.getBackendAddonData( IBaClasses.ADDON_ID, S5BaClassesData.class ).classInfos.setAll( readClassInfos() );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5BaClassesSession
  //
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public IStridablesList<IDtoClassInfo> readClassInfos() {
    return sysdescrSupport.readClassInfos();
  }

  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public void writeClassInfos( IStringList aRemoveClassIds, IStridablesList<IDtoClassInfo> aUpdateClassInfos ) {
    TsNullArgumentRtException.checkNulls( aRemoveClassIds, aUpdateClassInfos );
    sysdescrSupport.writeClassInfos( aRemoveClassIds, aUpdateClassInfos );
  }
}
