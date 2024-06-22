package org.toxsoft.uskat.s5.schedules.supports;

import javax.ejb.Local;

import org.toxsoft.uskat.s5.schedules.lib.IBaSchedules;
import org.toxsoft.uskat.s5.server.backend.IS5BackendSupportSingleton;

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
