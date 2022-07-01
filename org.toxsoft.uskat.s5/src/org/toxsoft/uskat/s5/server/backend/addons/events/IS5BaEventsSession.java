package org.toxsoft.uskat.s5.server.backend.addons.events;

import javax.ejb.Remote;

import org.toxsoft.uskat.core.backend.api.IBaEvents;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonSession;

/**
 * Сессия расширения backend {@link IBaEvents}
 *
 * @author mvk
 */
@Remote
public interface IS5BaEventsSession
    extends IBaEvents, IS5BackendAddonSession {
  // nop
}
