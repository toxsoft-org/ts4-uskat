package org.toxsoft.uskat.s5.server.backend.addons.events;

import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.s5.server.backend.addons.*;

import jakarta.ejb.*;

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
