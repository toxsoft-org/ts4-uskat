package org.toxsoft.uskat.regref.lib.impl;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoAttrInfo;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoLinkInfo;
import org.toxsoft.uskat.regref.lib.*;

/**
 * Реализация встроенных правил валидации редактирования НСИ.
 *
 * @author goga
 */
class SkRriSectionDefaultValidator
    implements ISkRriSectionValidator {

  // TODO проверить, что при создании оюъекта компаниона заданы ВСЕ атрибуты (новые ачения или по умолчанию)

  private final SkRriSection rriSection;

  public SkRriSectionDefaultValidator( SkRriSection aOwner ) {
    rriSection = aOwner;
  }

  @Override
  public ValidationResult canSetSectionParams( ISkRriSection aSection, String aName, String aDescription,
      IOptionSet aParams ) {
    // TODO canSetSectionParams()
    return ValidationResult.SUCCESS;
  }

  @Override
  public ValidationResult canDefineAttrParam( String aClassId, IDtoAttrInfo aAttrDef ) {
    // TODO canDefineAttrParam()
    return ValidationResult.SUCCESS;
  }

  @Override
  public ValidationResult canDefineLinkParam( String aClassId, IDtoLinkInfo aLinkDef ) {
    // TODO canDefineLinkParam()
    return ValidationResult.SUCCESS;
  }

  @Override
  public ValidationResult canRemoveParam( String aClassId, String aParamId ) {
    // TODO canRemoveParam()
    return ValidationResult.SUCCESS;
  }

  @Override
  public ValidationResult canRemoveAll( String aClassId ) {
    // TODO canRemoveAll()
    return ValidationResult.SUCCESS;
  }

  @Override
  public ValidationResult canSetAttrParamValue( Skid aObjId, String aParamId, IAtomicValue aValue, String aReason ) {
    // TODO canSetAttrParamValue()
    return ValidationResult.SUCCESS;
  }

  @Override
  public ValidationResult canSetLinkParamValue( Skid aObjId, String aParamId, ISkidList aObjIds, String aReason ) {
    // TODO canSetLinkParamValue()
    return ValidationResult.SUCCESS;
  }

  @Override
  public ValidationResult canSetParamValues( ISkRriParamValues aValues, String aReason ) {
    // TODO canSetParamValues()
    return ValidationResult.SUCCESS;
  }

}
