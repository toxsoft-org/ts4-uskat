package org.toxsoft.uskat.core.utils.ugwi;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;

import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * The UGWI kind description.
 * <p>
 * The ID {@link #id()} as a kind ID of the {@link Ugwi}.
 * <p>
 * User defined UGWI kinds must be registered by {@link UgwiUtils#registerKind(IUgwiKind)}for users of the UGWI to
 * handle UGWIs of different kinds. Note that {@link Ugwi} as a syntactical wrapper over canonical textual
 * representation does not uses {@link IUgwiKind}.
 *
 * @author hazard157
 */
public sealed interface IUgwiKind
    extends IStridableParameterized permits UgwiKind {

  /**
   * Checks if <code>aUgwi</code> is syntactically valid.
   * <p>
   * Following checks are performed:
   * <ul>
   * <li>{@link Ugwi#kindId()} matches {@link #id()}. Argument {@link Ugwi#NONE} is allowed for any kind and returns
   * <code>true</code>;</li>
   * <li>calls {@link #validateUgwi(String, String)} to check {@link Ugwi#namespace()} and {@link Ugwi#essence()}.</li>
   * </ul>
   * <p>
   * Syntactically valid UGWI does not guarantees existence of the addressed entity.
   *
   * @param aUgwi {@link Ugwi} - the entity identified
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult validateUgwi( Ugwi aUgwi );

  /**
   * Validates if UGWI components are valid for this kind.
   * <p>
   * Following checks are performed:
   * <ul>
   * <li><code>aNamespace</code> has allowed value. Namespace is not checked for existence, only syntactical check is
   * performed if kind uses additional constrains over the IDpath. For example, if namespace is interpreted as an
   * Internet domain name, it is required IDpath to contain two or more component;</li>
   * <li><code>aEssence</code> is checked for syntax validity.</li>
   * </ul>
   * <p>
   * Empty string as a namespace is valid for any kind of UGWI.
   *
   * @param aNamespace String - the namespace
   * @param aEssence String - the essence
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult validateUgwi( String aNamespace, String aEssence );

  /**
   * Returns the kind helper if any was registered.
   * <p>
   * Note: argument class must be exaclly the same as helper was reistered by {@link #registerHelper(Class, Object)}.
   *
   * @param <T> - expected type of the helper
   * @param aHelperClass {@link Class}&lt;T&gt; - helper class used at registration time
   * @return &lt;T&gt; - found helper or <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws ClassCastException found helper is not of requested type
   */
  <T> T findHelper( Class<T> aHelperClass );

  /**
   * Registers UGWI kind helper.
   *
   * @param <T> - type of the helper
   * @param aHelperClass {@link Class}&lt;T&gt; - key class used for helper registration
   * @param aHelper &lt;T&gt; - the helper instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemAlreadyExistsRtException helper with specified key is already registered
   * @throws ClassCastException found helper is not of specified type
   */
  <T> void registerHelper( Class<T> aHelperClass, T aHelper );

  // ------------------------------------------------------------------------------------
  // inline methods for convenience
  //

  @SuppressWarnings( "javadoc" )
  default ValidationResult validateUgwi( String aEssence ) {
    return validateUgwi( EMPTY_STRING, aEssence );
  }

  @SuppressWarnings( "javadoc" )
  default <T> T getHelper( Class<T> aHelperClass ) {
    return TsItemNotFoundRtException.checkNull( findHelper( aHelperClass ) );
  }

}
