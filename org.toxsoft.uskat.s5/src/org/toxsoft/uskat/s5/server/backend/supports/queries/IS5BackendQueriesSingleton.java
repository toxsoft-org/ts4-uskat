package org.toxsoft.uskat.s5.server.backend.supports.queries;

import javax.ejb.Local;

import org.toxsoft.uskat.core.backend.api.IBaQueries;
import org.toxsoft.uskat.s5.server.backend.IS5BackendSupportSingleton;

/**
 * Локальный интерфейс синглетона запросов хранимых данных предоставляемый s5-сервером.
 *
 * @author mvk
 */
@Local
public interface IS5BackendQueriesSingleton
    extends IBaQueries, IS5BackendSupportSingleton {
  // nop
}
