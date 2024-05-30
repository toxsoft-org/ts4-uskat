package org.toxsoft.uskat.core.api.ugwis.helpers;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.ugwis.*;

/**
 * Helps to get UGWI content as an atomic value.
 * <p>
 * TODO motivation and usage
 *
 * @author hazard157
 */
public interface IUgwiAvHelper {

  /**
   * Determines if the addressed content has a meaningful atomic value representation.
   * <p>
   * The UGWI may have an atomic value representation even if {@link IUgwiKind#findContent(Ugwi)} returns not an
   * {@link IAtomicValue}. Note that this method does not checks the content existence.
   *
   * @param aUgwi {@link Ugwi} - the UGWI
   * @return boolean - <code>true</code> UGWI content may be represented as atomic value
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException argument is not of this kind
   */
  boolean hasAtomicValue( Ugwi aUgwi );

  /**
   * Finds the content addressed by the UGWI and returns it's atomic value representation if applicable.
   * <p>
   * If content does not exists or content does not has atomic value representation returns {@link IAtomicValue#NULL}.
   *
   * @param aUgwi {@link Ugwi} - the UGWI
   * @return {@link IAtomicValue} - content as atomic value or {@link IAtomicValue#NULL}
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException argument is not of this kind
   */
  IAtomicValue getAtomicValue( Ugwi aUgwi );

  /**
   * Returns the data type of the value to be returned by {@link #getAtomicValue(Ugwi)}.
   * <p>
   * This method is intended to retrieve meta-information from USkat and use it together with atomic value returned by
   * {@link #getAtomicValue(Ugwi)}. For example, {@link IDataType#formatString()} may be used for unified textual
   * representation of the value.
   * <p>
   * If met-information retrieved or content does not has atomic value representation returns
   * {@link IAvMetaConstants#DDEF_NONE}.
   *
   * @param aUgwi {@link Ugwi} - the UGWI
   * @return {@link IDataType} - the atomic value meta information or {@link IAvMetaConstants#DDEF_NONE}
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException argument is not of this kind
   */
  IDataType getValueType( Ugwi aUgwi );

}
