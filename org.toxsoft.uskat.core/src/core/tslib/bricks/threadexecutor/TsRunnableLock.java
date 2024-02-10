package core.tslib.bricks.threadexecutor;

import org.toxsoft.core.tslib.utils.logs.impl.LoggerUtils;

/**
 * Source: org.eclipse.swt.widgets.RunnableLock
 *
 * @author mvk
 */
class TsRunnableLock {

  Runnable  runnable;
  long      timestamp;
  Thread    thread;
  Throwable throwable;

  TsRunnableLock( Runnable aRunnable, int aDelay ) {
    runnable = aRunnable;
    timestamp = System.currentTimeMillis() + aDelay;
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
