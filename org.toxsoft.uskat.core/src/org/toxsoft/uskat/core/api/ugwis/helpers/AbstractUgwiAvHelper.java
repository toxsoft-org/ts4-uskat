package org.toxsoft.uskat.core.api.ugwis.helpers;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.ugwis.*;

/**
 * {@link IUgwiAvHelper} base implementation.
 *
 * @author hazard157
 * @param <T> - the UGWI content type
 * @param <K> - UGWI kind implementation class
 */
public abstract class AbstractUgwiAvHelper<T, K extends AbstractUgwiKind<T>>
    implements IUgwiAvHelper {

  private final K kind;

  /**
   * Constructor for subclass.
   *
   * @param aUgwiKind &lt;K&gt; - parent kind
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  protected AbstractUgwiAvHelper( K aUgwiKind ) {
    TsNullArgumentRtException.checkNull( aUgwiKind );
    kind = aUgwiKind;
  }

  // ------------------------------------------------------------------------------------
  // IUgwiAvHelper
  //

  @Override
  final public boolean hasAtomicValue( Ugwi aUgwi ) {
    TsNullArgumentRtException.checkNull( aUgwi );
    TsIllegalArgumentRtException.checkFalse( aUgwi.kindId().equals( kind.id() ) );
    return doHasAtomicValue( aUgwi );
  }

  @Override
  final public IAtomicValue getAtomicValue( Ugwi aUgwi ) {
    TsNullArgumentRtException.checkNull( aUgwi );
    TsIllegalArgumentRtException.checkFalse( aUgwi.kindId().equals( kind.id() ) );
    IAtomicValue av = doFindAtomicValue( aUgwi );
    return av != null ? av : IAtomicValue.NULL;
  }

  @Override
  final public IDataType getValueType( Ugwi aUgwi ) {
    TsNullArgumentRtException.checkNull( aUgwi );
    TsIllegalArgumentRtException.checkFalse( aUgwi.kindId().equals( kind.id() ) );
    IDataType dt = doGetValueType( aUgwi );
    return dt != null ? dt : IAvMetaConstants.DDEF_NONE;
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Returns the UGWI kind.
   *
   * @return &lt;K&gt; - parent kind
   */
  public K ugwiKind() {
    return kind;
  }

  // ------------------------------------------------------------------------------------
  // To override/implement
  //

  /**
   * Implementation must return the content as an atomic value.
   *
   * @param aUgwi {@link Ugwi} - the UGWI of this kind
   * @return boolean - <code>true</code> UGWI content may be represented as atomic value
   */
  protected abstract boolean doHasAtomicValue( Ugwi aUgwi );

  /**
   * Implementation must return the content as an atomic value.
   * <p>
   * Method may return null if the content does not exists.
   *
   * @param aUgwi {@link Ugwi} - the UGWI of this kind
   * @return {@link IAtomicValue} - content as atomic value or {@link IAtomicValue#NULL} or <code>null</code>
   */
  protected abstract IAtomicValue doFindAtomicValue( Ugwi aUgwi );

  /**
   * Implementation must return the data type.
   * <p>
   * Method may return null if the meta information about does not exists.
   *
   * @param aUgwi {@link Ugwi} - the UGWI of this kind
   * @return {@link IDataType} - the meta information or {@link IAvMetaConstants#DDEF_NONE} or <code>null</code>
   */
  protected abstract IDataType doGetValueType( Ugwi aUgwi );

}
