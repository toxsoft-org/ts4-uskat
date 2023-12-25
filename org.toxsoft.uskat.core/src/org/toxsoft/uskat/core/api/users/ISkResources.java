package org.toxsoft.uskat.core.api.users;

/**
 * Localizable resources.
 *
 * @author mvk
 */
interface ISkResources {

  /**
   * {@link ISkUserServiceHardConstants}
   */
  // String STR_N_PASSWORD = Messages.getString( "STR_N_PASSWORD" ); //$NON-NLS-1$
  // String STR_D_PASSWORD = Messages.getString( "STR_D_PASSWORD" ); //$NON-NLS-1$
  // String STR_N_ROLES = Messages.getString( "STR_N_ROLES" ); //$NON-NLS-1$
  // String STR_D_ROLES = Messages.getString( "STR_D_ROLES" ); //$NON-NLS-1$
  // String STR_N_USER_IS_ENABLED = Messages.getString( "STR_N_USER_IS_ENABLED" ); //$NON-NLS-1$
  // String STR_D_USER_IS_ENABLED = Messages.getString( "STR_D_USER_IS_ENABLED" ); //$NON-NLS-1$
  // String STR_N_USER_IS_HIDDEN = Messages.getString( "STR_N_USER_IS_HIDDEN" ); //$NON-NLS-1$
  // String STR_D_USER_IS_HIDDEN = Messages.getString( "STR_D_USER_IS_HIDDEN" ); //$NON-NLS-1$
  // String STR_N_ROLE_IS_ENABLED = Messages.getString( "STR_N_ROLE_IS_ENABLED" ); //$NON-NLS-1$
  // String STR_D_ROLE_IS_ENABLED = Messages.getString( "STR_D_ROLE_IS_ENABLED" ); //$NON-NLS-1$
  // String STR_N_ROLE_IS_HIDDEN = Messages.getString( "STR_N_ROLE_IS_HIDDEN" ); //$NON-NLS-1$
  // String STR_D_ROLE_IS_HIDDEN = Messages.getString( "STR_D_ROLE_IS_HIDDEN" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // USkat entities are defined only in English, l10n done via USkat localization service

  String STR_ROLE              = "Role";                                                                       //$NON-NLS-1$
  String STR_ROLE_D            = "The role - Role - a set of user access rights to system functions and data"; //$NON-NLS-1$
  String STR_ROLE_IS_ENABLED   = "Enabled?";                                                                   //$NON-NLS-1$
  String STR_ROLE_IS_ENABLED_D = "Sign of permission to login the system with this role";                      //$NON-NLS-1$
  String STR_ROLE_IS_HIDDEN    = "Hidden?";                                                                    //$NON-NLS-1$
  String STR_ROLE_IS_HIDDEN_D  = "Sign of hiding the role in the lists of normal administration";              //$NON-NLS-1$
  String STR_USER              = "User";                                                                       //$NON-NLS-1$
  String STR_USER_D            = "A user or program module that has the right to login to the system";         //$NON-NLS-1$
  String STR_PASSWORD          = "Password";                                                                   //$NON-NLS-1$
  String STR_PASSWORD_D        = "User password (more precisely, a hash code for verification)";               //$NON-NLS-1$
  String STR_USER_IS_ENABLED   = "Enabled?";                                                                   //$NON-NLS-1$
  String STR_USER_IS_ENABLED_D = "Sign of user permission to login the system";                                //$NON-NLS-1$
  String STR_USER_IS_HIDDEN    = "Hidden?";                                                                    //$NON-NLS-1$
  String STR_USER_IS_HIDDEN_D  = "Sign of hiding the user in the lists of normal administration";              //$NON-NLS-1$
  String STR_ALLOWED_ROLES     = "Roles";                                                                      //$NON-NLS-1$
  String STR_ALLOWED_ROLES_D   = "Roles that a user can log in";                                               //$NON-NLS-1$

}
