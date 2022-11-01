package org.toxsoft.uskat.s5.server.backend.addons.clobs;

import javax.ejb.Remote;

import org.toxsoft.uskat.core.backend.api.IBaClobs;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonSession;

/**
 * Сессия расширения backend {@link IBaClobs}
 *
 * @author mvk
 */
@Remote
public interface IS5BaClobsSession
    extends IBaClobs, IS5BackendAddonSession {
  // nop
}
