package org.toxsoft.uskat.s5.server.backend.addons.commands;

import javax.ejb.Remote;

import org.toxsoft.uskat.core.backend.api.IBaCommands;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonSession;

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
