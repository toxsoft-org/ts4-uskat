package org.toxsoft.uskat.regref.lib;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * {@link ISkRegRefInfoService} methods pre-validation.
 * <p>
 * Note: {@link ISkRriSection} validation is implemented in per-section basis in {@link ISkRriSectionValidator}.
 *
 * @author goga
 */
public interface ISkRegRefInfoServiceValidator {

  /**
   * Checks if section can be created.
   *
   * @param aSectionId String - identifier of the section to be created
   * @param aName String - название раздела
   * @param aDescription String - описание раздела
   * @param aParams {@link IOptionSet} - the values of the parameters
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canCreateSection( String aSectionId, String aName, String aDescription, IOptionSet aParams );

  /**
   * Checks if sections can be removed.
   *
   * @param aSectionId String - identifier of the section to be removed
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canRemoveSection( String aSectionId );

}
