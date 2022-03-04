package org.toxsoft.uskat.s5.client.remote.addons;

import static org.toxsoft.uskat.s5.client.remote.addons.IS5Resources.*;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.filter.ITsCombiFilterParams;
import org.toxsoft.core.tslib.bricks.validator.IValResList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.client.remote.S5BackendAddonRemote;
import org.toxsoft.uskat.s5.server.backend.addons.batch.IS5BatchOperationsRemote;

import ru.uskat.backend.addons.batchops.ISkBackendAddonBatchOperations;
import ru.uskat.common.dpu.container.*;

/**
 * Реализация удаленного доступа к расширению backend {@link ISkBackendAddonBatchOperations} предоставляемое s5-сервером
 *
 * @author mvk
 */
public final class S5BatchOperationsRemote
    extends S5BackendAddonRemote<IS5BatchOperationsRemote>
    implements ISkBackendAddonBatchOperations {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор
   */
  public S5BatchOperationsRemote() {
    super( ISkBackendAddonBatchOperations.SK_BACKEND_ADDON_ID, //
        STR_D_BACKEND_ADDON_BATCH_OPERATIONS, //
        IS5BatchOperationsRemote.class );
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
