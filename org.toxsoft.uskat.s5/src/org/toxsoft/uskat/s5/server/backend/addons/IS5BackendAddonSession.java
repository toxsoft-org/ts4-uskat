package org.toxsoft.uskat.s5.server.backend.addons;

import javax.ejb.Remote;

import org.toxsoft.core.tslib.bricks.strid.IStridable;

/**
 * Сессия расширения бекенда предоставляемая s5-сервером для удаленного доступа {@link IS5BackendAddonRemote}.
 *
 * @author mvk
 */
@Remote
public interface IS5BackendAddonSession
    extends IStridable {
  // nop
}
