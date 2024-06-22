package org.toxsoft.uskat.s5.schedules.addons;

import javax.ejb.Remote;

import org.toxsoft.uskat.s5.schedules.lib.IBaSchedules;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonRemote;

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
