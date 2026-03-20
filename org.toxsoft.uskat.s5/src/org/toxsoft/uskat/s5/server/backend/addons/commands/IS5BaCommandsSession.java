package org.toxsoft.uskat.s5.server.backend.addons.commands;

import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.s5.server.backend.addons.*;

import jakarta.ejb.*;

/**
 * Сессия расширения backend {@link IBaCommands}
 *
 * @author mvk
 */
@Remote
public interface IS5BaCommandsSession
    extends IBaCommands, IS5BackendAddonSession {
  // nop
}
