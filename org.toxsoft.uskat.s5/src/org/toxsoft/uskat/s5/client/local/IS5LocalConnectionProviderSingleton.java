package org.toxsoft.uskat.s5.client.local;

import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.connection.*;

import jakarta.ejb.*;

/**
 * Поставщик соединений {@link ISkConnection} с локальным сервером.
 * <p>
 * Вспомогательный интерфейс для аннотирования {@link ISkBackendProvider} как локального интерфейса
 *
 * @author mvk
 */
@Local
public interface IS5LocalConnectionProviderSingleton
    extends ISkBackendProvider {
  // nop
}
