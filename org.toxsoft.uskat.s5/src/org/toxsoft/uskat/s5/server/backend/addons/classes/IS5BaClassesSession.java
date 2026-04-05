package org.toxsoft.uskat.s5.server.backend.addons.classes;

import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.s5.server.backend.addons.*;

import jakarta.ejb.*;

/**
 * Сессия расширения backend {@link IBaClasses}
 *
 * @author mvk
 */
@Remote
public interface IS5BaClassesSession
    extends IBaClasses, IS5BackendAddonSession {
  // nop
}
