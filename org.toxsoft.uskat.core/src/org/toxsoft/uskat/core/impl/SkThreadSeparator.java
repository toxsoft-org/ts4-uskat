package org.toxsoft.uskat.core.impl;

import java.util.Timer;
import java.util.TimerTask;

import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.connection.ISkConnection;
import org.toxsoft.uskat.core.devapi.IDevCoreApi;

/**
 * Thread separation service.
 * <p>
 * Solves the tasks of sharing data access between client threads and uskat.
 *
 * @author mvk
 */
public final class SkThreadSeparator
    extends SkAbstractThreadSeparator {

  /**
   * Constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  protected SkThreadSeparator( IDevCoreApi aCoreApi ) {
    super( aCoreApi );
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkService
  //

  /**
   * Initialize separator. The descendant must initialize its state.
   *
   * @param aArgs {@link ITsContextRo} - connection opening arguments from {@link ISkConnection#open(ITsContextRo)}
   */
  @Override
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
  @Override
  public void asyncExec( Runnable aRunnable ) {
    // TODO:
  }

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
  @Override
  public void syncExec( Runnable aRunnable ) {
    // TODO:
  }

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
  @Override
  public void timerExec( int aMilliseconds, Runnable aRunnable ) {
    Timer timer = new Timer();
    TimerTask task = new TimerTask() {

      @Override
      public void run() {
        asyncExec( aRunnable );
      }
    };
    timer.schedule( task, aMilliseconds, aMilliseconds );
  }
}
