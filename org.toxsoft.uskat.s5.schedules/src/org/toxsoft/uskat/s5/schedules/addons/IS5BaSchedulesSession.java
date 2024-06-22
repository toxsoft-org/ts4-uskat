package org.toxsoft.uskat.s5.schedules.addons;

import javax.ejb.Remote;

import org.toxsoft.uskat.s5.schedules.lib.IBaSchedules;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonSession;

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
