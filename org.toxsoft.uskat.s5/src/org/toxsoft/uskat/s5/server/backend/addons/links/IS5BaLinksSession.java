package org.toxsoft.uskat.s5.server.backend.addons.links;

import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.s5.server.backend.addons.*;

import jakarta.ejb.*;

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
