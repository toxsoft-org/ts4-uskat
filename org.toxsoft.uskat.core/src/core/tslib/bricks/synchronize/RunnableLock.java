package core.tslib.bricks.synchronize;

import org.toxsoft.core.tslib.utils.logs.impl.LoggerUtils;

/**
 * Source: org.eclipse.swt.widgets.RunnableLock
 *
 * @author mvk
 */
class RunnableLock {

  Runnable  runnable;
  Thread    thread;
  Throwable throwable;

  RunnableLock( Runnable aRunnable ) {
    runnable = aRunnable;
  }

  boolean done() {
    return runnable == null || throwable != null;
  }

  void run() {
    if( runnable != null ) {
      try {
        runnable.run();
      }
      catch( RuntimeException | Error error ) {
        LoggerUtils.defaultLogger().error( error );
      }
    }
    runnable = null;
  }
}
