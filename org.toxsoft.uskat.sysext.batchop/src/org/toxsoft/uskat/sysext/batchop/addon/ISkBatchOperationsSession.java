package org.toxsoft.uskat.sysext.batchop.addon;

import javax.ejb.Local;

import org.toxsoft.uskat.s5.server.backend.IS5BackendAddonSession;

import ru.uskat.backend.addons.batchops.ISkBackendAddonBatchOperations;

/**
 * Сессия расширения backend {@link ISkBackendAddonBatchOperations}
 *
 * @author mvk
 */
@Local
public interface ISkBatchOperationsSession
    extends ISkBackendAddonBatchOperations, IS5BackendAddonSession {
  // nop
}
