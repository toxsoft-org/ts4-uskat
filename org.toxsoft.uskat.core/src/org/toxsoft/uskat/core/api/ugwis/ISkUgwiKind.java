package org.toxsoft.uskat.core.api.ugwis;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;

/**
 * The UGWI kind description bound to the specific Sk-connection via {@link #coreApi()}.
 * <p>
 * This is the Sk-connection dependent extension of the static {@link IUgwiKind} singleton.
 * <p>
 * The {@link #id()} is the same as {@link IUgwiKind#id()} and means the UGWI kind ID {@link Ugwi#kindId()}. Event more
 * methods of {@link IStridableParameterized} returns the same values as for {@link #ugwiKind()}.
 *
 * @author hazard157
 */
public sealed interface ISkUgwiKind
    extends IStridableParameterized
    permits AbstractSkUgwiKind {

  /**
   * Returns the core API of the Sk-connection this kind is bound to.
   *
   * @return {@link ISkCoreApi} - the core API
   */
  ISkCoreApi coreApi();

  /**
   * Returns the static part of the kind description.
   *
   * @return {@link IUgwiKind} - the static, Sk-connection independent kind singleton
   */
  IUgwiKind ugwiKind();

  /**
   * Finds the content addressed by the UGWI.
   *
   * @param aUgwi {@link Ugwi} - the UGWI
   * @return {@link Object} - the content or <code>null</code> if not found
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException argument is not of expected kind
   */
  Object findContent( Ugwi aUgwi );

  /**
   * Determines if the addressed content has a natural atomic value representation.
   * <p>
   * Any UGWI content may be represented as an atomic value however <i>natural</i> means that the
   * {@link ISkUgwiKind#findContent(Ugwi)} returns {@link IAtomicValue}.
   * <p>
   * Note: this method does not checks the content existence. For most kinds it even does not analysis the essence
   * {@link Ugwi#essence()} because the kind is designed for a concrete type of content.
   *
   * @param aUgwi {@link Ugwi} - the UGWI
   * @return boolean - <code>true</code> UGWI content may be represented as atomic value
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException argument is not of this kind
   */
  boolean isNaturalAtomicValue( Ugwi aUgwi );

  /**
   * Finds the content addressed by the UGWI and returns it's atomic value representation if applicable.
   * <p>
   * There are several cases when executing the method:
   * <ul>
   * <li>If content does not exists method returns {@link IAtomicValue#NULL}, as a data type
   * {@link IAvMetaConstants#DDEF_NONE} is returned;</li>
   * <li>If content is a natural atomic value method returns {@link ISkUgwiKind#getContent(Ugwi)}. As a data type the
   * meta-information from the respective Sk-service is returned;</li>
   * <li>If content can be represented as an atomic value method returns value created by
   * {@link AvUtils#avFromObj(Object)}. A data type is a simple wrapper over value returned by
   * {@link AvUtils#atFromObj(Object)};</li>
   * <li>Content can not be represented as an atomic value, so {@link EAtomicType#STRING} will be returned with
   * {@link Object#toString() content.toString()}. As a data type {@link IAvMetaConstants#DDEF_STRING} is returned.</li>
   * </ul>
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
   * Values returned for difference cases are described in the comments of the method {@link #getAtomicValue(Ugwi)}.
   *
   * @param aUgwi {@link Ugwi} - the UGWI
   * @return {@link IDataType} - the atomic value meta information or {@link IAvMetaConstants#DDEF_NONE}
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException argument is not of this kind
   */
  IDataType getAtomicValueDataType( Ugwi aUgwi );

  /**
   * Returns the kind helper if any was registered.
   * <p>
   * Note: argument class must be exactly the same as helper was registered by {@link #registerHelper(Class, Object)}.
   *
   * @param <H> - expected type of the helper
   * @param aHelperClass {@link Class}&lt;T&gt; - helper class used at registration time
   * @return &lt;H&gt; - found helper or <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws ClassCastException found helper is not of requested type
   */
  <H> H findHelper( Class<H> aHelperClass );

  /**
   * Registers UGWI kind helper.
   * <p>
   * Existing registered helper will be overwritten.
   *
   * @param <H> - type of the helper
   * @param aHelperClass {@link Class}&lt;T&gt; - key class used for helper registration
   * @param aHelper &lt;H&gt; - the helper instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  <H> void registerHelper( Class<H> aHelperClass, H aHelper );

  // ------------------------------------------------------------------------------------
  // inline methods for convenience
  // Note: some inline methods may be re-implemented for the optimization reasons
  //

  /**
   * Returns the kind helper.
   * <p>
   * Note: argument class must be exactly the same as helper was registered by {@link #registerHelper(Class, Object)}.
   *
   * @param <H> - expected type of the helper
   * @param aHelperClass {@link Class}&lt;T&gt; - helper class used at registration time
   * @return &lt;T&gt; - found helper
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws ClassCastException found helper is not of requested type
   * @throws TsItemNotFoundRtException no registered helper of the specified class
   */
  default <H> H getHelper( Class<H> aHelperClass ) {
    return TsItemNotFoundRtException.checkNull( findHelper( aHelperClass ) );
  }

  /**
   * Determines if the content addresses by the UGWI exists.
   *
   * @param aUgwi {@link Ugwi} - the UGWI
   * @return boolean - <code>true</code> if content exists so {@link #findContent(Ugwi)} returns non-<code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException argument is not of expected kind
   */
  default boolean isContent( Ugwi aUgwi ) {
    return findContent( aUgwi ) != null;
  }

  /**
   * Finds content as the specified class.
   *
   * @param <T> - expected type of the content
   * @param aUgwi {@link Gwid} - the UGWI
   * @param aContentClass {@link Class}&lt;T&gt; - expected class of the cntent
   * @return {@link Object} - the content or <code>null</code> if not found
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws ClassCastException content was found but not of expected type
   */
  default <T> T findContentAs( Ugwi aUgwi, Class<T> aContentClass ) {
    TsNullArgumentRtException.checkNull( aContentClass );
    return aContentClass.cast( findContent( aUgwi ) );
  }

  /**
   * Returns the content addresses by the UGWI.
   *
   * @param aUgwi {@link Ugwi} - the UGWI
   * @return {@link Object} - the content
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException content not found
   * @throws TsIllegalArgumentRtException argument is not of expected kind
   */
  default Object getContent( Ugwi aUgwi ) {
    return TsItemNotFoundRtException.checkNull( findContent( aUgwi ) );
  }

}
