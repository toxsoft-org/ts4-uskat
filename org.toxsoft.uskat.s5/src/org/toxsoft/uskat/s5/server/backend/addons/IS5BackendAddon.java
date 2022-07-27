package org.toxsoft.uskat.s5.server.backend.addons;

import org.toxsoft.core.tslib.bricks.ICooperativeMultiTaskable;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.uskat.core.backend.api.IBackendAddon;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;

/**
 * Расширение бекенда предоставляемое сервером s5
 *
 * @author mvk
 */
public interface IS5BackendAddon
    extends IBackendAddon, ICooperativeMultiTaskable, ICloseable {

  /**
   * Возвращает фронтенд с который работает бекенд
   *
   * @return {@link IS5FrontendRear} фронтенд
   */
  IS5FrontendRear frontend();

  /**
   * Called by backend when there is a message for frontend.
   * <p>
   * Note on frontend implementation: frontend puts message in queue and returns immediately.
   *
   * @param aMessage {@link GtMessage} - the message from frontend
   */
  void onBackendMessage( GtMessage aMessage );
}
