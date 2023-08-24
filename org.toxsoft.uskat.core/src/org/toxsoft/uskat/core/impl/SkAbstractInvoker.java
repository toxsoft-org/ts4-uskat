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
 * Abstract implementation of invoker.
 * <p>
 * Solves the tasks of sharing data access between client threads and uskat.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public abstract class SkAbstractInvoker {

  /**
   * Constructor.
   *
   */
  protected SkAbstractInvoker( ) {
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
