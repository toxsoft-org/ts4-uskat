package org.toxsoft.uskat.s5.server.backend.addons.realtime;

import javax.ejb.Remote;

import org.toxsoft.uskat.s5.server.backend.IS5BackendAddonRemote;

import ru.uskat.backend.addons.realtime.ISkBackendAddonRealtime;

/**
 * Удаленный доступ к расширению backend {@link ISkBackendAddonRealtime}
 *
 * @author mvk
 */
@Remote
public interface IS5RealtimeRemote
    extends ISkBackendAddonRealtime, IS5BackendAddonRemote {
  // nop
}
