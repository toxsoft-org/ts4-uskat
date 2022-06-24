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

  /**
   * Checks if user can be created.
   *
   * @param aUserDto {@link IDtoFullObject} - user data including links
   * @return {@link ValidationResult} - validation result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canCreateUser( IDtoFullObject aUserDto );

  /**
   * Checks if user can be edited.
   *
   * @param aUserDto {@link IDtoFullObject} - user data including links
   * @param aOldUser {@link ISkUser} - current user data
   * @return {@link ValidationResult} - validation result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canEditUser( IDtoFullObject aUserDto, ISkUser aOldUser );

  /**
   * Checks if user may removed.
   *
   * @param aLogin String - user login
   * @return {@link ValidationResult} - validation result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canRemoveUser( String aLogin );

  /**
   * Checks if user can be created.
   *
   * @param aRoleDto {@link IDtoObject} - role data
   * @return {@link ValidationResult} - validation result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canCreateRole( IDtoObject aRoleDto );

  /**
   * Checks if role can be edited
   *
   * @param aRoleDto {@link IDtoObject} - role data
   * @param aOldRole {@link ISkRole} - current role data
   * @return {@link ValidationResult} - validation result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canEditRole( IDtoObject aRoleDto, ISkRole aOldRole );

  /**
   * Check if role can be removed.
   *
   * @param aRoleId String - the role ID (that is {@link ISkRole#id()})
   * @return {@link ValidationResult} - validation result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canRemoveRole( String aRoleId );

}
