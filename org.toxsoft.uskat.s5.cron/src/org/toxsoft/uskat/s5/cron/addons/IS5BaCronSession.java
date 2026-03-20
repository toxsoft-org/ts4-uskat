package org.toxsoft.uskat.s5.cron.addons;

import org.toxsoft.uskat.s5.cron.lib.*;
import org.toxsoft.uskat.s5.server.backend.addons.*;

import jakarta.ejb.*;

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
