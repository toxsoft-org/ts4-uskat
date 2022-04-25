package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.connection.*;

// TODO TRANSLATE

/**
 * Helper class to separate backend and frontend execution theads.
 * <p>
 * <h2>Usage:</h2><br>
 * To separate backend and fromtend threads you need to:
 * <ul>
 * <li>create instance of this class before opening the connection;</li>
 * <li>put the reference to the instance as {@link ISkCoreConfigConstants#REFDEF_BACKEND_THREAD_SEPARATOR} as the argument
 * for connection opening {@link ISkConnection#open(ITsContextRo)};</li>
 * <li>call the {@link #doJob()} method from the same execution thread from which {@link ISkCoreApi} and Sk-services are
 * called with the necessary periodicity .</li>
 * </ul>
 *
 * @author hazard157
 */
public final class SkBackendThreadSeparator
    implements ICooperativeMultiTaskable {

  /**
   * Tasks to be called from {@link #doJob()}.
   * <p>
   * See the comments on the {@link #updateAddRemoveLists()} methods for details on the implementation of adding and
   * removing tasks in {@link #tasks}.
   */
  private final IListEdit<ICooperativeMultiTaskable> tasks = new ElemArrayList<>();

  // tasks waiting to be added/removed to the actual tasks list
  private final IListEdit<ICooperativeMultiTaskable> toAdd    = new ElemArrayList<>();
  private final IListEdit<ICooperativeMultiTaskable> toRemove = new ElemArrayList<>();

  /**
   * Constructor.
   */
  public SkBackendThreadSeparator() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // API пакета
  //

  /**
   * Adds a task to be executed from {@link #doJob()}.
   * <p>
   * If such a task has already been added, the method does nothing.
   * <p>
   * The method call must be made from the execution thread {@link #doJob()}.
   * <p>
   * See the comments on the {@link #updateAddRemoveLists()} methods for details on the implementation of adding and
   * removing tasks in {@link #tasks}.
   *
   * @param aDoJobTask {@link ICooperativeMultiTaskable} - the tsak to be added
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  void addDoJobTask( ICooperativeMultiTaskable aDoJobTask ) {
    // if it was previously supposed to delete the task, then cancel the removal
    toRemove.remove( aDoJobTask );
    if( !toAdd.hasElem( aDoJobTask ) ) {
      toAdd.add( aDoJobTask );
    }
  }

  /**
   * Removes a task previously added using the {@link #addDoJobTask(ICooperativeMultiTaskable)} method.
   * <p>
   * If no such task has been added, the method does nothing.
   * <p>
   * The method call must be made from the execution thread {@link #doJob()}.
   * <p>
   * See the comments on the {@link #updateAddRemoveLists()} methods for details on the implementation of adding and
   * removing tasks in {@link #tasks}.
   *
   * @param aDoJobTask {@link ICooperativeMultiTaskable} - the tsak to be removed
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  void removeDoJobTask( ICooperativeMultiTaskable aDoJobTask ) {
    // if it was previously supposed to add a task, then cancel the addition
    toAdd.remove( aDoJobTask );
    if( !toRemove.hasElem( aDoJobTask ) ) {
      toRemove.add( aDoJobTask );
    }
  }

  /**
   * Really adds/removes tasks to/from {@link #tasks}.
   * <p>
   * Adding/removing tasks to/from {@link #tasks} cannot be done from {@link #doJob()}, since at this time the list
   * {@link #tasks} is iterated, and this process does not tolerate changing the list "on the fly". Therefore, tasks are
   * added to the {@link #toAdd} and {@link #toRemove} temporary lists, and from them are transferred to {@link #tasks}
   * when entering the {@link #doJob()} method.
   * <p>
   * An alternative in the form of the usual approach - creating a copy of {@link #tasks} and iterating over the copy is
   * not implemented for performance reasons.
   */
  void updateAddRemoveLists() {
    // remove the tasks
    while( !toRemove.isEmpty() ) {
      ICooperativeMultiTaskable t = toRemove.removeByIndex( 0 );
      tasks.remove( t );
    }
    // add the tasks
    while( !toAdd.isEmpty() ) {
      ICooperativeMultiTaskable t = toAdd.removeByIndex( 0 );
      tasks.add( t );
    }
  }

  // ------------------------------------------------------------------------------------
  // ICooperativeMultiTaskable
  //

  @Override
  public void doJob() {
    updateAddRemoveLists();
    for( int i = 0, n = tasks.size(); i < n; i++ ) {
      ICooperativeMultiTaskable t = tasks.get( i );
      try {
        t.doJob();
      }
      catch( Throwable ex ) {
        LoggerUtils.errorLogger().error( ex );
      }
    }
  }

}
