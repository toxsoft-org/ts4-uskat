package org.toxsoft.uskat.core.backend.metainf;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.backend.metainf.ISkResources.*;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * {@link ISkBackendMetaInfo} implementation.
 * <p>
 * Class may be instantiated directly or subclass may be created to perform additional checks in
 * {@link #doCheckArguments(ITsContextRo)}.
 *
 * @author hazard157
 */
public non-sealed class SkBackendMetaInfo
    extends Stridable
    implements ISkBackendMetaInfo {

  private final IStridablesListEdit<IDataDef>       opDefs  = new StridablesList<>();
  private final IStringMapEdit<ITsContextRefDef<?>> refDefs = new StringMap<>();
  private final ESkAuthentificationType             authentificationType;

  /**
   * Constructor.
   *
   * @param aId String - provider ID (an IDpath)
   * @param aName String - short name
   * @param aDescription String - description
   * @param aAuthType {@link ESkAuthentificationType} - required authentification type
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath
   */
  public SkBackendMetaInfo( String aId, String aName, String aDescription, ESkAuthentificationType aAuthType ) {
    super( aId, aName, aDescription );
    TsNullArgumentRtException.checkNull( aAuthType );
    authentificationType = aAuthType;
  }

  // ------------------------------------------------------------------------------------
  // ISkBackendMetaInfo
  //

  @Override
  final public IStridablesListEdit<IDataDef> argOps() {
    return opDefs;
  }

  @Override
  final public IStringMapEdit<ITsContextRefDef<?>> argRefs() {
    return refDefs;
  }

  @Override
  final public ESkAuthentificationType getAuthentificationType() {
    return authentificationType;
  }

  @Override
  public ValidationResult checkOptions( IOptionSet aArgOptions ) {
    ValidationResult vr = OptionSetUtils.validateOptionSet( aArgOptions, opDefs );
    if( vr.isError() ) {
      return vr;
    }
    return ValidationResult.firstNonOk( vr, doCheckOptions( aArgOptions ) );
  }

  @Override
  final public ValidationResult checkArguments( ITsContextRo aArgs ) {
    TsNullArgumentRtException.checkNull( aArgs );
    // check options
    ValidationResult vr = checkOptions( aArgs.params() );
    if( vr.isError() ) {
      return vr;
    }
    // check mandatory references present
    for( ITsContextRefDef<?> rd : refDefs ) {
      if( rd.isMandatory() && !aArgs.hasKey( rd.refKey() ) ) {
        String rdName = DDEF_NAME.getValue( rd.params() ).asString();
        return ValidationResult.error( FMT_ERR_NO_MANDATORY_REF, rd.refKey(), rdName );
      }
    }
    // check references values against defined types
    for( ITsContextRefDef<?> rd : refDefs ) {
      if( aArgs.hasKey( rd.refKey() ) ) {
        Object ref = aArgs.get( rd.refKey() );
        Class<?> expectedType = rd.refClass();
        Class<?> realType = ref.getClass();
        if( !expectedType.isAssignableFrom( realType ) ) {
          return ValidationResult.error( FMT_ERR_REF_TYPE_MISMATCH, rd.refKey(), expectedType.getSimpleName(),
              realType.getSimpleName() );
        }
      }
    }
    return ValidationResult.firstNonOk( vr, doCheckArguments( aArgs ) );
  }

  // ------------------------------------------------------------------------------------
  // To override
  //

  /**
   * The subclass may perform additional checks in {@link #checkOptions(IOptionSet)}.
   * <p>
   * <code>aArgs</code> is already checked that has all mandatory options and references of the valid type. Only
   * backend-specific additional checks have to be performed here.
   * <p>
   * In the base class simply returns {@link ValidationResult#SUCCESS}, there is no need to call superclass method when
   * overriding.
   *
   * @param aArgOptions {@link IOptionSet} - the arguments options
   * @return {@link ValidationResult} - the check result
   */
  protected ValidationResult doCheckOptions( IOptionSet aArgOptions ) {
    return ValidationResult.SUCCESS;
  }

  /**
   * The subclass may perform additional checks in {@link #checkArguments(ITsContextRo)}.
   * <p>
   * <code>aArgs</code> is already checked by {@link #checkOptions(IOptionSet)}.
   * <p>
   * In the base class simply returns {@link ValidationResult#SUCCESS}, there is no need to call superclass method when
   * overriding.
   *
   * @param aArgs {@link ITsContextRo} - the arguments
   * @return {@link ValidationResult} - the check result
   */
  protected ValidationResult doCheckArguments( ITsContextRo aArgs ) {
    return ValidationResult.SUCCESS;
  }

}
