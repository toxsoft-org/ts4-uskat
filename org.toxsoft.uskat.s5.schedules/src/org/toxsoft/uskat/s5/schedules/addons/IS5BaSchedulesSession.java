package org.toxsoft.uskat.s5.schedules.addons;

import org.toxsoft.uskat.s5.schedules.lib.*;
import org.toxsoft.uskat.s5.server.backend.addons.*;

import jakarta.ejb.*;

/**
 * Сессия службы {@link IBaSchedules}.
 *
 * @author mvk
 */
@Remote
public interface IS5BaSchedulesSession
    extends IBaSchedules, IS5BackendAddonSession {
  // nop
}
