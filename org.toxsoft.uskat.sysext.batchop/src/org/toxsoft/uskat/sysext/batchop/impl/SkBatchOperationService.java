package org.toxsoft.uskat.sysext.batchop.impl;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.filter.ITsCombiFilterParams;
import org.toxsoft.core.tslib.bricks.validator.IValResList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsUnsupportedFeatureRtException;
import org.toxsoft.uskat.sysext.batchop.ISkBatchOperationService;

import ru.uskat.backend.addons.batchops.ISkBackendAddonBatchOperations;
import ru.uskat.common.dpu.container.IDpuContainer;
import ru.uskat.common.dpu.container.IDpuIdContainer;
import ru.uskat.core.api.ISkServiceCreator;
import ru.uskat.core.devapi.IDevCoreApi;
import ru.uskat.core.impl.AbstractSkService;

/**
 * Реализация {@link ISkBatchOperationService}.
 *
 * @author goga
 */
public class SkBatchOperationService
    extends AbstractSkService
    implements ISkBatchOperationService {

  /**
   * Синглтон создателя сервиса.
   */
  public static final ISkServiceCreator<SkBatchOperationService> CREATOR = SkBatchOperationService::new;

  private ISkBackendAddonBatchOperations batchOpsBackend = null;

  /**
   * The constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - core API for service developers
   */
  public SkBatchOperationService( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );
  }

  // ------------------------------------------------------------------------------------
  // Реализация AbstractSkService
  //

  @Override
  protected void doInit( ITsContextRo aArgs ) {
    batchOpsBackend = coreApi().getBackendAddon( ISkBackendAddonBatchOperations.SK_BACKEND_ADDON_ID,
        ISkBackendAddonBatchOperations.class );
  }

  @Override
  protected void doClose() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса ISkBatchOperationService
  //

  @Override
  public boolean isAvailable() {
    return batchOpsBackend != null;
  }

  @Override
  public IValResList batchUpdate( IDpuIdContainer aToRemove, IDpuContainer aAddAndUpdate ) {
    TsNullArgumentRtException.checkNulls( aToRemove, aAddAndUpdate );
    TsUnsupportedFeatureRtException.checkNull( batchOpsBackend );
    return batchOpsBackend.batchUpdate( aToRemove, aAddAndUpdate );
  }

  @Override
  public IDpuContainer batchRead( IOptionSet aReadOptions, ITsCombiFilterParams aClassIdsFilter,
      ITsCombiFilterParams aDataTypeIdsFilter, ITsCombiFilterParams aClobIdsFilter ) {
    TsNullArgumentRtException.checkNulls( aReadOptions, aClassIdsFilter, aDataTypeIdsFilter, aClobIdsFilter );
    TsUnsupportedFeatureRtException.checkNull( batchOpsBackend );
    return batchOpsBackend.batchRead( aReadOptions, aClassIdsFilter, aDataTypeIdsFilter, aClobIdsFilter );
  }

}
