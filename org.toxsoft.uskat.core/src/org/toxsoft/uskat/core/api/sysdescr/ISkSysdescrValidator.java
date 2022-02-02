package org.toxsoft.uskat.core.api.sysdescr;

import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoClassInfo;

/**
 * {@link ISkSysdescr} service validator.
 *
 * @author hazard157
 */
public interface ISkSysdescrValidator {

  /**
   * Checks if new class can be created.
   *
   * @param aNewClassInfo {@link IDtoClassInfo} - information of the new class
   * @return {@link ValidationResult} - the validation result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canCreateClass( IDtoClassInfo aNewClassInfo );

  /**
   * Checks if the existing class may be changed.
   *
   * @param aNewClassInfo {@link IDtoClassInfo} - new information of the class
   * @param aOldClassInfo {@link ISkClassInfo} - existing information of the class
   * @return {@link ValidationResult} - the validation result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canEditClass( IDtoClassInfo aNewClassInfo, ISkClassInfo aOldClassInfo );

  /**
   * Checks if class may be removed (deleted).
   *
   * @param aClassId String - ID of the class to be deleted
   * @return {@link ValidationResult} - the validation result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canRemoveClass( String aClassId );

}
