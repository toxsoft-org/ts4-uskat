package org.toxsoft.uskat.core.gui.conn;

import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;

/**
 * Реализация {@link ITsThreadExecutor} для GUI (SWT)
 *
 * @author mvk
 */
public final class SkGuiThreadExecutor
    implements ITsThreadExecutor {

  private final Display display;
  @SuppressWarnings( "unused" )
  private ILogger       logger;

  /**
   * Constructor.
   *
   * @param aDisplay {@link Display} -= the display
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SkGuiThreadExecutor( Display aDisplay ) {
    display = TsNullArgumentRtException.checkNull( aDisplay );
  }

  // ------------------------------------------------------------------------------------
  // ITsThreadExecutor
  //

  @Override
  public Thread thread() {
    return display.getThread();
  }

  @Override
  public void asyncExec( Runnable aRunnable ) {
    TsNullArgumentRtException.checkNull( aRunnable );
    display.asyncExec( aRunnable );
  }

  @Override
  public void syncExec( Runnable aRunnable ) {
    TsNullArgumentRtException.checkNull( aRunnable );
    display.syncExec( aRunnable );
  }

  @Override
  public void timerExec( int aMilliseconds, Runnable aRunnable ) {
    TsNullArgumentRtException.checkNull( aRunnable );
    if( display.getThread().equals( Thread.currentThread() ) ) {
      display.timerExec( aMilliseconds, aRunnable );
      return;
    }
    asyncExec( () -> display.timerExec( aMilliseconds, aRunnable ) );
  }

  @Override
  public void setLogger( ILogger aLogger ) {
    TsNullArgumentRtException.checkNull( aLogger );
    logger = aLogger;
  }

  // ------------------------------------------------------------------------------------
  // ICooperativeMultiTaskable
  //

  @Override
  public void doJob() {
    TsIllegalStateRtException.checkFalse( thread().equals( Thread.currentThread() ) );
    display.readAndDispatch();
  }

}
