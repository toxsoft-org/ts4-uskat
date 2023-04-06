package org.toxsoft.uskat.core.gui.km5;

import java.util.concurrent.locks.*;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.synch.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * KM5 utilities.
 * <p>
 * All methods are thread-safe.
 *
 * @author hazard157
 */
public class KM5Utils {

  private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

  private static final SynchronizedListEdit<IKM5ContributorCreator> creatorsList =
      new SynchronizedListEdit<>( new ElemArrayList<>(), lock );

  /**
   * Registers contributor creator to be used with all connections.
   *
   * @param aCreator {@link IKM5ContributorCreator} - the creator
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static void registerContributorCreator( IKM5ContributorCreator aCreator ) {
    TsNullArgumentRtException.checkNull( aCreator );
    lock.writeLock().lock();
    try {
      if( !creatorsList.hasElem( aCreator ) ) {
        creatorsList.add( aCreator );
      }
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Returns all registered contributor creators.
   *
   * @return {@link IList}&lt;{@link IKM5ContributorCreator}&gt; - copy of the creators list
   */
  public static IList<IKM5ContributorCreator> listContributorCreators() {
    return creatorsList.copyTo( null );
  }

  /**
   * No subclassing.
   */
  private KM5Utils() {
    // nop
  }

}
