package org.toxsoft.uskat.s5.server.backend.supports.rtdata;

import javax.ejb.Local;

import org.toxsoft.uskat.core.backend.api.IBaRtdata;
import org.toxsoft.uskat.s5.server.backend.IS5BackendSupportSingleton;

/**
 * Локальный интерфейс синглетона запросов к данным реального времени предоставляемый s5-сервером.
 *
 * @author mvk
 */
@Local
public interface IS5BackendRtdataSingleton
    extends IBaRtdata, IS5BackendSupportSingleton {
  // nop
}
