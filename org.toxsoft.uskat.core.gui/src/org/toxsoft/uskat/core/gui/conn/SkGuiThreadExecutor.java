package org.toxsoft.uskat.core.gui.conn;

import org.eclipse.swt.widgets.Display;
import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

import core.tslib.bricks.synchronize.ITsThreadExecutor;

/**
 * Реализация {@link ITsThreadExecutor} для GUI (SWT)
 *
 * @author mvk
 */
public final class SkGuiThreadExecutor
    implements ITsThreadExecutor {

  private final Display display;

  /**
   * Конструктор
   *
   * @param aDisplay {@link Display} дисплей
   * @throws TsNullArgumentRtException аргумент = null
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
    display.timerExec( aMilliseconds, aRunnable );
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
