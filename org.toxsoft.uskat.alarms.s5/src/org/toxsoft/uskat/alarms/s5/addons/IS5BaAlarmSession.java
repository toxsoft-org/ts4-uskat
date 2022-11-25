package org.toxsoft.uskat.alarms.s5.addons;

import javax.ejb.Remote;

import org.toxsoft.uskat.alarms.lib.IBaAlarms;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonSession;

/**
 * Сессия расширения backend {@link IBaAlarms}
 *
 * @author mvk
 */
@Remote
public interface IS5BaAlarmSession
    extends IBaAlarms, IS5BackendAddonSession {
  // nop
}
