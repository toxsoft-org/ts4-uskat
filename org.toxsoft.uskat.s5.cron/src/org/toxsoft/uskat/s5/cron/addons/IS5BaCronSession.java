package org.toxsoft.uskat.s5.cron.addons;

import javax.ejb.Remote;

import org.toxsoft.uskat.s5.cron.lib.IBaCrone;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonSession;

/**
 * Сессия службы {@link IBaCrone}.
 *
 * @author mvk
 */
@Remote
public interface IS5BaCronSession
    extends IBaCrone, IS5BackendAddonSession {
  // nop
}
