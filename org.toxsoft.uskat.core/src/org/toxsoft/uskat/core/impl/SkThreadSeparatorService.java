package org.toxsoft.uskat.core.impl;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.impl.AvUtils;
import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.ISkServiceCreator;
import org.toxsoft.uskat.core.devapi.IDevCoreApi;

/**
 * Implementation of thread separation service.
 * <p>
 * Solves the tasks of sharing data access between client threads and uskat.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public class SkThreadSeparatorService
    extends AbstractSkService
    implements ITsThreadSynchronizer {

  /**
   * The service ID.
   */
  public static final String SERVICE_ID = "SkThreadSeparatorService"; //$NON-NLS-1$

  /**
   * Service creator singleton.
   */
  public static final ISkServiceCreator<AbstractSkService> CREATOR = SkThreadSeparatorService::new;

  /**
   * Mandotary context parameter: dojob timeout (msec)
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  public static final IDataDef OP_DOJOB_TIMEOUT = create( SERVICE_ID + ".DoJobTimeout", INTEGER, //$NON-NLS-1$
      TSID_NAME, "Timeout", // N_DOJOB_TIMEOUT, //
      TSID_DESCRIPTION, "Dojob timeout (msec)", // D_DOJOB_TIMEOUT, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AvUtils.avInt( 100 ) );

  private final IDevCoreApi     devCoreApi;
  private ITsThreadSynchronizer synchronizer;
  private int                   doJobTimeout;

  /**
   * Constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  protected SkThreadSeparatorService( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );
    devCoreApi = aCoreApi;
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkService
  //
  @Override
  final protected void doInit( ITsContextRo aArgs ) {
    doJobTimeout = OP_DOJOB_TIMEOUT.getValue( aArgs.params() ).asInt();
    synchronizer = ISkCoreConfigConstants.REFDEF_THREAD_SYNCHRONIZER.getRef( aArgs, null );
    if( synchronizer == null ) {
      throw new TsIllegalArgumentRtException( ERR_API_THREAD_UNDEF );
    }
    // run seporator task
    asyncExec( new Runnable() {

      @Override
      public void run() {
        devCoreApi.doJobInCoreMainThread();
        synchronizer.timerExec( doJobTimeout, this );
      }
    } );
  }

  @Override
  final protected void doClose() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // ITsThreadSynchronizer
  //
  @Override
  public Thread thread() {
    return synchronizer.thread();
  }

  @Override
  public void asyncExec( Runnable aRunnable ) {
    TsNullArgumentRtException.checkNull( aRunnable );
    synchronizer.asyncExec( aRunnable );
  }

  @Override
  public void syncExec( Runnable aRunnable ) {
    TsNullArgumentRtException.checkNull( aRunnable );
    synchronizer.syncExec( aRunnable );
  }

  @Override
  public void timerExec( int aMilliseconds, Runnable aRunnable ) {
    TsNullArgumentRtException.checkNull( aRunnable );
    synchronizer.timerExec( aMilliseconds, aRunnable );
  }

}
