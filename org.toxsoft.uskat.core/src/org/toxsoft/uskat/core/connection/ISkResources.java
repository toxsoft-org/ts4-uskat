package org.toxsoft.uskat.core.connection;

/**
 * Localizable resources.
 *
 * @author hazard157
 */
@SuppressWarnings( "nls" )
interface ISkResources {

  /**
   * {@link ESkAuthentificationType}
   */
  String STR_N_SAT_NONE   = Messages.getString( "STR_N_SAT_NONE" );   //$NON-NLS-1$
  String STR_D_SAT_NONE   = Messages.getString( "STR_D_SAT_NONE" );   //$NON-NLS-1$
  String STR_N_SAT_SIMPLE = Messages.getString( "STR_N_SAT_SIMPLE" ); //$NON-NLS-1$
  String STR_D_SAT_SIMPLE = Messages.getString( "STR_D_SAT_SIMPLE" ); //$NON-NLS-1$

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
  String STR_LOGIN      = Messages.getString( "STR_LOGIN" );      //$NON-NLS-1$
  String STR_LOGIN_D    = Messages.getString( "STR_LOGIN_D" );    //$NON-NLS-1$
  String STR_PASSWORD   = Messages.getString( "STR_PASSWORD" );   //$NON-NLS-1$
  String STR_PASSWORD_D = Messages.getString( "STR_PASSWORD_D" ); //$NON-NLS-1$
  String STR_ROLE       = Messages.getString( "STR_ROLE" );       //$NON-NLS-1$
  String STR_ROLE_D     = Messages.getString( "STR_ROLE_D" );     //$NON-NLS-1$

}
