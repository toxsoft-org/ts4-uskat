package org.toxsoft.uskat.s5.server.backend.addons.objects;

import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.s5.server.backend.addons.*;

import jakarta.ejb.*;

/**
 * Сессия расширения backend {@link IBaObjects}
 *
 * @author mvk
 */
@Remote
public interface IS5BaObjectsSession
    extends IBaObjects, IS5BackendAddonSession {
  // nop
}
