package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Invoker solves the tasks of sharing data access between client threads and uskat by one thread.
 *
 * @author mvk
 */
public interface ITsThreadSynchronizer {

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
   * @see #syncExec
   */
  void asyncExec( Runnable aRunnable );

  /**
   * Causes the <code>run()</code> method of the runnable to be invoked by {@link #thread()} at the next reasonable
   * opportunity. The thread which calls this method is suspended until the runnable completes.
   *
   * @param aRunnable code to run on the uskat thread
   * @throws TsNullArgumentRtException argument = null
   * @see #asyncExec
   */
  void syncExec( Runnable aRunnable );

  /**
   * Causes the <code>run()</code> method of the runnable to be invoked {@link #thread()} after the specified number of
   * milliseconds have elapsed. If milliseconds is less than zero, the runnable is not executed.
   *
   * @param aMilliseconds the delay before running the runnable
   * @param aRunnable code to run on the uskat thread
   * @throws TsNullArgumentRtException argument = null
   * @see #asyncExec
   */
  void timerExec( int aMilliseconds, Runnable aRunnable );

}
