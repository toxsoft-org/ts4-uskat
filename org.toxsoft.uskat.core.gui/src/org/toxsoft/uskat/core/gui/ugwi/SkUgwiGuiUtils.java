package org.toxsoft.uskat.core.gui.ugwi;

import java.util.concurrent.locks.*;

import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.ugwis.*;
import org.toxsoft.uskat.core.api.ugwis.kinds.*;
import org.toxsoft.uskat.core.gui.conn.*;
import org.toxsoft.uskat.core.gui.ugwi.kinds.*;

/**
 * UGWI GUI support utilities.
 *
 * @author hazard157
 */
public class SkUgwiGuiUtils {

  /**
   * Client must implement this interface to automate adding of UGWI kind helpers.
   *
   * @author hazard157
   * @param <T> - the UGWI content type
   */
  public interface IRegistrator<T> {

    /**
     * Called by {@link ISkConnectionSupplier} when the connection is open.
     * <p>
     * Implementation must create helper instances and register for the specified kubd by
     * {@link AbstractUgwiKind#registerHelper(Class, Object)}.
     *
     * @param aKind {@link AbstractUgwiKind} - UGWI kind to register helper
     */
    void registerHelpersForKind( AbstractUgwiKind<T> aKind );

  }

  private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

  private static final IStringMapEdit<IListEdit<IRegistrator<?>>> helperRegistrators = new StringMap<>();

  static {
    addRegistrator( UgwiKindSkAttr.KIND_ID, UgwiGuiHelperSkAttr.REGISTRATOR );
    // TODO add all built-in helper registrators
  }

  /**
   * Returns helper registrators.
   *
   * @return {@link IStringMap}&lt;{@link IList}&lt;{@link IRegistrator}&gt;&gt; - map "kindID" - "registrators list"
   */
  public static IStringMap<IList<IRegistrator<?>>> getHelperRegistratorsMap() {
    lock.readLock().lock();
    try {
      IStringMapEdit<IList<IRegistrator<?>>> map = new StringMap<>();
      for( String ugwiKindId : helperRegistrators.keys() ) {
        IList<IRegistrator<?>> ll = new ElemArrayList<>( helperRegistrators.getByKey( ugwiKindId ) );
        map.put( ugwiKindId, ll );
      }
      return map;
    }
    finally {
      lock.readLock().unlock();
    }
  }

  /**
   * Add registrator to the UGWI kind.
   *
   * @param aUgwiKindId String - the UGWI kind ID
   * @param aRegistrator {@link IRegistrator} - helpers registrator
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath
   */
  public static void addRegistrator( String aUgwiKindId, IRegistrator<?> aRegistrator ) {
    StridUtils.checkValidIdPath( aUgwiKindId );
    TsNullArgumentRtException.checkNull( aRegistrator );
    lock.writeLock().lock();
    try {
      IListEdit<IRegistrator<?>> ll = helperRegistrators.findByKey( aUgwiKindId );
      if( ll == null ) {
        ll = new ElemArrayList<>();
        helperRegistrators.put( aUgwiKindId, ll );
      }
      if( !ll.hasElem( aRegistrator ) ) {
        ll.add( aRegistrator );
      }
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * No subclasses.
   */
  private SkUgwiGuiUtils() {
    // nop
  }
}
