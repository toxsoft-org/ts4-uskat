package org.toxsoft.uskat.core.utils.ugwi;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.utils.ugwi.kind.*;

/**
 * Helper static methods of UGWI handling.
 *
 * @author hazard157
 */
public class UgwiUtils {

  private static final IStridablesListEdit<IUgwiKind> kindsList = new StridablesList<>();

  static {
    kindsList.add( UgwiKindNone.INSTANCE );
    kindsList.add( UgwiKindFile.INSTANCE );
    kindsList.add( UgwiKindSkid.INSTANCE );
    kindsList.add( UgwiKindGwid.INSTANCE );
    kindsList.add( UgwiKindURI.INSTANCE );
    kindsList.add( UgwiKindURL.INSTANCE );
  }

  /**
   * Returns the {@link IUgwiKind} implementation of the specified kind.
   * <p>
   * It is callers responsibility to request correct type of the UGWI kind implementation. Otherwise
   * {@link ClassCastException} will be thrown.
   *
   * @param <T> - expected type of the {@link IUgwiKind} implementation
   * @param aKindId String - the kind ID
   * @return &lt;T&gt; - the UGWI kind
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no kind with specified ID is registered
   * @throws ClassCastException UGWI kind implementation is not of requested type
   */
  @SuppressWarnings( "unchecked" )
  public static <T extends IUgwiKind> T getKind( String aKindId ) {
    return (T)kindsList.getByKey( aKindId );
  }

  /**
   * Returns the all registered UGWI kinds.
   *
   * @return {@link IStridablesList}&lt;{@link IUgwiKind}&gt; - list of registered kinds
   */
  public static IStridablesList<IUgwiKind> listRegisteredKinds() {
    return kindsList;
  }

  /**
   * @param aKind {@link IUgwiKind} - the kind to register
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemAlreadyExistsRtException kind with the same ID is already registered
   */
  public static void registerKind( IUgwiKind aKind ) {
    TsNullArgumentRtException.checkNull( aKind );
    TsItemAlreadyExistsRtException.checkTrue( kindsList.hasKey( aKind.id() ) );
    kindsList.add( aKind );
  }

  /**
   * No subclasses.
   */
  private UgwiUtils() {
    // nop
  }

}
