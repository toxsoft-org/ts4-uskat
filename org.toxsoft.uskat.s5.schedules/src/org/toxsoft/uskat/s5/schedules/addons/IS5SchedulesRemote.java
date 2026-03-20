package org.toxsoft.uskat.s5.schedules.addons;

import org.toxsoft.uskat.s5.schedules.lib.*;
import org.toxsoft.uskat.s5.server.backend.addons.*;

import jakarta.ejb.*;

/**
 * Удаленный интерфейс службы {@link IBaSchedules}.
 *
 * @author mvk
 */
@Remote
public interface IS5SchedulesRemote
    extends IBaSchedules, IS5BackendAddonRemote {
  // nop
}
