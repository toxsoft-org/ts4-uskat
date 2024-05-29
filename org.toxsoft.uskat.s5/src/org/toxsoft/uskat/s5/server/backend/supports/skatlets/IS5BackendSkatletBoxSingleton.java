package org.toxsoft.uskat.s5.server.backend.supports.skatlets;

import javax.ejb.Local;

import org.toxsoft.uskat.core.devapi.ISkatlet;
import org.toxsoft.uskat.s5.server.backend.IS5BackendSupportSingleton;

/**
 * Локальный интерфейс синглетона поддержки скатлетов ({@link ISkatlet}) предоставляемый s5-сервером.
 *
 * @author mvk
 */
@Local
public interface IS5BackendSkatletBoxSingleton
    extends IS5BackendSupportSingleton {

  // nop
}
