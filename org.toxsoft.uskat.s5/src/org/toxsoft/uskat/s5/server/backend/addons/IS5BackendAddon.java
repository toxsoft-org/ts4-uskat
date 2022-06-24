package org.toxsoft.uskat.s5.server.backend.addons;

import org.toxsoft.core.tslib.bricks.ICooperativeMultiTaskable;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.uskat.core.api.ISkService;
import org.toxsoft.uskat.core.backend.api.IBackendAddon;

/**
 * Расширение бекенда предоставляемое сервером s5
 *
 * @author mvk
 */
public interface IS5BackendAddon
    extends IBackendAddon, ICooperativeMultiTaskable, ICloseable {

  /**
   * Called by backend when there is a message forfrontend.
   * <p>
   * Frontend implementation redirects the message to the appropriate core service. The topic ID
   * {@link GtMessage#topicId()} of the message is the service ID {@link ISkService#serviceId()}.
   * <p>
   * Note on frontend implementation: frontend puts message in queue and returns immediately.
   *
   * @param aMessage {@link GtMessage} - the message from frontend
   */
  void onBackendMessage( GtMessage aMessage );
}
