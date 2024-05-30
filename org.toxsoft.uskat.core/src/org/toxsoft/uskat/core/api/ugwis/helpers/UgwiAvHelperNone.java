package org.toxsoft.uskat.core.api.ugwis.helpers;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.ugwis.*;

/**
 * {@link IUgwiAvHelper} implementation for kinds having no atomic value representation.
 * <p>
 * Instance of this class is always created in {@link AbstractUgwiKind} constructor to be returned by
 * {@link IUgwiKind#getAvHelper()} method.
 *
 * @author hazard157
 * @param <T> - the UGWI content type
 */
public class UgwiAvHelperNone<T>
    extends AbstractUgwiAvHelper<T, AbstractUgwiKind<T>> {

  /**
   * Constructor.
   *
   * @param aUgwiKind {@link AbstractUgwiKind} - parent kind
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public UgwiAvHelperNone( AbstractUgwiKind<T> aUgwiKind ) {
    super( aUgwiKind );
  }

  // ------------------------------------------------------------------------------------
  // AbstractUgwiAvHelper
  //

  @Override
  protected boolean doHasAtomicValue( Ugwi aUgwi ) {
    return false;
  }

  @Override
  protected IAtomicValue doFindAtomicValue( Ugwi aUgwi ) {
    Object content = ugwiKind().findContent( aUgwi );
    return AvUtils.avFromObj( content );
  }

  @Override
  protected IDataType doGetValueType( Ugwi aUgwi ) {
    return IAvMetaConstants.DDEF_NONE;
  }

}
