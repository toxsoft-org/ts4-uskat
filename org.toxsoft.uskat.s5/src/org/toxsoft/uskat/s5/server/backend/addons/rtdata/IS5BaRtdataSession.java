package org.toxsoft.uskat.s5.server.backend.addons.rtdata;

import javax.ejb.Remote;

import org.toxsoft.uskat.core.backend.api.IBaRtdata;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonSession;

/**
 * Сессия расширения backend {@link IBaRtdata}
 *
 * @author mvk
 */
@Remote
public interface IS5BaRtdataSession
    extends IBaRtdata, IS5BackendAddonSession {
  // nop
}
