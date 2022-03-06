package org.toxsoft.uskat.core.backend;

import org.toxsoft.core.tslib.bricks.events.msg.*;

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
   * TODO ??? Note on frontend implementation. Frontend puts message in queue and returns immediately.
   *
   * @param aMessage {@link GtMessage} - the message from backend
   */
  void onBackendMessage( GtMessage aMessage );

}
