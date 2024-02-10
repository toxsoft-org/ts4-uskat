package core.tslib.bricks.threadexecutor;

import java.util.concurrent.Executor;

import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Implementation {@link ITsThreadExecutor} for server and console apps
 *
 * @author mvk
 */
public final class TsThreadExecutor
    implements ITsThreadExecutor {

  private TsSynchronizer synchronizer;

  /**
   * Default constructor.
   */
  public TsThreadExecutor() {
    synchronizer = new TsSynchronizer();
  }

  /**
   * Constructor with specify synchronized doJob thread.
   *
   * @param aDoJobThread {@link Thread} synchronized doJob thread
   * @throws TsNullArgumentRtException arg = null
   */
  public TsThreadExecutor( Thread aDoJobThread ) {
    TsNullArgumentRtException.checkNull( aDoJobThread );
    synchronizer = new TsSynchronizer( aDoJobThread );
  }

  /**
   * Constructor with specify synchronized ExecutorService.
   *
   * @param aExecutor {@link Executor} executor of synchronized doJob thread
   * @throws TsNullArgumentRtException arg = null
   */
  public TsThreadExecutor( Executor aExecutor ) {
    TsNullArgumentRtException.checkNull( aExecutor );
    synchronizer = new TsSynchronizer( aExecutor );
  }

  /**
   * Set new executor for synchronizer
   *
   * @param aExecutor {@link Executor} executor
   * @throws TsNullArgumentRtException arg = null
   */
  public void setExecutor( Executor aExecutor ) {
    TsNullArgumentRtException.checkNull( aExecutor );
    synchronizer.setExecutor( aExecutor );
  }

  /**
   * Free all resources and close synchronizer.
   */
  public void close() {
    synchronizer.close();
  }

  // ------------------------------------------------------------------------------------
  // ITsThreadExecutor
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

  // ------------------------------------------------------------------------------------
  // ICooperativeMultiTaskable
  //
  @Override
  public void doJob() {
    TsIllegalStateRtException.checkFalse( thread().equals( Thread.currentThread() ) );
    // aAll = true
    synchronizer.runAsyncMessages( true );
  }

  // ------------------------------------------------------------------------------------
  // private methods
  //
}
