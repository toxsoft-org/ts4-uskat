package org.toxsoft.uskat.s5.cron.addons;

import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.cron.lib.*;
import org.toxsoft.uskat.s5.cron.supports.*;
import org.toxsoft.uskat.s5.server.backend.addons.*;

import jakarta.ejb.*;

/**
 * Local {@link IBaCrone} implementation.
 *
 * @author mvk
 */
public final class S5BaCronLocal
    extends S5AbstractBackendAddonLocal
    implements IBaCrone {

  /**
   * backend календарей системы.
   */
  @EJB
  private IS5BackendCronSingleton schedulesSupport;

  /**
   * Данные конфигурации фронтенда для {@link IBaCrone}
   */
  private final S5BaCronData baData = new S5BaCronData();

  /**
   * Constructor.
   *
   * @param aOwner {@link IS5BackendLocal} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5BaCronLocal( IS5BackendLocal aOwner ) {
    super( aOwner, ISkCronHardConstants.BAINF_CRON );

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
