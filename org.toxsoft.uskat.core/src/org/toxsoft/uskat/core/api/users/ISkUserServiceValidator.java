package org.toxsoft.uskat.core.api.users;

import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.uskat.core.api.objserv.*;

/**
 * {@link ISkUserService} service validator.
 *
 * @author hazard157
 */
public interface ISkUserServiceValidator {

  ValidationResult canCreateUser( IDtoObject aUserDpu );

  ValidationResult canEditUser( IDtoObject aUserDpu, ISkUser aOldUser );

  ValidationResult canRemoveUser( String aLogin );

  ValidationResult canSetPassword( ISkUser aUser, String aPassword );

}
