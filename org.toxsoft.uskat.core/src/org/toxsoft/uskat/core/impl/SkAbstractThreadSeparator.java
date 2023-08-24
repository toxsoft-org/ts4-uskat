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
import org.toxsoft.uskat.core.connection.ISkConnection;
import org.toxsoft.uskat.core.devapi.IDevCoreApi;

/**
 * Abstract implementation of thread separation service.
 * <p>
 * Solves the tasks of sharing data access between client threads and uskat.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public abstract class SkAbstractThreadSeparator
    extends AbstractSkService {

  /**
   * The service ID.
   */
  public static final String SERVICE_ID = "SkThreadSeparator"; //$NON-NLS-1$

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

  private final IDevCoreApi devCoreApi;
  private int               doJobTimeout;

  /**
   * Constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  protected SkAbstractThreadSeparator( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );
    devCoreApi = aCoreApi;
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkService
  //
  @Override
  final protected void doInit( ITsContextRo aArgs ) {
    doJobTimeout = OP_DOJOB_TIMEOUT.getValue( aArgs.params() ).asInt();
    Thread thread = ISkCoreConfigConstants.REFDEF_API_THREAD.getRef( aArgs, null );
    if( thread == null ) {
      throw new TsIllegalArgumentRtException( ERR_API_THREAD_UNDEF );
    }
    doInitSeparator( aArgs );

    // run seporator task
    asyncExec( new Runnable() {

      @Override
      public void run() {
        devCoreApi.doJobInCoreMainThread();
        timerExec( doJobTimeout, this );
      }
    } );
  }

  @Override
  final protected void doClose() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // To override
  //

  /**
   * Initialize separator. The descendant must initialize its state.
   *
   * @param aArgs {@link ITsContextRo} - connection opening arguments from {@link ISkConnection#open(ITsContextRo)}
   */
  protected void doInitSeparator( ITsContextRo aArgs ) {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // To implement
  //

  /**
   * Causes the <code>run()</code> method of the runnable to be invoked by the uskat thread (
   * {@link ISkCoreConfigConstants#REFDEF_API_THREAD}) at the next reasonable opportunity. The caller of this method
   * continues to run in parallel, and is not notified when the runnable has completed.
   *
   * @param aRunnable code to run on the uskat thread
   * @throws TsNullArgumentRtException argument = null
   * @see ISkCoreConfigConstants#REFDEF_API_THREAD
   * @see #syncExec
   */
  public abstract void asyncExec( Runnable aRunnable );

  /**
   * Causes the <code>run()</code> method of the runnable to be invoked by the uskat thread (
   * {@link ISkCoreConfigConstants#REFDEF_API_THREAD}) at the next reasonable opportunity. The thread which calls this
   * method is suspended until the runnable completes.
   *
   * @param aRunnable code to run on the uskat thread
   * @throws TsNullArgumentRtException argument = null
   * @see ISkCoreConfigConstants#REFDEF_API_THREAD
   * @see #asyncExec
   */
  public abstract void syncExec( Runnable aRunnable );

  /**
   * Causes the <code>run()</code> method of the runnable to be invoked by the uskat thread (
   * {@link ISkCoreConfigConstants#REFDEF_API_THREAD}) after the specified number of milliseconds have elapsed. If
   * milliseconds is less than zero, the runnable is not executed.
   *
   * @param aMilliseconds the delay before running the runnable
   * @param aRunnable code to run on the uskat thread
   * @throws TsNullArgumentRtException argument = null
   * @see ISkCoreConfigConstants#REFDEF_API_THREAD
   * @see #asyncExec
   */
  public abstract void timerExec( int aMilliseconds, Runnable aRunnable );

}
