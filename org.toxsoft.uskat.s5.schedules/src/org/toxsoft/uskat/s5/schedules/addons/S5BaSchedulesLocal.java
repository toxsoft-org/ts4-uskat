package org.toxsoft.uskat.s5.schedules.addons;

import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.schedules.lib.IBaSchedules;
import org.toxsoft.uskat.s5.schedules.lib.ISkSchedulesHardConstants;
import org.toxsoft.uskat.s5.schedules.supports.IS5BackendSchedulesSingleton;
import org.toxsoft.uskat.s5.schedules.supports.S5BackendSchedulesSingleton;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendLocal;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonLocal;

/**
 * Local {@link IBaSchedules} implementation.
 *
 * @author mvk
 */
public final class S5BaSchedulesLocal
    extends S5AbstractBackendAddonLocal
    implements IBaSchedules {

  /**
   * Поддержка бекенда службы
   */
  @SuppressWarnings( "unused" )
  private final IS5BackendSchedulesSingleton schedulesSupport;

  /**
   * Данные конфигурации фронтенда для {@link IBaSchedules}
   */
  private final S5BaSchedulesData baData = new S5BaSchedulesData();

  /**
   * Constructor.
   *
   * @param aOwner {@link IS5BackendLocal} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5BaSchedulesLocal( IS5BackendLocal aOwner ) {
    super( aOwner, ISkSchedulesHardConstants.BAINF_SCHEDULES );
    schedulesSupport = aOwner.backendSingleton().get( S5BackendSchedulesSingleton.BACKEND_SCHEDULES_ID,
        IS5BackendSchedulesSingleton.class );
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
