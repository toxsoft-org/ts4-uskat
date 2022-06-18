package org.toxsoft.uskat.core.api.users;

import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;

/**
 * {@link ISkUserService} service validator.
 *
 * @author hazard157
 */
public interface ISkUserServiceValidator {

  ValidationResult canCreateUser( IDtoObject aUserDpu );

  /**
   * @param aUserDpu
   * @param aOldUser
   * @return {@link ValidationResult} - validation result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canEditUser( IDtoObject aUserDpu, ISkUser aOldUser );

  /**
   * @param aLogin
   * @return {@link ValidationResult} - validation result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canRemoveUser( String aLogin );

  /**
   * @param aRoleDpu
   * @return {@link ValidationResult} - validation result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canCreateRole( IDtoObject aRoleDpu );

  /**
   * @param aRoleDpu
   * @param aOldRole
   * @return {@link ValidationResult} - validation result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canEditRole( IDtoObject aRoleDpu, ISkRole aOldRole );

  /**
   * Check if role can be removed.
   *
   * @param aRoleId String - th role ID (that is {@link ISkRole#id()})
   * @return {@link ValidationResult} - validation result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canRemoveRole( String aRoleId );

}
