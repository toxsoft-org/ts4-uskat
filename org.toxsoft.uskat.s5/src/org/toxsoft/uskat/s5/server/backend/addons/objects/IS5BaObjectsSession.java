package org.toxsoft.uskat.s5.server.backend.addons.objects;

import javax.ejb.Remote;

import org.toxsoft.uskat.core.backend.api.IBaObjects;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonSession;

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
