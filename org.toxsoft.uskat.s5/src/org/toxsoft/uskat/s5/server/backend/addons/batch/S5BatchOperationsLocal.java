package org.toxsoft.uskat.s5.server.backend.addons.batch;

import static org.toxsoft.uskat.s5.server.backend.addons.batch.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.links.S5BackendLinksSingleton.*;
import static org.toxsoft.uskat.s5.server.backend.supports.lobs.S5BackendLobsSingleton.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.S5BackendObjectsSingleton.*;
import static org.toxsoft.uskat.s5.server.backend.supports.sysdescr.S5BackendSysDescrSingleton.*;

import javax.ejb.EJB;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.filter.ITsCombiFilterParams;
import org.toxsoft.core.tslib.bricks.validator.IValResList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.client.local.S5LocalBackend;
import org.toxsoft.uskat.s5.server.backend.IS5BackendCoreSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.links.IS5BackendLinksSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.lobs.IS5BackendLobsSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.objects.IS5BackendObjectsSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.IS5BackendSysDescrSingleton;
import org.toxsoft.uskat.s5.server.sessions.S5BackendAddonLocal;

import ru.uskat.backend.addons.batchops.ISkBackendAddonBatchOperations;
import ru.uskat.common.dpu.container.IDpuContainer;
import ru.uskat.common.dpu.container.IDpuIdContainer;
import ru.uskat.core.common.helpers.batchop.SkBatchOperationsSupport;

/**
 * Реализация локального доступа к расширению backend {@link ISkBackendAddonBatchOperations}.
 *
 * @author mvk
 */
public final class S5BatchOperationsLocal
    extends S5BackendAddonLocal
    implements ISkBackendAddonBatchOperations {

  private static final long serialVersionUID = 157157L;

  /**
   * backend системного описания
   */
  @EJB
  private IS5BackendSysDescrSingleton sysdescrBackend;

  /**
   * backend управления объектами
   */
  @EJB
  private IS5BackendObjectsSingleton objectsBackend;

  /**
   * backend управления связями между объектами
   */
  @EJB
  private IS5BackendLinksSingleton linksBackend;

  /**
   * backend управления большими объектами объектами (Large OBject - LOB) системы
   */
  @EJB
  private IS5BackendLobsSingleton lobsBackend;

  /**
   * Пустой конструктор.
   */
  public S5BatchOperationsLocal() {
    super( SK_BACKEND_ADDON_ID, STR_N_BACKEND_BATCH );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов
  //
  @Override
  public void doAfterInit( S5LocalBackend aOwner, IS5BackendCoreSingleton aBackend ) {
    sysdescrBackend = aBackend.get( BACKEND_SYSDESCR_ID, IS5BackendSysDescrSingleton.class );
    objectsBackend = aBackend.get( BACKEND_OBJECTS_ID, IS5BackendObjectsSingleton.class );
    linksBackend = aBackend.get( BACKEND_LINKS_ID, IS5BackendLinksSingleton.class );
    lobsBackend = aBackend.get( BACKEND_LOBS_ID, IS5BackendLobsSingleton.class );
  }

  // ------------------------------------------------------------------------------------
  // Реализация ISkBackendAddonBatchOperations
  //
  @Override
  public IValResList batchUpdate( IDpuIdContainer aToRemove, IDpuContainer aAddAndUpdate ) {
    TsNullArgumentRtException.checkNulls( aToRemove, aAddAndUpdate );
    // Выполнение запроса
    IValResList retValue = SkBatchOperationsSupport.batchUpdate( backend(), aToRemove, aAddAndUpdate );
    if( retValue.results().size() > 0 ) {
      logger().info( retValue.results().first().message() );
    }
    return retValue;
  }

  @Override
  public IDpuContainer batchRead( IOptionSet aReadOptions, ITsCombiFilterParams aClassIdsFilter,
      ITsCombiFilterParams aDataTypeIdsFilter, ITsCombiFilterParams aClobIdsFilter ) {
    TsNullArgumentRtException.checkNulls( aReadOptions, aClassIdsFilter, aDataTypeIdsFilter, aClobIdsFilter );
    return SkBatchOperationsSupport.batchRead( sysdescrBackend.getReader(), backend(), aReadOptions, aClassIdsFilter,
        aDataTypeIdsFilter, aClobIdsFilter );
  }
}
