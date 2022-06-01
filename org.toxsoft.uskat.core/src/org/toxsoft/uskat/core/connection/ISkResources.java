package org.toxsoft.uskat.core.connection;

/**
 * Localizable resources.
 *
 * @author hazard157
 */
@SuppressWarnings( "nls" )
interface ISkResources {

  /**
   * {@link ESkConnState}
   */
  String STR_N_CLOSED   = "Closed";
  String STR_D_CLOSED   = "Connection is closed (was not opened or already closed)";
  String STR_N_INACTIVE = "Inactive";
  String STR_D_INACTIVE = "Connection is open but temporary there is no contact with the server";
  String STR_N_ACTIVE   = "Active";
  String STR_D_ACTIVE   = "Connection is open and active";

  /**
   * {@link ISkConnectionConstants}
   */
  String STR_N_LOGIN    = "Login";
  String STR_D_LOGIN    = "The user login name";
  String STR_N_PASSWORD = "Password";
  String STR_D_PASSWORD = "The user password hash string";
  String STR_N_ROLES    = "Roles";
  String STR_D_ROLES    = "The user roles IDs list";

}
