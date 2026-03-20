package org.toxsoft.uskat.s5.schedules.supports;

import org.toxsoft.uskat.s5.schedules.lib.*;
import org.toxsoft.uskat.s5.server.backend.*;

import jakarta.ejb.*;

/**
 * Локальный интерфейс синглетона backend {@link IBaSchedules} предоставляемый s5-сервером.
 *
 * @author mvk
 */
@Local
public interface IS5BackendSchedulesSingleton
    extends IS5BackendSupportSingleton {
  // nop
}
