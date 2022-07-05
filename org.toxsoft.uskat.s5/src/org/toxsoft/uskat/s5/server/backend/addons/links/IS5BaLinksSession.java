package org.toxsoft.uskat.s5.server.backend.addons.links;

import javax.ejb.Remote;

import org.toxsoft.uskat.core.backend.api.IBaLinks;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonSession;

/**
 * Сессия расширения backend {@link IBaLinks}
 *
 * @author mvk
 */
@Remote
public interface IS5BaLinksSession
    extends IBaLinks, IS5BackendAddonSession {
  // nop
}
