package org.toxsoft.uskat.s5.server.backend.addons.batch;

import javax.ejb.Local;

import org.toxsoft.uskat.s5.server.backend.IS5BackendAddonSession;

import ru.uskat.backend.addons.batchops.ISkBackendAddonBatchOperations;

/**
 * Сессия расширения backend {@link ISkBackendAddonBatchOperations}
 *
 * @author mvk
 */
@Local
public interface IS5BatchOperationsSession
    extends ISkBackendAddonBatchOperations, IS5BackendAddonSession {
  // nop
}
