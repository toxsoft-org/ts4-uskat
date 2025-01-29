package org.toxsoft.uskat.s5.server.backend.addons.gwiddb;

import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.strid.more.IdChain;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.api.IBaClobs;
import org.toxsoft.uskat.core.backend.api.IBaGwidDb;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendLocal;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonLocal;
import org.toxsoft.uskat.s5.server.backend.supports.gwiddb.IS5BackendGwidDbSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.gwiddb.S5BackendGwidDbSingleton;

/**
 * Local {@link IBaGwidDb} implementation.
 *
 * @author mvk
 */
class S5BaGwidDbLocal
    extends S5AbstractBackendAddonLocal
    implements IBaGwidDb {

  /**
   * Поддержка сервера запросов {@link IBaGwidDb}
   */
  private final IS5BackendGwidDbSingleton gwidDbSupport;

  /**
   * Данные конфигурации фронтенда для {@link IBaClobs}
   */
  private final S5BaGwidDbData baData = new S5BaGwidDbData();

  /**
   * Constructor.
   *
   * @param aOwner {@link IS5BackendLocal} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5BaGwidDbLocal( IS5BackendLocal aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_GWID_DB );
    // Синглтон поддержки чтения/записи системного описания
    gwidDbSupport =
        aOwner.backendSingleton().findSupport( S5BackendGwidDbSingleton.BACKEND_GWIDDB_ID, IS5BackendGwidDbSingleton.class );
    // Установка конфигурации фронтенда
    frontend().frontendData().setBackendAddonData( IBaGwidDb.ADDON_ID, baData );
  }

  // ------------------------------------------------------------------------------------
  // BackendAddonBase
  //
  @Override
  public void onBackendMessage( GtMessage aMessage ) {
    // if( aMessage.messageId().equals( S5BaAfterInitMessages.MSG_ID ) ) {
    // }
  }

  @Override
  public void close() {
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
  // IBaGwidDb
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

  @Override
  public void writeValue( IdChain aSectionId, Gwid aKey, String aValue ) {
    TsNullArgumentRtException.checkNulls( aSectionId, aKey, aValue );
    gwidDbSupport.writeValue( aSectionId, aKey, aValue );
  }

  @Override
  public void removeValue( IdChain aSectionId, Gwid aKey ) {
    TsNullArgumentRtException.checkNulls( aSectionId, aKey );
    gwidDbSupport.removeValue( aSectionId, aKey );
  }

  @Override
  public void removeSection( IdChain aSectionId ) {
    TsNullArgumentRtException.checkNull( aSectionId );
    gwidDbSupport.removeSection( aSectionId );
  }

}
