package core.tslib.bricks.synchronize;

import org.toxsoft.core.tslib.utils.logs.impl.LoggerUtils;

/**
 * Source: org.eclipse.swt.widgets.Synchronizer
 *
 * @author mvk
 */
class Synchronizer {

  Thread           doJobThread;
  int              messageCount;
  RunnableLock[]   messages;
  Object           messageLock   = new Object();
  Thread           syncThread;
  static final int GROW_SIZE     = 4;
  static final int MESSAGE_LIMIT = 64;

  /**
   * Constructs a new instance of this class.
   *
   * @param aDoJobThread {@link Thread} synchronized doJob thread
   */
  public Synchronizer( Thread aDoJobThread ) {
    doJobThread = aDoJobThread;
  }

  /**
   * Removes all pending events from the receiver and inserts them into the beginning of the given synchronizer's queue
   *
   * @param toReceiveTheEvents the synchronizer that will receive the events
   */
  void moveAllEventsTo( Synchronizer toReceiveTheEvents ) {
    RunnableLock[] oldMessages;
    int oldMessageCount;
    synchronized (messageLock) {
      oldMessages = messages;
      messages = null;
      oldMessageCount = messageCount;
      messageCount = 0;
    }
    toReceiveTheEvents.addFirst( oldMessages, oldMessageCount );
  }

  /**
   * Adds the given events to the beginning of the message queue, to be processed in order.
   *
   * @param toAdd events to add. Permits null if and only if numToAdd is 0.
   * @param numToAdd number of events to add from the beginning of the given array.
   */
  void addFirst( RunnableLock[] toAdd, int numToAdd ) {
    if( numToAdd <= 0 ) {
      return;
    }
    boolean wake = false;
    synchronized (messageLock) {
      int nextSize = messageCount + Math.max( numToAdd, GROW_SIZE );
      if( messages == null ) {
        messages = new RunnableLock[nextSize];
      }
      if( messages.length < messageCount + numToAdd ) {
        RunnableLock[] newMessages = new RunnableLock[nextSize];
        System.arraycopy( messages, 0, newMessages, numToAdd, messageCount );
        messages = newMessages;
      }
      else {
        System.arraycopy( messages, 0, messages, numToAdd, messageCount );
      }
      System.arraycopy( toAdd, 0, messages, 0, numToAdd );
      wake = (messageCount == 0);
      messageCount += numToAdd;
    }
    if( wake ) {
      // TODO: ???
      // display.wakeThread();
    }
  }

  void addLast( RunnableLock lock ) {
    boolean wake = false;
    synchronized (messageLock) {
      if( messages == null ) {
        messages = new RunnableLock[GROW_SIZE];
      }
      if( messageCount == messages.length ) {
        RunnableLock[] newMessages = new RunnableLock[messageCount + GROW_SIZE];
        System.arraycopy( messages, 0, newMessages, 0, messageCount );
        messages = newMessages;
      }
      messages[messageCount++] = lock;
      wake = messageCount == 1;
    }
    if( wake ) {
      // TODO: ???
      // display.wakeThread();
    }
  }

  /**
   * Causes the <code>run()</code> method of the runnable to be invoked by the user-interface thread at the next
   * reasonable opportunity. The caller of this method continues to run in parallel, and is not notified when the
   * runnable has completed.
   *
   * @param runnable code to run on the user-interface thread.
   * @see #syncExec
   */
  protected void asyncExec( Runnable runnable ) {
    addLast( new RunnableLock( runnable ) );
  }

  int getMessageCount() {
    synchronized (messageLock) {
      return messageCount;
    }
  }

  void releaseSynchronizer() {
    messages = null;
    messageLock = null;
    syncThread = null;
  }

  RunnableLock removeFirst() {
    synchronized (messageLock) {
      if( messageCount == 0 ) {
        return null;
      }
      RunnableLock lock = messages[0];
      System.arraycopy( messages, 1, messages, 0, --messageCount );
      messages[messageCount] = null;
      if( messageCount == 0 ) {
        if( messages.length > MESSAGE_LIMIT ) {
          messages = null;
        }
      }
      return lock;
    }
  }

  boolean runAsyncMessages() {
    return runAsyncMessages( false );
  }

  boolean runAsyncMessages( boolean all ) {
    boolean run = false;
    do {
      RunnableLock lock = removeFirst();
      if( lock == null ) {
        return run;
      }
      run = true;
      synchronized (lock) {
        syncThread = lock.thread;
        try {
          lock.run();
        }
        catch( Throwable t ) {
          lock.throwable = t;
        }
        finally {
          syncThread = null;
          lock.notifyAll();
        }
      }
    } while( all );
    return run;
  }

  /**
   * Causes the <code>run()</code> method of the runnable to be invoked by the user-interface thread at the next
   * reasonable opportunity. The thread which calls this method is suspended until the runnable completes.
   *
   * @param runnable code to run on the user-interface thread.
   * @see #asyncExec
   */
  protected void syncExec( Runnable runnable ) {
    RunnableLock lock = null;
    synchronized (this) {
      if( doJobThread != Thread.currentThread() ) {
        lock = new RunnableLock( runnable );
        /*
         * Only remember the syncThread for syncExec.
         */
        lock.thread = Thread.currentThread();
        addLast( lock );
      }
    }
    if( lock == null ) {
      if( runnable != null ) {
        try {
          runnable.run();
        }
        catch( RuntimeException | Error error ) {
          LoggerUtils.defaultLogger().error( error );
        }
      }
      return;
    }
    synchronized (lock) {
      boolean interrupted = false;
      while( !lock.done() ) {
        try {
          lock.wait();
        }
        catch( InterruptedException e ) {
          interrupted = true;
          LoggerUtils.defaultLogger().error( e );
        }
      }
      if( interrupted ) {
        Thread.currentThread().interrupt();
      }
      if( lock.throwable != null ) {
        LoggerUtils.defaultLogger().error( lock.throwable );
      }
    }
  }
}
