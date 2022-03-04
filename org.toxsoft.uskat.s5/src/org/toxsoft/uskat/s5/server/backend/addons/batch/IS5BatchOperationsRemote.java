package org.toxsoft.uskat.s5.server.backend.addons.batch;

import javax.ejb.Remote;

import org.toxsoft.core.tslib.bricks.validator.IValResList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.IS5BackendAddonRemote;

import ru.uskat.backend.addons.batchops.ISkBackendAddonBatchOperations;
import ru.uskat.common.dpu.container.*;

/**
 * Удаленный доступ к расширению backend {@link ISkBackendAddonBatchOperations}
 *
 * @author mvk
 */
@Remote
public interface IS5BatchOperationsRemote
    extends ISkBackendAddonBatchOperations, IS5BackendAddonRemote {

  /**
   * Осуществляет пакетное обновление содержимого.
   * <p>
   * TODO: Необходимость метода обусловлена особенностями/ошибками реализации сериализации {@link IDpuContainer}. Когда
   * вопрос будет закрыт метод можно удалить и вместо него использовать вызов
   * {@link #batchUpdate(IDpuIdContainer, IDpuContainer)}.
   *
   * @param aToRemove {@link IDpuIdContainer} - сущности, которые должны быть удалены
   * @param aAddAndUpdate String - {@link IDpuContainer} в формате {@link DpuContainerKeeper}.
   * @return {@link IValResList} - результат выполнения, может содержать множество ошибок и предупреждении
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  IValResList batchUpdate( IDpuIdContainer aToRemove, String aAddAndUpdate );
}
