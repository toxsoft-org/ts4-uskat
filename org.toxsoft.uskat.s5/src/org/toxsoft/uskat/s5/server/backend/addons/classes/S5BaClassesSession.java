package org.toxsoft.uskat.s5.server.backend.addons.classes;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;

import java.util.concurrent.TimeUnit;

import javax.ejb.*;

import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoClassInfo;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.api.IBaClasses;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonSession;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.IS5BackendSysDescrSingleton;
import org.toxsoft.uskat.s5.server.sessions.init.IS5SessionInitData;
import org.toxsoft.uskat.s5.server.sessions.init.S5SessionInitResult;
import org.toxsoft.uskat.s5.server.sessions.pas.S5SessionCallbackWriter;

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
class S5BaClassesSession
    extends S5AbstractBackendAddonSession
    implements IS5BaClassesSession {

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
  protected void doAfterInit( S5SessionCallbackWriter aCallbackWriter, IS5SessionInitData aInitData,
      S5SessionInitResult aInitResult ) {
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
