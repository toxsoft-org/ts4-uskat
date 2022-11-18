package org.toxsoft.uskat.s5.client.remote;

/**
 * Константы, локализуемые ресурсы .
 *
 * @author mvk
 */
interface IS5Resources {

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String STR_N_BACKEND_ADDONS = Messages.getString( "IS5Resources.STR_N_BACKEND_ADDONS" ); //$NON-NLS-1$
  String STR_D_BACKEND_ADDONS = Messages.getString( "IS5Resources.STR_D_BACKEND_ADDONS" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_BACKEND_NOT_INIT                  = Messages.getString( "IS5Resources.ERR_BACKEND_NOT_INIT" );               //$NON-NLS-1$
  String ERR_NO_CONNECTION                     = Messages.getString( "IS5Resources.ERR_NO_CONNECTION" );                  //$NON-NLS-1$
  String ERR_BACKEND_ADDON_NOT_FOUND           = Messages.getString( "IS5Resources.ERR_BA_SESSION_NOT_FOUND" );           //$NON-NLS-1$
  String ERR_ON_EVENTS                         = Messages.getString( "IS5Resources.ERR_ON_EVENTS" );                      //$NON-NLS-1$
  String ERR_NOT_FOUND_BACKEND_ADDONS_PROVIDER =
      Messages.getString( "IS5Resources.ERR_NOT_FOUND_BACKEND_ADDONS_PROVIDER" );                                         //$NON-NLS-1$
  String ERR_TRY_LOCK                          = Messages.getString( "IS5Resources.ERR_TRY_LOCK" );                       //$NON-NLS-1$
  String ERR_ADDONS_NOT_FOUND                  = Messages.getString( "IS5Resources.ERR_ADDONS_NOT_FOUND" );               //$NON-NLS-1$
  String ERR_BA_CREATOR_NOT_FOUND              =
      "createBackendAddons(...): backend addon creator class is not found (client classpath). addonId = %s, baClass= %s"; //$NON-NLS-1$
  String ERR_NOT_FOUND_INIT_IMPL_CONSTRUCTOR   =
      Messages.getString( "createBackendAddons(...): class = %s. constructor not found. cause = %s." );                   //$NON-NLS-1$
  String ERR_NOT_FOUND_INIT_IMPL_INSTANTIATION =
      Messages.getString( "createBackendAddons(...): class = %s. instantiation error.  cause = %s." );                    //$NON-NLS-1$
  String ERR_NOT_FOUND_INIT_IMPL_UNEXPECTED    =
      Messages.getString( "createBackendAddons(...): class = %s. unexpected error. cause = %s." );                        //$NON-NLS-1$

}
