package org.toxsoft.uskat.s5.server.backend;

import javax.ejb.Local;

import org.toxsoft.uskat.s5.server.singletons.IS5Singleton;

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
