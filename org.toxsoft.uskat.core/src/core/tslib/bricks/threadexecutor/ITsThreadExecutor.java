package core.tslib.bricks.threadexecutor;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Еxecutor of API calls in one thread.
 *
 * @author mvk
 */
public interface ITsThreadExecutor
    extends ICooperativeMultiTaskable {

  /**
   * Returns the thread in which the functions of API are called.
   * <p>
   * Warning: method {@link #doJob()} must be called from this thread!
   *
   * @return {@link Thread} - the execution thread
   */
  Thread thread();

  /**
   * Causes the <code>run()</code> method of the runnable to be invoked by {@link #thread()} at the next reasonable
   * opportunity. The caller of this method continues to run in parallel, and is not notified when the runnable has
   * completed.
   *
   * @param aRunnable code to run on the uskat thread
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  void asyncExec( Runnable aRunnable );

  /**
   * Causes the <code>run()</code> method of the runnable to be invoked by {@link #thread()} at the next reasonable
   * opportunity. The thread which calls this method is suspended until the runnable completes.
   *
   * @param aRunnable code to run on the uskat thread
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  void syncExec( Runnable aRunnable );

  /**
   * Causes the <code>run()</code> method of the runnable to be invoked by the {@link #thread()} after the specified
   * number of milliseconds have elapsed. If milliseconds is less than zero, the runnable is not executed.
   *
   * @param aMilliseconds the delay before running the runnable
   * @param aRunnable code to run on the user-interface thread
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  void timerExec( int aMilliseconds, Runnable aRunnable );
}
