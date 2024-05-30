package org.toxsoft.uskat.core.api.ugwis.helpers;

import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.ugwis.*;

/**
 * {@link IUgwiAvHelper} base implementation for kinds with atomic value representation.
 * <p>
 * Warning: this implementation assumes that <b>any</b> UGWI of this kind has the atomic value representation.
 *
 * @author hazard157
 * @param <T> - the UGWI content type
 * @param <K> - UGWI kind implementation class
 */
public abstract class AbstractUgwiAvHelperWithAv<T, K extends AbstractUgwiKind<T>>
    extends AbstractUgwiAvHelper<T, AbstractUgwiKind<T>> {

  /**
   * Constructor.
   *
   * @param aUgwiKind {@link AbstractUgwiKind} - parent kind
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public AbstractUgwiAvHelperWithAv( AbstractUgwiKind<T> aUgwiKind ) {
    super( aUgwiKind );
  }

  // ------------------------------------------------------------------------------------
  // AbstractUgwiAvHelper
  //

  @Override
  final protected boolean doHasAtomicValue( Ugwi aUgwi ) {
    return true;
  }

}
