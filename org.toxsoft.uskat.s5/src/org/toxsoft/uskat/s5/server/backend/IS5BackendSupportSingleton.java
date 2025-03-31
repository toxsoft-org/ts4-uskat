package org.toxsoft.uskat.s5.server.backend;

import javax.ejb.*;

import org.toxsoft.uskat.s5.server.singletons.*;

/**
 * Локальный интерфейс синглетона поддержки бекенда предоставляемого s5-сервером.
 *
 * @author mvk
 */
@Local
public interface IS5BackendSupportSingleton
    extends IS5Singleton {
  // nop
}
