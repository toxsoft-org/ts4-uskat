package org.toxsoft.uskat.core.utils.ugwi;

import static org.toxsoft.uskat.core.utils.ugwi.ITsResources.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Base implementation of {@link IUgwiKind}.
 *
 * @author hazard157
 */
public non-sealed class UgwiKind
    extends StridableParameterized
    implements IUgwiKind {

  /**
   * Constructor.
   *
   * @param aId String - the ID (IDpath)
   * @param aParams {@link IOptionSet} - {@link #params()} initial values
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath
   */
  public UgwiKind( String aId, IOptionSet aParams ) {
    super( aId, aParams );
  }

  // ------------------------------------------------------------------------------------
  // IUgwiKind
  //

  @Override
  final public ValidationResult validateUgwi( Ugwi aUgwi ) {
    TsNullArgumentRtException.checkNull( aUgwi );
    if( aUgwi == Ugwi.NONE ) {
      return ValidationResult.SUCCESS;
    }
    if( !aUgwi.kindId().equals( id() ) ) {
      return ValidationResult.error( FMT_ERR_UWGI_NOT_OF_THIS_KIND, aUgwi.kindId(), id() );
    }
    return validateUgwi( aUgwi.namespace(), aUgwi.essence() );
  }

  @Override
  final public ValidationResult validateUgwi( String aNamespace, String aEssence ) {
    TsNullArgumentRtException.checkNulls( aNamespace, aEssence );
    ValidationResult vr = ValidationResult.SUCCESS;
    if( !aNamespace.isEmpty() ) {
      if( !StridUtils.isValidIdPath( aNamespace ) ) {
        return ValidationResult.error( MSG_ERR_UGWI_NAMESPACE_NOT_IDPATH );
      }
      vr = doValidateNamespace( aNamespace );
      if( vr.isError() ) {
        return vr;
      }
    }
    return ValidationResult.firstNonOk( vr, doValidateEssence( aEssence ) );
  }

  // ------------------------------------------------------------------------------------
  // To override/implement
  //

  /**
   * Implementation may perform additional check of UGWI namespace for syntactical validity.
   * <p>
   * In the base class returns {@link ValidationResult#SUCCESS}, there is no need to call superclass method when
   * overriding.
   *
   * @param aNamespace String - the namespace, valid IDpath, not an empty string
   * @return {@link Object} - found (created) entity or <code>null</code>
   */
  protected ValidationResult doValidateNamespace( String aNamespace ) {
    return ValidationResult.SUCCESS;
  }

  /**
   * Implementation may perform additional check of UGWI essence for syntactical validity.
   * <p>
   * In the base class returns {@link ValidationResult#SUCCESS}, there is no need to call superclass method when
   * overriding.
   *
   * @param aEssence String - the essence, never is <code>null</code>, may be an empty string
   * @return {@link Object} - found (created) entity or <code>null</code>
   */
  protected ValidationResult doValidateEssence( String aEssence ) {
    return ValidationResult.SUCCESS;
  }

}
