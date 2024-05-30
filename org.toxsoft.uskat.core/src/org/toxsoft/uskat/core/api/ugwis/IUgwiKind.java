package org.toxsoft.uskat.core.api.ugwis;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;

import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.ugwis.helpers.*;

/**
 * The UGWI kind description.
 * <p>
 * The ID {@link #id()} as a kind ID of the {@link Ugwi}.
 * <p>
 * UGWI kinds must be registered by {@link SkUgwiUtils#registerUgwiKindRegistrator(AbstractUgwiKindRegistrator)} for
 * users of the UGWI to handle UGWIs of different kinds. Note that {@link Ugwi} as a syntactical wrapper over canonical
 * textual representation does not uses {@link IUgwiKind}.
 *
 * @author hazard157
 */
public sealed interface IUgwiKind
    extends IStridableParameterized
    permits AbstractUgwiKind {

  /**
   * Checks if <code>aUgwi</code> is syntactically valid.
   * <p>
   * Simply calls {@link ISkUgwiKindRegistrator#validateUgwi(Ugwi)}.
   *
   * @param aUgwi {@link Ugwi} - the entity identified
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult validateUgwi( Ugwi aUgwi );

  /**
   * Validates if UGWI components are valid for this kind.
   * <p>
   * Simply calls {@link ISkUgwiKindRegistrator#validateUgwi(String, String)}.
   *
   * @param aNamespace String - the namespace
   * @param aEssence String - the essence
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult validateUgwi( String aNamespace, String aEssence );

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
  // Note: inline methods may be re-implemented for the optimization reasons
  //

  /**
   * Returns {@link IUgwiAvHelper} for this kind.
   * <p>
   * This method always returns non-<code>null</code> helper.
   *
   * @return {@link IUgwiAvHelper} - the atomic value helper for this kind, never is null
   */
  default IUgwiAvHelper getAvHelper() {
    return getHelper( IUgwiAvHelper.class );
  }

  /**
   * Validates if UGWI components are valid for this kind assuming the empty namespace.
   *
   * @param aEssence String - the essence
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  default ValidationResult validateUgwi( String aEssence ) {
    return validateUgwi( EMPTY_STRING, aEssence );
  }

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
