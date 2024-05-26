package org.toxsoft.uskat.core.api.ugwis;

import java.util.concurrent.locks.*;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.synch.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Helper static methods of UGWI handling.
 * <p>
 * All methods are thread-safe.
 *
 * @author hazard157
 */
public class SkUgwiUtils {

  private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

  private static final SynchronizedListEdit<AbstractUgwiKindRegistrator<?>> creatorsList =
      new SynchronizedListEdit<>( new ElemArrayList<>(), lock );

  /**
   * Defines (creates and adds) registered UGWI kinds to {@link ISkUgwiService#listKinds()}.
   *
   * @param aSkConn {@link ISkConnection} - the Sk-connection
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException connection is in closed state
   */
  public static void defineKindsToTheSkConnection( ISkConnection aSkConn ) {
    TsNullArgumentRtException.checkNull( aSkConn );
    TsIllegalArgumentRtException.checkFalse( aSkConn.state().isOpen() );
    ISkUgwiService ugwiService = aSkConn.coreApi().ugwiService();
    for( AbstractUgwiKindRegistrator<?> kr : creatorsList.copyTo( null ) ) {
      AbstractUgwiKind<?> kind = kr.createUgwiKind( aSkConn );
      ugwiService.registerKind( kind );
    }
  }

  /**
   * Registers kind registrator to be used with all connections.
   *
   * @param aCreator {@link AbstractUgwiKindRegistrator} - the creator
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static void registerUgwiKindRegistrator( AbstractUgwiKindRegistrator<?> aCreator ) {
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
   * @return {@link IList}&lt;{@link AbstractUgwiKindRegistrator}&gt; - copy of the creators list
   */
  public static IList<AbstractUgwiKindRegistrator<?>> listUgwiKindCreators() {
    return creatorsList.copyTo( null );
  }

  /**
   * No subclasses.
   */
  private SkUgwiUtils() {
    // nop
  }

}
