package org.toxsoft.uskat.s5.server.backend.addons.gwiddb;

import javax.ejb.Remote;

import org.toxsoft.uskat.core.backend.api.IBaGwidDb;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonSession;

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
