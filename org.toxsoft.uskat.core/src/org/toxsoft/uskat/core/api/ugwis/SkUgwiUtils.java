package org.toxsoft.uskat.core.api.ugwis;

import java.util.concurrent.locks.*;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.*;
import org.toxsoft.uskat.core.api.ugwis.kinds.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * Helper static methods of UGWI handling.
 * <p>
 * All methods are thread-safe.
 *
 * @author hazard157
 */
public class SkUgwiUtils {

  private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

  private static final SynchronizedStridablesList<AbstractUgwiKind<?>> ugwiKindsList =
      new SynchronizedStridablesList<>( new StridablesList<>(), lock );

  static {
    ugwiKindsList.add( UgwiKindSkAttr.INSTANCE );
    ugwiKindsList.add( UgwiKindSkRtdata.INSTANCE );
    ugwiKindsList.add( UgwiKindSkCmd.INSTANCE );
    ugwiKindsList.add( UgwiKindSkSkid.INSTANCE );
    ugwiKindsList.add( UgwiKindSkLink.INSTANCE );
    ugwiKindsList.add( UgwiKindSkRivet.INSTANCE );
    // TODO add all built-in registrators
  }

  /**
   * Core handler to register all Sk-connection bound {@link ISkUgwiKind} when connection opens.
   * <p>
   * Note: not for users! Field is public for {@link SkCoreUtils} to access it.
   */
  public static final ISkCoreExternalHandler ugwisRegistrationHandler = aCoreApi -> {
    ISkUgwiService uServ = ISkUgwiService.class.cast( aCoreApi.services().findByKey( ISkUgwiService.SERVICE_ID ) );
    if( uServ != null ) {
      for( AbstractUgwiKind<?> uk : ugwiKindsList ) {
        AbstractSkUgwiKind<?> skuk = uk.createUgwiKind( aCoreApi );
        uServ.registerKind( skuk );
      }
    }
  };

  /**
   * Returns all registered UGWI kinds.
   *
   * @return {@link IStridablesList}&lt;{@link AbstractUgwiKind}&gt; - copy of the UGWI kinds list
   */
  public static IStridablesList<AbstractUgwiKind<?>> listUgwiKinds() {
    return ugwiKindsList.copyTo( null );
  }

  /**
   * Registers kind registrator to be used with all connections.
   * <p>
   * Does nothing if the king with same ID was already registered.
   *
   * @param aCreator {@link AbstractUgwiKind} - the creator
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static void registerUgwiKind( AbstractUgwiKind<?> aCreator ) {
    TsNullArgumentRtException.checkNull( aCreator );
    lock.writeLock().lock();
    try {
      if( !ugwiKindsList.hasElem( aCreator ) ) {
        ugwiKindsList.add( aCreator );
      }
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * No subclasses.
   */
  private SkUgwiUtils() {
    // nop
  }

}
