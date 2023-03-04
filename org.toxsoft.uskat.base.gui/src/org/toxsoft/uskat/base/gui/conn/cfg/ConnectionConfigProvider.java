package org.toxsoft.uskat.base.gui.conn.cfg;

import static org.toxsoft.uskat.core.impl.ISkCoreConfigConstants.*;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.metainf.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * Implementation of {@link IConnectionConfigProvider}.
 *
 * @author hazard157
 */
public non-sealed class ConnectionConfigProvider
    implements IConnectionConfigProvider {

  private final IOptionSet                    params;
  private final IStridablesListEdit<IDataDef> opDefs = new StridablesList<>();

  private final ISkBackendProvider backendProvider;
  private final ISkBackendMetaInfo backInfo;

  /**
   * Constructor.
   *
   * @param aSource {@link ISkBackendProvider} - underlying backend provider
   * @param aParams {@link IOptionSet} - values of {@link #params()}
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public ConnectionConfigProvider( ISkBackendProvider aSource, IOptionSet aParams ) {
    TsNullArgumentRtException.checkNulls( aSource, aParams );
    backendProvider = aSource;
    backInfo = backendProvider.getMetaInfo();
    params = new OptionSet( aParams );
    // define options
    opDefs.addAll( ISkCoreConfigConstants.ALL_SK_CORE_CFG_PARAMS );
    opDefs.addAll( backInfo.argOps() );
    opDefs.addAll( backendMetaInfo().getAuthentificationType().authentificationOptionDefs() );

    // TODO REFDEF_BACKEND_PROVIDER
    // TODO REFDEF_USER_SERVICES
    // TODO REFDEF_THREAD_SEPARATOR

  }

  // ------------------------------------------------------------------------------------
  // IStridable
  //

  @Override
  public String id() {
    return backInfo.id();
  }

  @Override
  public String nmName() {
    return backInfo.nmName();
  }

  @Override
  public String description() {
    return backInfo.description();
  }

  // ------------------------------------------------------------------------------------
  // IParameterized
  //

  @Override
  public IOptionSet params() {
    return params;
  }

  @Override
  public ISkBackendMetaInfo backendMetaInfo() {
    return backInfo;
  }

  @Override
  public IStridablesList<IDataDef> opDefs() {
    return opDefs;
  }

  @Override
  public ValidationResult validateOpValues( IOptionSet aOpValues ) {
    TsNullArgumentRtException.checkNull( aOpValues );
    // validate against options definitions
    ValidationResult vr = OptionSetUtils.validateOptionSet( aOpValues, opDefs );
    if( vr.isError() ) {
      return vr;
    }
    // additional validation
    return ValidationResult.firstNonOk( vr, doValidate( aOpValues ) );
  }

  @Override
  public void fillArgs( ITsContext aSkConnArgs, IOptionSet aOptions ) {
    TsNullArgumentRtException.checkNull( aSkConnArgs );
    TsValidationFailedRtException.checkError( validateOpValues( aOptions ) );
    // all options
    for( IDataDef opdef : opDefs ) {
      if( aOptions.hasValue( opdef.id() ) ) {
        aSkConnArgs.params().setValue( opdef, aOptions.getValue( opdef ) );
      }
      else {
        aSkConnArgs.params().setValue( opdef, opdef.defaultValue() );
      }
    }
    // known references
    REFDEF_BACKEND_PROVIDER.setRef( aSkConnArgs, backendProvider );
    // additional processing
    doProcessArgs( aSkConnArgs );
  }

  // ------------------------------------------------------------------------------------
  // To override
  //

  /**
   * The subclass may perform additional checks on option values.
   * <p>
   * At the time of the call, it is checked that the set has all the mandatory options and that the type of all values
   * is compatible with the option atomic type from {@link #opDefs()}.
   * <p>
   * Base class simply returns {@link ValidationResult#SUCCESS}, when overridden, there is no need to call the parent
   * method.
   *
   * @param aOpValues {@link IOptionSet} - values of the options listed in {@link #opDefs()}
   * @return {@link ValidationResult} - validation result
   */
  protected ValidationResult doValidate( IOptionSet aOpValues ) {
    return ValidationResult.SUCCESS;
  }

  /**
   * The subclass may perform additional processing of the Sk-connection opening arguments.
   * <p>
   * Base class does nothing, there is no need to call the parent method when overriding.
   *
   * @param aSkConnArgs {@link ITsContext} - editable context that will be the argument to open the connection
   */
  protected void doProcessArgs( ITsContext aSkConnArgs ) {
    // nop
  }

}
