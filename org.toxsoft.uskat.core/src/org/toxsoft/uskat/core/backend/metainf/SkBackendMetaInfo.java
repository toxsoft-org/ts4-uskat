package org.toxsoft.uskat.core.backend.metainf;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.backend.metainf.ISkResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.errors.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.backend.*;

/**
 * {@link ISkBackendMetaInfo} implementation.
 * <p>
 * Class may be instantiated directly or subclass may be created to perform additional checks in
 * {@link #doCheckArguments(ITsContext)}.
 *
 * @author hazard157
 */
public non-sealed class SkBackendMetaInfo
    extends Stridable
    implements ISkBackendMetaInfo {

  private final IStridablesListEdit<IDataDef>       ops  = new StridablesList<>();
  private final IStringMapEdit<ITsContextRefDef<?>> refs = new StringMap<>();
  private final ISkBackendProvider                  provider;

  /**
   * Constructor.
   *
   * @param aId String - provider ID (an IDpath)
   * @param aName String - short name
   * @param aDescription String - description
   * @param aProvider {@link ISkBackendProvider} - this backend provider
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath
   */
  public SkBackendMetaInfo( String aId, String aName, String aDescription, ISkBackendProvider aProvider ) {
    super( aId, aName, aDescription );
    TsNullArgumentRtException.checkNull( aProvider );
    provider = aProvider;
  }

  // ------------------------------------------------------------------------------------
  // ISkBackendMetaInfo
  //

  @Override
  final public IStridablesListEdit<IDataDef> argOps() {
    return ops;
  }

  @Override
  final public IStringMapEdit<ITsContextRefDef<?>> argRefs() {
    return refs;
  }

  @Override
  final public ValidationResult checkArguments( ITsContext aArgs ) {
    TsNullArgumentRtException.checkNull( aArgs );
    // check mandatory option present
    for( IDataDef dd : ops ) {
      if( dd.isMandatory() && !aArgs.params().hasKey( dd.id() ) ) {
        return ValidationResult.error( FMT_ERR_NO_MANDATORY_OP, dd.id(), dd.nmName() );
      }
    }
    // check mandatory references present
    for( ITsContextRefDef<?> rd : refs ) {
      if( rd.isMandatory() && !aArgs.hasKey( rd.refKey() ) ) {
        String rdName = DDEF_NAME.getValue( rd.params() ).asString();
        return ValidationResult.error( FMT_ERR_NO_MANDATORY_REF, rd.refKey(), rdName );
      }
    }
    // check option values against defined types
    for( IDataDef dd : ops ) {
      if( aArgs.params().hasKey( dd.id() ) ) {
        IAtomicValue opVal = aArgs.params().getValue( dd.id() );
        if( AvTypeCastRtException.canAssign( dd.atomicType(), opVal.atomicType() ) ) {
          return ValidationResult.error( FMT_ERR_OP_TYPE_MISMATCH, dd.id(), dd.atomicType().id(),
              opVal.atomicType().id() );
        }
      }
    }
    // check references values against defined types
    for( ITsContextRefDef<?> rd : refs ) {
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
    return doCheckArguments( aArgs );
  }

  @Override
  public ISkBackendProvider provider() {
    return provider;
  }

  // ------------------------------------------------------------------------------------
  // To override
  //

  /**
   * The subclass may perform additional checks in {@link #checkArguments(ITsContext)}.
   * <p>
   * <code>aArgs</code> is already checked that has all mandatory options and references of the valid type. Only
   * backend-specific additional checks have to be performed here.
   * <p>
   * In the base class simply returns {@link ValidationResult#SUCCESS}, there is no need to call superclass method when
   * overriding.
   *
   * @param aArgs {@link ITsContext} - the arguments
   * @return {@link ValidationResult} - the check result
   */
  protected ValidationResult doCheckArguments( ITsContext aArgs ) {
    return ValidationResult.SUCCESS;
  }

}
