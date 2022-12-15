package org.toxsoft.uskat.regref.lib.impl;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.bricks.validator.impl.AbstractTsValidationSupport;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoAttrInfo;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoLinkInfo;
import org.toxsoft.uskat.regref.lib.*;

/**
 * Вспомогательный класс для реализации {@link ISkRriSection#svs()}.
 *
 * @author goga
 */
class SkRriSectionValidationSupport
    extends AbstractTsValidationSupport<ISkRriSectionValidator>
    implements ISkRriSectionValidator {

  @Override
  public ISkRriSectionValidator validator() {
    return this;
  }

  @Override
  public ValidationResult canSetSectionParams( ISkRriSection aSection, String aName, String aDescription,
      IOptionSet aParams ) {
    TsNullArgumentRtException.checkNulls( aSection, aName, aDescription, aParams );
    ValidationResult vr = ValidationResult.SUCCESS;
    for( ISkRriSectionValidator v : validatorsList() ) {
      vr = ValidationResult.firstNonOk( vr, v.canSetSectionParams( aSection, aName, aDescription, aParams ) );
    }
    return vr;
  }

  @Override
  public ValidationResult canDefineAttrParam( String aClassId, IDtoAttrInfo aAttrDef ) {
    TsNullArgumentRtException.checkNulls( aClassId, aAttrDef );
    ValidationResult vr = ValidationResult.SUCCESS;
    for( ISkRriSectionValidator v : validatorsList() ) {
      vr = ValidationResult.firstNonOk( vr, v.canDefineAttrParam( aClassId, aAttrDef ) );
    }
    return vr;
  }

  @Override
  public ValidationResult canDefineLinkParam( String aClassId, IDtoLinkInfo aLinkDef ) {
    TsNullArgumentRtException.checkNulls( aClassId, aLinkDef );
    ValidationResult vr = ValidationResult.SUCCESS;
    for( ISkRriSectionValidator v : validatorsList() ) {
      vr = ValidationResult.firstNonOk( vr, v.canDefineLinkParam( aClassId, aLinkDef ) );
    }
    return vr;
  }

  @Override
  public ValidationResult canRemoveParam( String aClassId, String aParamId ) {
    TsNullArgumentRtException.checkNulls( aClassId, aParamId );
    ValidationResult vr = ValidationResult.SUCCESS;
    for( ISkRriSectionValidator v : validatorsList() ) {
      vr = ValidationResult.firstNonOk( vr, v.canRemoveParam( aClassId, aParamId ) );
    }
    return vr;
  }

  @Override
  public ValidationResult canRemoveAll( String aClassId ) {
    TsNullArgumentRtException.checkNulls( aClassId );
    ValidationResult vr = ValidationResult.SUCCESS;
    for( ISkRriSectionValidator v : validatorsList() ) {
      vr = ValidationResult.firstNonOk( vr, v.canRemoveAll( aClassId ) );
    }
    return vr;
  }

  @Override
  public ValidationResult canSetAttrParamValue( Skid aObjId, String aParamId, IAtomicValue aValue, String aReason ) {
    TsNullArgumentRtException.checkNulls( aObjId, aParamId, aValue, aReason );
    ValidationResult vr = ValidationResult.SUCCESS;
    for( ISkRriSectionValidator v : validatorsList() ) {
      vr = ValidationResult.firstNonOk( vr, v.canSetAttrParamValue( aObjId, aParamId, aValue, aReason ) );
    }
    return vr;
  }

  @Override
  public ValidationResult canSetLinkParamValue( Skid aObjId, String aParamId, ISkidList aObjIds, String aReason ) {
    TsNullArgumentRtException.checkNulls( aObjId, aParamId, aObjIds, aReason );
    ValidationResult vr = ValidationResult.SUCCESS;
    for( ISkRriSectionValidator v : validatorsList() ) {
      vr = ValidationResult.firstNonOk( vr, v.canSetLinkParamValue( aObjId, aParamId, aObjIds, aReason ) );
    }
    return vr;
  }

  @Override
  public ValidationResult canSetParamValues( ISkRriParamValues aValues, String aReason ) {
    TsNullArgumentRtException.checkNulls( aValues, aReason );
    ValidationResult vr = ValidationResult.SUCCESS;
    for( ISkRriSectionValidator v : validatorsList() ) {
      vr = ValidationResult.firstNonOk( vr, v.canSetParamValues( aValues, aReason ) );
    }
    return vr;
  }
}
