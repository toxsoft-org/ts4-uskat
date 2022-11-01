package org.toxsoft.uskat.s5.server.backend.addons.clobs;

import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.api.IBaClobs;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendLocal;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonLocal;
import org.toxsoft.uskat.s5.server.backend.supports.clobs.IS5BackendClobsSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.clobs.S5BackendClobsSingleton;

/**
 * Local {@link IBaClobs} implementation.
 *
 * @author mvk
 */
class S5BaClobsLocal
    extends S5AbstractBackendAddonLocal
    implements IBaClobs {

  /**
   * Поддержка сервера обработки запросов clob
   */
  private final IS5BackendClobsSingleton clobsSupport;

  /**
   * Данные конфигурации фронтенда для {@link IBaClobs}
   */
  private final S5BaClobsData baData = new S5BaClobsData();

  /**
   * Constructor.
   *
   * @param aOwner {@link IS5BackendLocal} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5BaClobsLocal( IS5BackendLocal aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_CLOBS );
    // Синглтон поддержки чтения/записи системного описания
    clobsSupport =
        aOwner.backendSingleton().get( S5BackendClobsSingleton.BACKEND_CLOBS_ID, IS5BackendClobsSingleton.class );
    // Установка конфигурации фронтенда
    frontend().frontendData().setBackendAddonData( IBaClobs.ADDON_ID, baData );
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
    // clobsSupport.close( frontend(), queryId );
    // }
  }

  // ------------------------------------------------------------------------------------
  // IBaClobs
  //
  @Override
  public String readClob( Gwid aGwid ) {
    TsNullArgumentRtException.checkNull( aGwid );
    return clobsSupport.readClob( aGwid );
  }

  @Override
  public void writeClob( Gwid aGwid, String aClob ) {
    TsNullArgumentRtException.checkNulls( aGwid, aClob );
    clobsSupport.writeClob( aGwid, aClob );
  }

}
