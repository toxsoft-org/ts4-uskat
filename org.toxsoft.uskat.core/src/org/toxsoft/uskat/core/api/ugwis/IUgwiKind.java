package org.toxsoft.uskat.core.api.ugwis;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;

import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;

/**
 * The UGWI kind description.
 * <p>
 * This interface is to be implemented by the Sk-connection independent kind singleton while {@link ISkUgwiKind} extends
 * it with Sk-connection bound abilities.
 * <p>
 * The {@link #id()} is the same as {@link ISkUgwiKind#id()} and means the UGWI kind ID {@link Ugwi#kindId()}.
 * <p>
 * To be usable, created instances must be registered via {@link SkUgwiUtils#registerUgwiKind(AbstractUgwiKind)}.
 *
 * @author hazard157
 */
public sealed interface IUgwiKind
    extends IStridableParameterized
    permits AbstractUgwiKind {

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

  /**
   * Determines if entity addresses by the UGWI actually exists in the particular USkat.
   * <p>
   * For {@link Ugwi#NONE} returns false.
   *
   * @param aUgwi {@link Ugwi} - the UGWI
   * @param aCoreApi {@link ISkCoreApi} - USKat API
   * @return boolean - the entity existence flag<br>
   *         <b>true</b> - addressed entity exists in <code>aCoreApi</code>;<br>
   *         <b>false</b> - argument addresses nothing in <code>aCoreApi</code>.
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException argument is not a valid UGWI of this kind
   */
  boolean isSkEntity( Ugwi aUgwi, ISkCoreApi aCoreApi );

  /**
   * Determines if entity addresses by this kind of UGWI also may be addressed by a GWID. <
   *
   * @return boolean - the sign that this kind of UGWI points to the GWID addressed Sk-entity<br>
   *         <b>true</b> - this is an alias to GWID, method {@link #getGwid(Ugwi)} returns valid GWID;<br>
   *         <b>false</b> - this is non-GWID entity, {@link #getGwid(Ugwi)} throws an exception.
   */
  boolean hasGwid();

  /**
   * Returns the GWID address of the Sk-entity addressed by this UGWI, if supported.
   *
   * @param aUgwi {@link Ugwi} - the UGWI to get GWID from
   * @return {@link Gwid} - the GWID alias of this UGWI
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException argument is {@link Ugwi#NONE}
   * @throws TsUnsupportedFeatureRtException this UGWI kind does supports UWGI aliases
   * @throws TsValidationFailedRtException argument is not a valid UGWI of this kind
   */
  Gwid getGwid( Ugwi aUgwi );

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
