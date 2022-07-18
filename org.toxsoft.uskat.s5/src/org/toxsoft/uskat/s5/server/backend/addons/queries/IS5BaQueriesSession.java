package org.toxsoft.uskat.s5.server.backend.addons.queries;

import javax.ejb.Remote;

import org.toxsoft.uskat.core.backend.api.IBaQueries;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonSession;

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
