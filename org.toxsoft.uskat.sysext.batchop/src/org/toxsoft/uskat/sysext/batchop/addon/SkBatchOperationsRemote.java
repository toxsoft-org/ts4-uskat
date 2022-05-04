package org.toxsoft.uskat.sysext.batchop.addon;

import static org.toxsoft.uskat.sysext.batchop.addon.ISkResources.*;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.filter.ITsCombiFilterParams;
import org.toxsoft.core.tslib.bricks.validator.IValResList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.client.remote.S5BackendAddonRemote;

import ru.uskat.backend.addons.batchops.ISkBackendAddonBatchOperations;
import ru.uskat.common.dpu.container.*;

/**
 * Реализация удаленного доступа к расширению backend {@link ISkBackendAddonBatchOperations} предоставляемое s5-сервером
 *
 * @author mvk
 */
public final class SkBatchOperationsRemote
    extends S5BackendAddonRemote<ISkBatchOperationsRemote>
    implements ISkBackendAddonBatchOperations {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор
   */
  public SkBatchOperationsRemote() {
    super( ISkBackendAddonBatchOperations.SK_BACKEND_ADDON_ID, //
        STR_D_BACKEND_ADDON_BATCH_OPERATIONS, //
        ISkBatchOperationsRemote.class );
  }

  // ------------------------------------------------------------------------------------
  // Реализация ISkBackendAddonBatchOperations
  //
  @Override
  public IValResList batchUpdate( IDpuIdContainer aToRemove, IDpuContainer aAddAndUpdate ) {
    TsNullArgumentRtException.checkNulls( aToRemove, aAddAndUpdate );
    String addAndUpdateStr = DpuContainerKeeper.KEEPER.ent2str( aAddAndUpdate );
    return remote().batchUpdate( aToRemove, addAndUpdateStr );
  }

  @Override
  public IDpuContainer batchRead( IOptionSet aReadOptions, ITsCombiFilterParams aClassIdsFilter,
      ITsCombiFilterParams aDataTypeIdsFilter, ITsCombiFilterParams aClobIdsFilter ) {
    TsNullArgumentRtException.checkNulls( aReadOptions, aClassIdsFilter, aDataTypeIdsFilter, aClassIdsFilter );
    return remote().batchRead( aReadOptions, aClassIdsFilter, aDataTypeIdsFilter, aClobIdsFilter );
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
}
