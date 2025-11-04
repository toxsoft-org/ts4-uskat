package org.toxsoft.uskat.core.devapi;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.core.tslib.bricks.ctx.impl.TsContextRefDef.*;
import static org.toxsoft.uskat.core.devapi.ISkResources.*;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.*;

/**
 * Константы подсистемы.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public interface ISkWorkerHardConstants {

  /**
   * General full ID prefix (IDpath).
   */
  String SITROL_WORKERS_FULL_ID = ISkHardConstants.USKAT_FULL_ID + ".workers";

  /**
   * Параметр контекста компонента {@link ISkWorker#setContext(ITsContextRo)}: API сервера.
   * <p>
   * Тип: {@link ISkCoreApi}
   */
  ITsContextRefDef<ISkCoreApi> REFDEF_WORKER_CORE_API = create( SITROL_WORKERS_FULL_ID + ".api", ISkCoreApi.class, //$NON-NLS-1$
      TSID_NAME, STR_N_WORKER_CORE_API, //
      TSID_DESCRIPTION, STR_D_WORKER_CORE_API, //
      TSID_IS_NULL_ALLOWED, AV_TRUE );

  /**
   * Параметр контекста компонента {@link ISkWorker#setContext(ITsContextRo)}: реестр доступных компонентов.
   * <p>
   * Тип: {@link IStringMap}&lt;{@link ISkWorker}&gt;
   */
  ITsContextRefDef<SkWorkerRegistry> REFDEF_WORKER_REGISTRY =
      create( SITROL_WORKERS_FULL_ID + ".registry", SkWorkerRegistry.class, //$NON-NLS-1$
          TSID_NAME, STR_N_WORKER_REGISTRY, //
          TSID_DESCRIPTION, STR_D_WORKER_REGISTRY, //
          TSID_IS_NULL_ALLOWED, AV_TRUE );

  /**
   * Параметр контекста компонента {@link ISkWorker#setContext(ITsContextRo)}: общий контекст между компонентами.
   * <p>
   * Тип: {@link ITsContext}
   */
  ITsContextRefDef<ITsContext> REFDEF_WORKER_SHARED_CONTEXT =
      create( SITROL_WORKERS_FULL_ID + ".SharedContext", ITsContext.class, //$NON-NLS-1$
          TSID_NAME, STR_N_WORKER_CORE_API, //
          TSID_DESCRIPTION, STR_D_WORKER_CORE_API, //
          TSID_IS_NULL_ALLOWED, AV_TRUE );

  /**
   * Параметр контекста компонента {@link ISkWorker#setContext(ITsContextRo)}: журнал работы компнента.
   * <p>
   * Тип: {@link ILogger}
   */
  ITsContextRefDef<ILogger> REFDEF_WORKER_LOGGER = create( SITROL_WORKERS_FULL_ID + ".logger", ILogger.class, //$NON-NLS-1$
      TSID_NAME, STR_N_WORKER_LOGGER, //
      TSID_DESCRIPTION, STR_D_WORKER_LOGGER, //
      TSID_IS_NULL_ALLOWED, AV_TRUE );
}
