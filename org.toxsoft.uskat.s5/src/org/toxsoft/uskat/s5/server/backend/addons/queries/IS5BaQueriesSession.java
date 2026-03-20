package org.toxsoft.uskat.s5.server.backend.addons.queries;

import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.s5.server.backend.addons.*;

import jakarta.ejb.*;

/**
 * Сессия расширения backend {@link IBaQueries}
 *
 * @author mvk
 */
@Remote
public interface IS5BaQueriesSession
    extends IBaQueries, IS5BackendAddonSession {
  // nop
}
