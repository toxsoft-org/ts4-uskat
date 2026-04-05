package org.toxsoft.uskat.s5.server.backend.addons.gwiddb;

import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.s5.server.backend.addons.*;

import jakarta.ejb.*;

/**
 * Сессия расширения backend {@link IBaGwidDb}
 *
 * @author mvk
 */
@Remote
public interface IS5BaGwidDbSession
    extends IBaGwidDb, IS5BackendAddonSession {
  // nop
}
