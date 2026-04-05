package org.toxsoft.uskat.s5.schedules.addons;

import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.schedules.lib.*;
import org.toxsoft.uskat.s5.schedules.supports.*;
import org.toxsoft.uskat.s5.server.backend.addons.*;

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
    schedulesSupport = aOwner.backendSingleton().findSupport( S5BackendSchedulesSingleton.BACKEND_SCHEDULES_ID,
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
