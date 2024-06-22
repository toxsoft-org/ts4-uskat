package org.toxsoft.uskat.s5.schedules.addons;

import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.schedules.lib.IBaSchedules;
import org.toxsoft.uskat.s5.schedules.lib.ISkSchedulesHardConstants;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendRemote;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonRemote;

/**
 * Remote {@link IBaSchedules} implementation.
 *
 * @author mvk
 */
public final class S5BaSchedulesRemote
    extends S5AbstractBackendAddonRemote<IS5BaSchedulesSession>
    implements IBaSchedules {

  /**
   * Данные конфигурации фронтенда для {@link IBaSchedules}
   */
  private final S5BaSchedulesData baData = new S5BaSchedulesData();

  /**
   * Constructor.
   *
   * @param aOwner {@link IS5BackendRemote} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5BaSchedulesRemote( IS5BackendRemote aOwner ) {
    super( aOwner, ISkSchedulesHardConstants.BAINF_SCHEDULES, IS5BaSchedulesSession.class );
    // Установка конфигурации фронтенда
    frontend().frontendData().setBackendAddonData( IBaSchedules.ADDON_ID, baData );
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
  // Реализация IBaSchedules
  //
}
