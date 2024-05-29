package org.toxsoft.uskat.core.api.ugwis;

import java.util.concurrent.locks.*;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.synch.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.ugwis.kinds.*;

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

  static {
    creatorsList.add( UgwiKindSkAttr.REGISTRATOR );
    // TODO add all built-in registrators
  }

  /**
   * Defines (creates and adds) registered UGWI kinds to {@link ISkUgwiService#listKinds()}.
   *
   * @param aCoreApi {@link ISkCoreApi} - the core API
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException connection is in closed state
   */
  public static void defineKindsToTheSkConnection( ISkCoreApi aCoreApi ) {
    TsNullArgumentRtException.checkNull( aCoreApi );
    ISkUgwiService ugwiService = aCoreApi.ugwiService();
    for( AbstractUgwiKindRegistrator<?> kr : creatorsList.copyTo( null ) ) {
      AbstractUgwiKind<?> kind = kr.createUgwiKind( aCoreApi );
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
