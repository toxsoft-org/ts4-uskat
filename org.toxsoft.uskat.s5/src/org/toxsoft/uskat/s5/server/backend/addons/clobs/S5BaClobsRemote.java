package org.toxsoft.uskat.s5.server.backend.addons.clobs;

import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.api.IBaClobs;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendRemote;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonRemote;

/**
 * Remote {@link IBaClobs} implementation.
 *
 * @author mvk
 */
class S5BaClobsRemote
    extends S5AbstractBackendAddonRemote<IS5BaClobsSession>
    implements IBaClobs {

  /**
   * Данные конфигурации фронтенда для {@link IBaClobs}
   */
  private final S5BaClobsData baData = new S5BaClobsData();

  /**
   * Constructor.
   *
   * @param aOwner {@link IS5BackendRemote} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5BaClobsRemote( IS5BackendRemote aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_CLOBS, IS5BaClobsSession.class );
    // Установка конфигурации фронтенда
    frontend().frontendData().setBackendAddonData( IBaClobs.ADDON_ID, baData );
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
  // IBaClobs
  //
  @Override
  public String readClob( Gwid aGwid ) {
    TsNullArgumentRtException.checkNull( aGwid );
    return session().readClob( aGwid );
  }

  @Override
  public void writeClob( Gwid aGwid, String aClob ) {
    TsNullArgumentRtException.checkNulls( aGwid, aClob );
    session().writeClob( aGwid, aClob );
  }
}
