package org.toxsoft.uskat.s5.cron.addons;

import javax.ejb.Remote;

import org.toxsoft.uskat.s5.cron.lib.IBaCrone;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonRemote;

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
