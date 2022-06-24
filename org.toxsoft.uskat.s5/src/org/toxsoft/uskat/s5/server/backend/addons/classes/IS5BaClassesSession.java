package org.toxsoft.uskat.s5.server.backend.addons.classes;

import javax.ejb.Remote;

import org.toxsoft.uskat.core.backend.api.IBaClasses;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonSession;

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
