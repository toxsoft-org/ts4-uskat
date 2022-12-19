package org.toxsoft.uskat.s5.server.backend.addons.gwiddb;

import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.strid.more.IdChain;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.api.IBaClobs;
import org.toxsoft.uskat.core.backend.api.IBaGwidDb;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendRemote;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonRemote;

/**
 * Remote {@link IBaGwidDb} implementation.
 *
 * @author mvk
 */
class S5BaGwidDbRemote
    extends S5AbstractBackendAddonRemote<IS5BaGwidDbSession>
    implements IBaGwidDb {

  /**
   * Данные конфигурации фронтенда для {@link IBaClobs}
   */
  private final S5BaGwidDbData baData = new S5BaGwidDbData();

  /**
   * Constructor.
   *
   * @param aOwner {@link IS5BackendRemote} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5BaGwidDbRemote( IS5BackendRemote aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_GWID_DB, IS5BaGwidDbSession.class );
    // Установка конфигурации фронтенда
    frontend().frontendData().setBackendAddonData( IBaGwidDb.ADDON_ID, baData );
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
  // IBaGwidDb
  //
  @Override
  public IList<IdChain> listSectionIds() {
    return session().listSectionIds();
  }

  @Override
  public IList<Gwid> listKeys( IdChain aSectionId ) {
    TsNullArgumentRtException.checkNull( aSectionId );
    return session().listKeys( aSectionId );
  }

  @Override
  public String readValue( IdChain aSectionId, Gwid aKey ) {
    TsNullArgumentRtException.checkNulls( aSectionId, aKey );
    return session().readValue( aSectionId, aKey );
  }

  @Override
  public void writeValue( IdChain aSectionId, Gwid aKey, String aValue ) {
    TsNullArgumentRtException.checkNulls( aSectionId, aKey, aValue );
    session().writeValue( aSectionId, aKey, aValue );
  }

  @Override
  public void removeValue( IdChain aSectionId, Gwid aKey ) {
    TsNullArgumentRtException.checkNulls( aSectionId, aKey );
    session().removeValue( aSectionId, aKey );
  }

  @Override
  public void removeSection( IdChain aSectionId ) {
    TsNullArgumentRtException.checkNull( aSectionId );
    session().removeSection( aSectionId );
  }
}
