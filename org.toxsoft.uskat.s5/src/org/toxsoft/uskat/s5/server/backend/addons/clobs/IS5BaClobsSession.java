package org.toxsoft.uskat.s5.server.backend.addons.clobs;

import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.s5.server.backend.addons.*;

import jakarta.ejb.*;

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
