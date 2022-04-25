package org.toxsoft.uskat.core.backend;

import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.uskat.core.api.*;

/**
 * The "rear" part of the core API implementation that is the frontend for {@link ISkBackend}.
 * <p>
 * All arguments of all methods must be an effectively immutable instances because they are used directly in
 * multithreaded environment, without creating defensive copies.
 *
 * @author goga
 */
public interface ISkFrontendRear {

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
