package org.toxsoft.uskat.users.gui;

import org.toxsoft.uskat.users.gui.km5.*;

/**
 * Localizable resources.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface ISkUsersGuiSharedResources {

  /**
   * {@link SkUserM5Model}
   */
  String STR_N_USER        = Messages.getString( "STR_N_USER" );        //$NON-NLS-1$
  String STR_D_USER        = Messages.getString( "STR_D_USER" );        //$NON-NLS-1$
  String STR_N_FDEF_NAME   = Messages.getString( "STR_N_FDEF_NAME" );   //$NON-NLS-1$
  String STR_D_FDEF_NAME   = Messages.getString( "STR_D_FDEF_NAME" );   //$NON-NLS-1$
  String STR_N_FDEF_LOGIN  = Messages.getString( "STR_N_FDEF_LOGIN" );  //$NON-NLS-1$
  String STR_D_FDEF_LOGIN  = Messages.getString( "STR_D_FDEF_LOGIN" );  //$NON-NLS-1$
  String STR_N_FDEF_ACTIVE = Messages.getString( "STR_N_FDEF_ACTIVE" ); //$NON-NLS-1$
  String STR_D_FDEF_ACTIVE = Messages.getString( "STR_D_FDEF_ACTIVE" ); //$NON-NLS-1$
  String STR_N_FDEF_HIDDEN = Messages.getString( "STR_N_FDEF_HIDDEN" ); //$NON-NLS-1$
  String STR_D_FDEF_HIDDEN = Messages.getString( "STR_D_FDEF_HIDDEN" ); //$NON-NLS-1$
  String STR_N_FDEF_DESCR  = Messages.getString( "STR_N_FDEF_DESCR" );  //$NON-NLS-1$
  String STR_D_FDEF_DESCR  = Messages.getString( "STR_D_FDEF_DESCR" );  //$NON-NLS-1$

  /**
   * {@link SkRoleM5Model}
   */
  String STR_N_ROLE    = "Роль";
  String STR_D_ROLE    = "Набор прав доступа в системе";
  String STR_N_ROLE_ID = "ИД роли";
  String STR_D_ROLE_ID = "Идентификатор роли";

  /**
   * {@link SkUserMpc}
   */
  String STR_N_TMI_BY_ROLES = "По ролям";
  String STR_D_TMI_BY_ROLES = "Группировка пользователей по ролям";

  /**
   * {@link ISkUsersGuiConstants}
   */
  String STR_N_NO_HIDDEN_USERS = "Скрыть";
  String STR_D_NO_HIDDEN_USERS = "Не показывать скрытых пользователей";
  String STR_N_CHANGE_PASSWORD = "Пароль";
  String STR_D_CHANGE_PASSWORD = "Показ диалога и ввод нового пароля пароля с подтверждением";

}
