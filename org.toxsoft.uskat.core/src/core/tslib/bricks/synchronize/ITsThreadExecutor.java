package core.tslib.bricks.synchronize;

import org.toxsoft.core.tslib.bricks.ICooperativeMultiTaskable;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Еxecutor of API calls in one thread.
 *
 * @author mvk
 */
public interface ITsThreadExecutor
    extends ICooperativeMultiTaskable {

  /**
   * Returns the thread in which the functions of API are called
   *
   * @return {@link Thread} thread
   */
  Thread thread();

  /**
   * Causes the <code>run()</code> method of the runnable to be invoked by {@link #thread()} at the next reasonable
   * opportunity. The caller of this method continues to run in parallel, and is not notified when the runnable has
   * completed.
   *
   * @param aRunnable code to run on the uskat thread
   * @throws TsNullArgumentRtException argument = null
   */
  void asyncExec( Runnable aRunnable );

  /**
   * Causes the <code>run()</code> method of the runnable to be invoked by {@link #thread()} at the next reasonable
   * opportunity. The thread which calls this method is suspended until the runnable completes.
   *
   * @param aRunnable code to run on the uskat thread
   * @throws TsNullArgumentRtException argument = null
   */
  void syncExec( Runnable aRunnable );

  /**
   * Causes the <code>run()</code> method of the runnable to be invoked by the user-interface thread after the specified
   * number of milliseconds have elapsed. If milliseconds is less than zero, the runnable is not executed.
   * <p>
   * Note that at the time the runnable is invoked, widgets that have the receiver as their display may have been
   * disposed. Therefore, it is necessary to check for this case inside the runnable before accessing the widget.
   * </p>
   *
   * @param aMilliseconds the delay before running the runnable
   * @param aRunnable code to run on the user-interface thread
   * @throws TsNullArgumentRtException argument = null
   */
  void timerExec( int aMilliseconds, Runnable aRunnable );
}
