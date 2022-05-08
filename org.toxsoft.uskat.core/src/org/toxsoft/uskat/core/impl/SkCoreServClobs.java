package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.backend.ISkBackendHardConstant.*;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.clobserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.devapi.*;

/**
 * {@link ISkClobService} implementation.
 *
 * @author hazard157
 */
class SkCoreServClobs
    extends AbstractSkCoreService
    implements ISkClobService {

  /**
   * Service creator singleton.
   */
  public static final ISkServiceCreator<AbstractSkService> CREATOR = SkCoreServClobs::new;

  /**
   * The service validator {@link ISkObjectService#svs()} implementation.
   *
   * @author hazard157
   */
  static class ValidationSupport
      extends AbstractTsValidationSupport<ISkClobServiceValidator>
      implements ISkClobServiceValidator {

    @Override
    public ISkClobServiceValidator validator() {
      // TODO реализовать SkCoreServClobs.ValidationSupport.validator()
      throw new TsUnderDevelopmentRtException( "SkCoreServClobs.ValidationSupport.validator()" );
    }

    // ------------------------------------------------------------------------------------
    // ISkClobServiceValidator
    //

    @Override
    public ValidationResult canWriteClob( Gwid aGwid, String aClob ) {
      TsNullArgumentRtException.checkNulls( aGwid, aClob );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkClobServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canWriteClob( aGwid, aClob ) );
      }
      return vr;
    }

  }

  private final ISkClobServiceValidator builtinValidator = ( aGwid, aClob ) -> {
    // TODO check if GWID is valid
    if( aGwid.kind() != EGwidKind.GW_CLOB ) {

    }
    if( aGwid.isAbstract() || aGwid.isMulti() ) {

    }
    // TODO check if CLOB size is less than ISkBackendHardConstant.OPDEF_SKBI_MAX_CLOB_LENGTH
    int maxLen = OPDEF_SKBI_MAX_CLOB_LENGTH.getValue( coreApi().openArgs().params() ).asInt();
    if( aClob.length() > maxLen ) {

    }
    return ValidationResult.SUCCESS;
  };

  final ValidationSupport validationSupport = new ValidationSupport();

  /**
   * Constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  SkCoreServClobs( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );
    validationSupport.addValidator( builtinValidator );
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkService
  //

  @Override
  protected void doInit( ITsContextRo aArgs ) {
    // nop
  }

  @Override
  protected void doClose() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  // ------------------------------------------------------------------------------------
  // ISkClobService
  //

  @Override
  public String readClob( Gwid aGwid ) {
    TsIllegalArgumentRtException.checkNull( aGwid );
    TsIllegalArgumentRtException.checkTrue( aGwid.isAbstract() || aGwid.isMulti() );
    TsIllegalArgumentRtException.checkTrue( aGwid.kind() != EGwidKind.GW_CLOB );

    // TODO реализовать SkCoreServClobs.readClob()
    throw new TsUnderDevelopmentRtException( "SkCoreServClobs.readClob()" );
  }

  @Override
  public void writeClob( Gwid aGwid, String aClob ) {
    TsValidationFailedRtException.checkError( validationSupport.canWriteClob( aGwid, aClob ) );

    // TODO реализовать SkCoreServClobs.writeClob()
    throw new TsUnderDevelopmentRtException( "SkCoreServClobs.writeClob()" );
  }

  @Override
  public ITsValidationSupport<ISkClobServiceValidator> svs() {
    return validationSupport;
  }

}
