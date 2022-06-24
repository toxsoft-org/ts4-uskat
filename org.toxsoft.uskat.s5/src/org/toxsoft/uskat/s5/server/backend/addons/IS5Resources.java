package org.toxsoft.uskat.s5.server.backend.addons;

/**
 * Локализуемые ресурсы реализации.
 *
 * @author mvk
 */
interface IS5Resources {

  String STR_N_BACKEND_REGREF = Messages.getString( "IS5Resources.STR_N_BACKEND_REGREF" ); //$NON-NLS-1$
  String STR_D_BACKEND_REGREF = Messages.getString( "IS5Resources.STR_D_BACKEND_REGREF" ); //$NON-NLS-1$

  String STR_N_BACKEND_REFBOOK = Messages.getString( "IS5Resources.STR_N_BACKEND_REFBOOK" ); //$NON-NLS-1$
  String STR_D_BACKEND_REFBOOK = Messages.getString( "IS5Resources.STR_D_BACKEND_REFBOOK" ); //$NON-NLS-1$

  String STR_N_BACKEND_MNEMO = Messages.getString( "IS5Resources.STR_N_BACKEND_MNEMO" ); //$NON-NLS-1$
  String STR_D_BACKEND_MNEMO = Messages.getString( "IS5Resources.STR_D_BACKEND_MNEMO" ); //$NON-NLS-1$

  String STR_N_BACKEND_ONEWS = Messages.getString( "IS5Resources.STR_N_BACKEND_ONEWS" ); //$NON-NLS-1$
  String STR_D_BACKEND_ONEWS = Messages.getString( "IS5Resources.STR_D_BACKEND_ONEWS" ); //$NON-NLS-1$

  String STR_N_BACKEND_GPREFS = Messages.getString( "IS5Resources.STR_N_BACKEND_GPREFS" ); //$NON-NLS-1$
  String STR_D_BACKEND_GPREFS = Messages.getString( "IS5Resources.STR_D_BACKEND_GPREFS" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_NO_CONNECTION           = Messages.getString( "IS5Resources.ERR_NO_CONNECTION" );           //$NON-NLS-1$
  String ERR_BA_SESSION_NOT_FOUND = Messages.getString( "IS5Resources.ERR_BA_SESSION_NOT_FOUND" ); //$NON-NLS-1$
  String ERR_SESSION_NOT_FOUND       = Messages.getString( "IS5Resources.ERR_SESSION_NOT_FOUND" );       //$NON-NLS-1$
  String ERR_TRY_LOCK                = Messages.getString( "IS5Resources.ERR_TRY_LOCK" );                //$NON-NLS-1$

  String ERR_ADDON_IMPL_NOT_FOUND              =
      "createBackendAddons(...): addon class not found (client classpath). addonId = %s, addonClass= %s"; //$NON-NLS-1$
  String ERR_NOT_FOUND_INIT_IMPL_CONSTRUCTOR   =
      Messages.getString( "createBackendAddons(...): class = %s. constructor not found. cause = %s." );   //$NON-NLS-1$
  String ERR_NOT_FOUND_INIT_IMPL_INSTANTIATION =
      Messages.getString( "createBackendAddons(...): class = %s. instantiation error.  cause = %s." );    //$NON-NLS-1$
  String ERR_NOT_FOUND_INIT_IMPL_UNEXPECTED    =
      Messages.getString( "createBackendAddons(...): class = %s. unexpected error. cause = %s." );        //$NON-NLS-1$

}
