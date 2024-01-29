package org.toxsoft.uskat.core.gui.conn;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.core.tslib.bricks.ctx.impl.TsContextRefDef.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;

import org.toxsoft.core.tsgui.mws.services.timers.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.devapi.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * USkat-service: MWS-based GUI client side service to ensure {@link IDevCoreApi#doJobInCoreMainThread()} calls.
 * <p>
 * <h3>Usage:</h3> put the the following references in the argument of the connection
 * opening{@link ISkConnection#open(ITsContextRo)} TODO ???
 * <p>
 * to the {@link ITsGuiTimersService} in the opening arguments as the {@link #REFDEF_TSGUI_TIMER_SERVICE}
 * <p>
 * immediately after opening connection, add the service to the connection by calling
 * {@link ISkCoreApi#addService(ISkServiceCreator) coreApi.addService(SwtThreadSeparatorService.CREATOR)}.
 * <p>
 * TODO do we need to use this service automatically in {@link SkConnectionSupplier} ???
 *
 * @author mvk
 * @author hazard157
 */
public final class SkSwtThreadSeparatorService
    extends AbstractSkService {

  /**
   * Service creator singleton.
   */
  public static final ISkServiceCreator<AbstractSkService> CREATOR = SkSwtThreadSeparatorService::new;

  /**
   * The service ID.
   */
  public static final String SERVICE_ID = SK_SYSEXT_SERVICE_ID_PREFIX + "SwtThreadSeparator"; //$NON-NLS-1$

  /**
   * Mandatory context parameter: reference to the {@link ITsGuiTimersService}.
   * <p>
   * Type: {@link ITsGuiTimersService}
   */
  public static final ITsContextRefDef<ITsGuiTimersService> REFDEF_TSGUI_TIMER_SERVICE =
      create( SERVICE_ID + ".TsGuiTimersService", ITsGuiTimersService.class, //$NON-NLS-1$
          TSID_IS_MANDATORY, AV_TRUE, //
          TSID_IS_NULL_ALLOWED, AV_FALSE //
      );

  private ITsGuiTimersService timerService;

  /**
   * Constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  SkSwtThreadSeparatorService( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkService
  //

  @Override
  protected void doInit( ITsContextRo aArgs ) {
    timerService = REFDEF_TSGUI_TIMER_SERVICE.getRef( aArgs );
    timerService.quickTimers().addListener( rt -> coreApi().doJobInCoreMainThread() );
  }

  @Override
  protected void doClose() {
    // nop
  }

}
