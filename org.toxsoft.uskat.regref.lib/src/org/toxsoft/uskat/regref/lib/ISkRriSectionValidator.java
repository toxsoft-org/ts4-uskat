package org.toxsoft.uskat.regref.lib;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoAttrInfo;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoLinkInfo;

/**
 * {@link ISkRriSection} methods pre-validation.
 * <p>
 * Note: {@link ISkRegRefInfoService} validation is implemented in per-section basis in
 * {@link ISkRegRefInfoServiceValidator}.
 *
 * @author goga
 */
public interface ISkRriSectionValidator {

  /**
   * Checks if section parameters can be changed.
   *
   * @param aSection ISkRriSection - the section
   * @param aName String - название раздела
   * @param aDescription String - описание раздела
   * @param aParams {@link IOptionSet} - значения параметров {@link ISkRriSection#params()}
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canSetSectionParams( ISkRriSection aSection, String aName, String aDescription, IOptionSet aParams );

  // TODO проверка с помощью ISkRriSectionValidator (self атрибут?, есть такая связь?, тип атрибута и т.п.)
  ValidationResult canDefineAttrParam( String aClassId, IDtoAttrInfo aAttrDef );

  // TODO проверка с помощью ISkRriSectionValidator (self связь?, есть такой атрибут?, параметры связи и т.п.)
  ValidationResult canDefineLinkParam( String aClassId, IDtoLinkInfo aLinkDef );

  ValidationResult canRemoveParam( String aClassId, String aParamId );

  ValidationResult canRemoveAll( String aClassId );

  ValidationResult canSetAttrParamValue( Skid aObjId, String aParamId, IAtomicValue aValue, String aReason );

  ValidationResult canSetLinkParamValue( Skid aObjId, String aParamId, ISkidList aObjIds, String aReason );

  ValidationResult canSetParamValues( ISkRriParamValues aValues, String aReason );

}
