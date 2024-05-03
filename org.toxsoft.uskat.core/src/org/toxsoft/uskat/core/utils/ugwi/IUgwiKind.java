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

  @SuppressWarnings( "javadoc" )
  default ValidationResult validateUgwi( String aEssence ) {
    return validateUgwi( EMPTY_STRING, aEssence );
  }

}
