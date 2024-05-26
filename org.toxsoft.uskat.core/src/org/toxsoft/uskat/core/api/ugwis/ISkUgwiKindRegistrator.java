package org.toxsoft.uskat.core.api.ugwis;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;

import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * UGWI kind registrator handles the UGWI syntactic checks and registers {@link IUgwiKind} for Sk-connections.
 * <p>
 * Registrator implementations may add {@link Ugwi} instance creation methods with the kind-specific arguments.
 * <p>
 * The {@link #id()} is the same as {@link IUgwiKind#id()} and means the UGWI kind ID {@link Ugwi#kindId()}.
 *
 * @author hazard157
 */
public sealed interface ISkUgwiKindRegistrator
    extends IStridableParameterized
    permits AbstractUgwiKindRegistrator {

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
   * Creates UGWI of this kind.
   *
   * @param aNamespace String - the namespace
   * @param aEssence String - the essence
   * @return {@link Ugwi} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed {@link #validateUgwi(String, String)}
   */
  Ugwi createUgwi( String aNamespace, String aEssence );

  // ------------------------------------------------------------------------------------
  // Inline methods for convenience

  /**
   * Validates if UGWI (with the empty namespace) essence is valid for this kind.
   *
   * @param aEssence String - the essence
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  default ValidationResult validateUgwi( String aEssence ) {
    return validateUgwi( EMPTY_STRING, aEssence );
  }

  /**
   * Creates UGWI (with the empty namespace) of this kind.
   *
   * @param aEssence String - the essence
   * @return {@link Ugwi} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed {@link #validateUgwi(String, String)}
   */
  default Ugwi createUgwi( String aEssence ) {
    return createUgwi( EMPTY_STRING, aEssence );
  }

}
