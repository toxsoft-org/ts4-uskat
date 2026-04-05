package org.toxsoft.uskat.s5.cron.supports;

import org.toxsoft.uskat.s5.cron.lib.*;
import org.toxsoft.uskat.s5.server.backend.*;

import jakarta.ejb.*;

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
