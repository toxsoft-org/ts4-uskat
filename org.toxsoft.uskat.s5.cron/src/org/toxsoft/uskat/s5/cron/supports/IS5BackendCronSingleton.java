package org.toxsoft.uskat.s5.cron.supports;

import javax.ejb.Local;

import org.toxsoft.uskat.s5.cron.lib.IBaCrone;
import org.toxsoft.uskat.s5.server.backend.IS5BackendSupportSingleton;

/**
 * Локальный интерфейс синглетона backend {@link IBaCrone} предоставляемый s5-сервером.
 *
 * @author mvk
 */
@Local
public interface IS5BackendCronSingleton
    extends IS5BackendSupportSingleton {
  // nop
}
