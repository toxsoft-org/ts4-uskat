package core.tslib.bricks.synchronize;

import java.util.concurrent.*;

import org.toxsoft.core.tslib.bricks.ICooperativeMultiTaskable;
import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Implementation {@link ITsThreadSynchronizer} for server and console apps
 *
 * @author mvk
 */
public final class TsThreadSynchronizer
    implements ITsThreadSynchronizer, ICooperativeMultiTaskable {

  private final Thread                   doJobThread;
  private final Synchronizer             synchronizer;
  private final ScheduledExecutorService timerService;

  /**
   * Констурктор
   *
   * @param aDoJobThread {@link Thread} synchronized doJob thread
   * @throws TsNullArgumentRtException arg = null
   */
  public TsThreadSynchronizer( Thread aDoJobThread ) {
    doJobThread = TsNullArgumentRtException.checkNull( aDoJobThread );
    synchronizer = new Synchronizer( aDoJobThread );
    timerService = Executors.newScheduledThreadPool( 1 );
  }

  // ------------------------------------------------------------------------------------
  // ITsThreadSynchronizer
  //
  @Override
  public Thread thread() {
    return doJobThread;
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
    timerService.schedule( aRunnable, aMilliseconds, TimeUnit.MILLISECONDS );
  }

  // ------------------------------------------------------------------------------------
  // ICooperativeMultiTaskable
  //
  @Override
  public void doJob() {
    TsIllegalStateRtException.checkFalse( Thread.currentThread() == doJobThread );
    synchronizer.runAsyncMessages();
  }

  // ------------------------------------------------------------------------------------
  // private methods
  //
}
