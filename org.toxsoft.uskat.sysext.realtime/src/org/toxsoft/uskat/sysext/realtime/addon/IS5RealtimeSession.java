package org.toxsoft.uskat.sysext.realtime.addon;

import javax.ejb.Local;

import org.toxsoft.uskat.s5.server.backend.IS5BackendAddonSession;

import ru.uskat.backend.addons.realtime.ISkBackendAddonRealtime;

/**
 * Сессия расширения backend {@link ISkBackendAddonRealtime}
 *
 * @author mvk
 */
@Local
public interface IS5RealtimeSession
    extends ISkBackendAddonRealtime, IS5BackendAddonSession {
  // nop
}
