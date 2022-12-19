package org.toxsoft.uskat.s5.server.backend.addons.gwiddb;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;

import java.util.concurrent.TimeUnit;

import javax.ejb.*;

import org.toxsoft.core.tslib.bricks.strid.more.IdChain;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.api.IBaGwidDb;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonSessionControl;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonSession;
import org.toxsoft.uskat.s5.server.backend.supports.gwiddb.IS5BackendGwidDbSingleton;
import org.toxsoft.uskat.s5.server.sessions.init.IS5SessionInitData;
import org.toxsoft.uskat.s5.server.sessions.init.S5SessionInitResult;
import org.toxsoft.uskat.s5.server.sessions.pas.S5SessionMessenger;

/**
 * Реализация сессии расширения бекенда {@link IS5BaGwidDbSession}.
 *
 * @author mvk
 */
@Stateful
@StatefulTimeout( value = STATEFULL_TIMEOUT, unit = TimeUnit.MILLISECONDS )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
@SuppressWarnings( "unused" )
public class S5BaGwidDbSession
    extends S5AbstractBackendAddonSession
    implements IS5BaGwidDbSession, IS5BackendAddonSessionControl {

  private static final long serialVersionUID = 157157L;

  /**
   * Поддержка сервера запросов {@link IBaGwidDb}
   */
  @EJB
  private IS5BackendGwidDbSingleton gwidDbSupport;

  /**
   * Пустой конструктор.
   */
  public S5BaGwidDbSession() {
    super( ISkBackendHardConstant.BAINF_GWID_DB );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendAddonSession
  //
  @Override
  protected Class<? extends IS5BaGwidDbSession> doGetSessionView() {
    return IS5BaGwidDbSession.class;
  }

  @Override
  protected void doAfterInit( S5SessionMessenger aMessenger, IS5SessionInitData aInitData,
      S5SessionInitResult aInitResult ) {
    S5BaGwidDbData baData = new S5BaGwidDbData();
    frontend().frontendData().setBackendAddonData( IBaGwidDb.ADDON_ID, baData );
  }

  @Override
  protected void doBeforeClose() {
    // S5BaGwidDbData baData = frontend().frontendData().findBackendAddonData( IBaGwidDb.ADDON_ID, S5BaGwidDbData.class
    // );
    // // Список идентификаторов открытых запросов
    // IStringList queryIds;
    // synchronized (baData) {
    // queryIds = new StringArrayList( baData.openQueries.keys() );
    // }
    // // Завершение работы открытых запросов
    // for( String queryId : queryIds ) {
    // gwidDbSupport.close( frontend(), queryId );
    // }
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5BaGwidDbSession
  //
  @Override
  public IList<IdChain> listSectionIds() {
    return gwidDbSupport.listSectionIds();
  }

  @Override
  public IList<Gwid> listKeys( IdChain aSectionId ) {
    TsNullArgumentRtException.checkNull( aSectionId );
    return gwidDbSupport.listKeys( aSectionId );
  }

  @Override
  public String readValue( IdChain aSectionId, Gwid aKey ) {
    TsNullArgumentRtException.checkNulls( aSectionId, aKey );
    return gwidDbSupport.readValue( aSectionId, aKey );
  }

  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public void writeValue( IdChain aSectionId, Gwid aKey, String aValue ) {
    TsNullArgumentRtException.checkNulls( aSectionId, aKey, aValue );
    gwidDbSupport.writeValue( aSectionId, aKey, aValue );
  }

  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public void removeValue( IdChain aSectionId, Gwid aKey ) {
    TsNullArgumentRtException.checkNulls( aSectionId, aKey );
    gwidDbSupport.removeValue( aSectionId, aKey );
  }

  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public void removeSection( IdChain aSectionId ) {
    TsNullArgumentRtException.checkNull( aSectionId );
    gwidDbSupport.removeSection( aSectionId );
  }
}
