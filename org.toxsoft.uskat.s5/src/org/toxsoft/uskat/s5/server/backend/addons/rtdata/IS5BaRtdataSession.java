package org.toxsoft.uskat.s5.server.backend.addons.rtdata;

import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.s5.server.backend.addons.*;

import jakarta.ejb.*;

/**
 * Сессия расширения backend {@link IBaRtdata}
 *
 * @author mvk
 */
@Remote
public interface IS5BaRtdataSession
    extends IBaRtdata, IS5BackendAddonSession {
  // nop
}
