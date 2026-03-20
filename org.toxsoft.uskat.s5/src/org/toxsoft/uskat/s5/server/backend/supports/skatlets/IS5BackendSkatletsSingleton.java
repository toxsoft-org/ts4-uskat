package org.toxsoft.uskat.s5.server.backend.supports.skatlets;

import org.toxsoft.uskat.core.devapi.*;
import org.toxsoft.uskat.s5.server.backend.*;

import jakarta.ejb.*;

/**
 * Локальный интерфейс синглетона поддержки скатлетов ({@link ISkatlet}) предоставляемый s5-сервером.
 *
 * @author mvk
 */
@Local
public interface IS5BackendSkatletsSingleton
    extends IS5BackendSupportSingleton {

  // nop
}
