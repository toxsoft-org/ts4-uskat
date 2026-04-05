package org.toxsoft.uskat.s5.cron.addons;

import org.toxsoft.uskat.s5.cron.lib.*;
import org.toxsoft.uskat.s5.server.backend.addons.*;

import jakarta.ejb.*;

/**
 * Удаленный интерфейс службы {@link IBaCrone}.
 *
 * @author mvk
 */
@Remote
public interface IS5CronRemote
    extends IBaCrone, IS5BackendAddonRemote {
  // nop
}
