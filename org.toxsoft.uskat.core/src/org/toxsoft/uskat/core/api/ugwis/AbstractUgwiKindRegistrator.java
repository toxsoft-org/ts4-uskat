package org.toxsoft.uskat.core.api.ugwis;

import static org.toxsoft.uskat.core.api.ugwis.ISkResources.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;

/**
 * Basic implementation of {@link IUgwiKind}.
 *
 * @author hazard157
 * @param <T> - the UGWI content type
 */
public non-sealed abstract class AbstractUgwiKindRegistrator<T>
    extends StridableParameterized
    implements ISkUgwiKindRegistrator {

  /**
   * Constructor.
   *
   * @param aId String - the ID (IDpath)
   * @param aParams {@link IOptionSet} - {@link #params()} initial values
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath
   */
  public AbstractUgwiKindRegistrator( String aId, IOptionSet aParams ) {
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

  @Override
  final public Ugwi createUgwi( String aNamespace, String aEssence ) {
    TsValidationFailedRtException.checkError( validateUgwi( aNamespace, aEssence ) );
    return Ugwi.of( id(), aNamespace, aEssence );
  }

  // ------------------------------------------------------------------------------------
  // class API
  //

  /**
   * Creates UWGI kind bound to the specified Sk-connection.
   *
   * @param aCoreApi {@link ISkCoreApi} - the core API
   * @return {@link AbstractUgwiKind} - created kind
   */
  public AbstractUgwiKind<?> createUgwiKind( ISkCoreApi aCoreApi ) {
    TsNullArgumentRtException.checkNull( aCoreApi );
    return doCreateUgwiKind( aCoreApi );
  }

  // ------------------------------------------------------------------------------------
  // API for subclasses
  //

  /**
   * Creates and returns result indicating general error of invalid essence format for this UGWI kind.
   *
   * @param aEssence String - the invalid essence
   * @return {@link ValidationResult} - result to be returned by {@link #doValidateEssence(String)}
   */
  public ValidationResult makeGeneralInvalidEssenceVr( String aEssence ) {
    return ValidationResult.error( MSG_ERR_INV_ESSENCE_FOR_KIND, id(), aEssence );
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
   * Implementation must perform check of UGWI essence for syntactical validity.
   *
   * @param aEssence String - the essence, never is <code>null</code>, may be an empty string
   * @return {@link Object} - found (created) entity or <code>null</code>
   */
  protected abstract ValidationResult doValidateEssence( String aEssence );

  /**
   * Creates UWGI kind bound to the specified Sk-connection.
   *
   * @param aCoreApi {@link ISkCoreApi} - the core API
   * @return {@link AbstractUgwiKind} - created kind
   */
  protected abstract AbstractUgwiKind<?> doCreateUgwiKind( ISkCoreApi aCoreApi );

}
