package org.toxsoft.uskat.onews.lib;

/**
 * Localizable resources.
 *
 * @author hazard157
 */
interface ISkResources {

  /**
   * {@link EOwsPermission}
   */
  String STR_N_OP_DENY  = Messages.getString( "STR_N_OP_DENY" );  //$NON-NLS-1$
  String STR_D_OP_DENY  = Messages.getString( "STR_D_OP_DENY" );  //$NON-NLS-1$
  String STR_N_OP_ALLOW = Messages.getString( "STR_N_OP_ALLOW" ); //$NON-NLS-1$
  String STR_D_OP_ALLOW = Messages.getString( "STR_D_OP_ALLOW" ); //$NON-NLS-1$

  /**
   * {@link OneWsRule}
   */
  String STR_N_RULE_ALLOW_ALL = Messages.getString( "STR_N_RULE_ALLOW_ALL" ); //$NON-NLS-1$
  String STR_N_RULE_DENY_ALL  = Messages.getString( "STR_N_RULE_DENY_ALL" );  //$NON-NLS-1$

  /**
   * {@link IOneWsConstants}
   */
  String STR_N_KIND_ID             = Messages.getString( "STR_N_KIND_ID" );             //$NON-NLS-1$
  String STR_D_KIND_ID             = Messages.getString( "STR_D_KIND_ID" );             //$NON-NLS-1$
  String STR_N_KIND_PERSPECTIVE    = Messages.getString( "STR_N_KIND_PERSPECTIVE" );    //$NON-NLS-1$
  String STR_D_KIND_PERSPECTIVE    = Messages.getString( "STR_D_KIND_PERSPECTIVE" );    //$NON-NLS-1$
  String STR_N_KIND_ACTION         = Messages.getString( "STR_N_KIND_ACTION" );         //$NON-NLS-1$
  String STR_D_KIND_ACTION         = Messages.getString( "STR_D_KIND_ACTION" );         //$NON-NLS-1$
  String STR_N_PROFILE_ATTR_RULES  = Messages.getString( "STR_N_PROFILE_ATTR_RULES" );  //$NON-NLS-1$
  String STR_D_PROFILE_ATTR_RULES  = Messages.getString( "STR_N_PROFILE_ATTR_RULES" );  //$NON-NLS-1$
  String STR_N_PROFILE_ATTR_PARAMS = Messages.getString( "STR_N_PROFILE_ATTR_PARAMS" ); //$NON-NLS-1$
  String STR_D_PROFILE_ATTR_PARAMS = Messages.getString( "STR_N_PROFILE_ATTR_PARAMS" ); //$NON-NLS-1$
  String STR_N_PROFILE_ATTR_ROLES  = Messages.getString( "STR_N_PROFILE_ATTR_ROLES" );  //$NON-NLS-1$
  String STR_D_PROFILE_ATTR_ROLES  = Messages.getString( "STR_N_PROFILE_ATTR_ROLES" );  //$NON-NLS-1$

}
