package org.toxsoft.uskat.users.gui.km5;

/**
 * Localizable resources.
 *
 * @author hazard157
 */
interface ISkResources {

  String FMT_ERR_PROFILE_ALREADY_EXISTS      = Messages.getString( "FMT_ERR_PROFILE_ALREADY_EXISTS" );      //$NON-NLS-1$
  String STR_ERR_CANT_REMOVE_DEFAULT_PROFILE = Messages.getString( "STR_ERR_CANT_REMOVE_DEFAULT_PROFILE" ); //$NON-NLS-1$
  String STR_ABILITY                         = Messages.getString( "STR_ABILITY" );                         //$NON-NLS-1$
  String STR_ABILITY_NAME                    = Messages.getString( "STR_ABILITY_NAME" );                    //$NON-NLS-1$

  /**
   * Общие
   */
  String MSG_ERR_NO_CONNECTION = Messages.getString( "MSG_ERR_NO_CONNECTION" ); //$NON-NLS-1$

  /**
   * {@link SkUserM5LifecycleManager}
   */
  String FMT_ERR_DUP_LOGIN                 = Messages.getString( "FMT_ERR_DUP_LOGIN" );                 //$NON-NLS-1$
  String FMT_ERR_CREATING_NON_ACTIVE       = Messages.getString( "FMT_ERR_CREATING_NON_ACTIVE" );       //$NON-NLS-1$
  String FMT_ERR_DEACT_SELF                = Messages.getString( "FMT_ERR_DEACT_SELF" );                //$NON-NLS-1$
  String FMT_ERR_DEACT_LAST_ACTIVE_USER    = Messages.getString( "FMT_ERR_DEACT_LAST_ACTIVE_USER" );    //$NON-NLS-1$
  String FMT_ERR_REMOVE_SELF               = Messages.getString( "FMT_ERR_REMOVE_SELF" );               //$NON-NLS-1$
  String FMT_ERR_REMOVING_LAST_ACTIVE_USER = Messages.getString( "FMT_ERR_REMOVING_LAST_ACTIVE_USER" ); //$NON-NLS-1$
  String MSG_ERR_IMPROPER_PASSWORD         = Messages.getString( "MSG_ERR_IMPROPER_PASSWORD" );         //$NON-NLS-1$
  String MSG_ERR_ROLE_MUST_BE              = "необходимо выбрать хотя бы одну роль";

  /**
   * {@link SkRoleM5LifecycleManager}
   */
  String FMT_ERR_REMOVING_LAST_ACTIVE_ROLE = "Нельзя удалить последнюю активную роль: %s";

  /**
   * {@link SkUserM5Model}
   */
  String STR_N_USER          = Messages.getString( "STR_N_USER" );          //$NON-NLS-1$
  String STR_D_USER          = Messages.getString( "STR_D_USER" );          //$NON-NLS-1$
  String STR_N_FDEF_NAME     = Messages.getString( "STR_N_FDEF_NAME" );     //$NON-NLS-1$
  String STR_D_FDEF_NAME     = Messages.getString( "STR_D_FDEF_NAME" );     //$NON-NLS-1$
  String STR_N_FDEF_LOGIN    = Messages.getString( "STR_N_FDEF_LOGIN" );    //$NON-NLS-1$
  String STR_D_FDEF_LOGIN    = Messages.getString( "STR_D_FDEF_LOGIN" );    //$NON-NLS-1$
  String STR_N_FDEF_PASSWORD = Messages.getString( "STR_N_FDEF_PASSWORD" ); //$NON-NLS-1$
  String STR_D_FDEF_PASSWORD = Messages.getString( "STR_D_FDEF_PASSWORD" ); //$NON-NLS-1$
  String STR_N_FDEF_ACTIVE   = Messages.getString( "STR_N_FDEF_ACTIVE" );   //$NON-NLS-1$
  String STR_D_FDEF_ACTIVE   = Messages.getString( "STR_D_FDEF_ACTIVE" );   //$NON-NLS-1$
  String STR_N_FDEF_HIDDEN   = Messages.getString( "STR_N_FDEF_HIDDEN" );   //$NON-NLS-1$
  String STR_D_FDEF_HIDDEN   = Messages.getString( "STR_D_FDEF_HIDDEN" );   //$NON-NLS-1$
  String STR_N_FDEF_DESCR    = Messages.getString( "STR_N_FDEF_DESCR" );    //$NON-NLS-1$
  String STR_D_FDEF_DESCR    = Messages.getString( "STR_D_FDEF_DESCR" );    //$NON-NLS-1$
  String STR_N_HIDDEN        = Messages.getString( "STR_N_HIDDEN" );        //$NON-NLS-1$
  String STR_D_HIDDEN        = Messages.getString( "STR_D_HIDDEN" );        //$NON-NLS-1$

  /**
   * {@link SkRoleM5Model}
   */
  String STR_N_ROLE    = "роль";
  String STR_D_ROLE    = "Набор прав доступа в системе";
  String STR_N_FDEF_ID = "id";
  String STR_D_FDEF_ID = "идентификатор роли";

}
