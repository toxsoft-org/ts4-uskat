package org.toxsoft.uskat.s5.cron.addons;

import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.cron.lib.IBaCrone;
import org.toxsoft.uskat.s5.cron.lib.ISkCronHardConstants;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendRemote;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonRemote;

/**
 * Remote {@link IBaCrone} implementation.
 *
 * @author mvk
 */
public final class S5BaCronRemote
    extends S5AbstractBackendAddonRemote<IS5BaCronSession>
    implements IBaCrone {

  /**
   * Данные конфигурации фронтенда для {@link IBaCrone}
   */
  private final S5BaCronData baData = new S5BaCronData();

  /**
   * Constructor.
   *
   * @param aOwner {@link IS5BackendRemote} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5BaCronRemote( IS5BackendRemote aOwner ) {
    super( aOwner, ISkCronHardConstants.BAINF_CRON, IS5BaCronSession.class );
    // Установка конфигурации фронтенда
    frontend().frontendData().setBackendAddonData( IBaCrone.ADDON_ID, baData );
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
  // Реализация IBaCrone
  //
}
