package org.toxsoft.uskat.core.gui.utils.ugwi;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.utils.ugwi.kind.*;

/**
 * Helper static methods of UGWI handling.
 *
 * @author hazard157
 */
public class UgwiGuiUtils {

  private static final IStridablesListEdit<IUgwiKindGuiHelper> kindsMap = new StridablesList<>();

  static {
    kindsMap.put( UgwiKindNone.KIND_ID, UgwiKindGuiHelperNone.INSTANCE );
    kindsMap.put( UgwiKindFile.KIND_ID, UgwiKindGuiHelperFile.INSTANCE );
    kindsMap.put( UgwiKindSkid.KIND_ID, UgwiKindGuiHelperSkid.INSTANCE );
    kindsMap.put( UgwiKindGwid.KIND_ID, UgwiKindGuiHelperGwid.INSTANCE );
    kindsMap.put( UgwiKindURI.KIND_ID, UgwiKindGuiHelperURI.INSTANCE );
    kindsMap.put( UgwiKindURL.KIND_ID, UgwiKindGuiHelperURL.INSTANCE );
  }

  /**
   * Returns the {@link IUgwiKindGuiHelper} implementation of the specified kind.
   * <p>
   * It is callers responsibility to request correct type of the UGWI kind implementation. Otherwise
   * {@link ClassCastException} will be thrown.
   *
   * @param <T> - expected type of the {@link IUgwiKindGuiHelper} implementation
   * @param aKindId String - the kind ID
   * @return &lt;T&gt; - the UGWI kind
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no kind with specified ID is registered
   * @throws ClassCastException UGWI kind implementation is not of requested type
   */
  @SuppressWarnings( "unchecked" )
  public static <T extends IUgwiKindGuiHelper> T getKind( String aKindId ) {
    return (T)kindsMap.getByKey( aKindId );
  }

  /**
   * Returns the all registered UGWI kinds.
   *
   * @return {@link IStridablesList}&lt;{@link IUgwiKindGuiHelper}&gt; - list of registered kinds
   */
  public static IStridablesList<IUgwiKindGuiHelper> listRegisteredKinds() {
    return kindsMap;
  }

  /**
   * @param aKind {@link IUgwiKindGuiHelper} - the kind to register
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemAlreadyExistsRtException kind with the same ID is already registered
   */
  public static void registerKind( IUgwiKindGuiHelper aKind ) {
    TsNullArgumentRtException.checkNull( aKind );
    TsItemAlreadyExistsRtException.checkTrue( kindsMap.hasKey( aKind.id() ) );
    kindsMap.add( aKind );
  }

  /**
   * No subclasses.
   */
  private UgwiGuiUtils() {
    // nop
  }

}
