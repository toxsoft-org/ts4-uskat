package org.toxsoft.uskat.s5.server.backend.addons;

/**
 * Локализуемые ресурсы реализации.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IS5Resources {

  String STR_N_BACKEND_REGREF = Messages.getString( "IS5Resources.STR_N_BACKEND_REGREF" );
  String STR_D_BACKEND_REGREF = Messages.getString( "IS5Resources.STR_D_BACKEND_REGREF" );

  String STR_N_BACKEND_REFBOOK = Messages.getString( "IS5Resources.STR_N_BACKEND_REFBOOK" );
  String STR_D_BACKEND_REFBOOK = Messages.getString( "IS5Resources.STR_D_BACKEND_REFBOOK" );

  String STR_N_BACKEND_MNEMO = Messages.getString( "IS5Resources.STR_N_BACKEND_MNEMO" );
  String STR_D_BACKEND_MNEMO = Messages.getString( "IS5Resources.STR_D_BACKEND_MNEMO" );

  String STR_N_BACKEND_ONEWS = Messages.getString( "IS5Resources.STR_N_BACKEND_ONEWS" );
  String STR_D_BACKEND_ONEWS = Messages.getString( "IS5Resources.STR_D_BACKEND_ONEWS" );

  String STR_N_BACKEND_GPREFS = Messages.getString( "IS5Resources.STR_N_BACKEND_GPREFS" );
  String STR_D_BACKEND_GPREFS = Messages.getString( "IS5Resources.STR_D_BACKEND_GPREFS" );

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_CHANGE_DEFAULT_LOGGER  =
      "S5AbstractBackend::run(): change default logger: LoggerUtils.setDefaultLogger( %s )";
  String MSG_RESTORE_DEFAULT_LOGGER =
      "S5AbstractBackend::run(): restore default logger:  LoggerUtils.setDefaultLogger( %s )";

  String MSG_CHANGE_ERROR_LOGGER  =
      "S5AbstractBackend::run(): change default error logger: LoggerUtils.setErrorLogger( %s )";
  String MSG_RESTORE_ERROR_LOGGER =
      "S5AbstractBackend::run(): restore default error logger: LoggerUtils.setErrorLogger( %s )";
  String MSG_DOJOB                = "S5AbstractBackend.run(...)";
  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_NO_CONNECTION              = Messages.getString( "IS5Resources.ERR_NO_CONNECTION" );
  String ERR_BA_SESSION_NOT_FOUND       = Messages.getString( "IS5Resources.ERR_BA_SESSION_NOT_FOUND" );
  String ERR_SESSION_NOT_FOUND          = Messages.getString( "IS5Resources.ERR_SESSION_NOT_FOUND" );
  String ERR_TRY_LOCK                   = Messages.getString( "IS5Resources.ERR_TRY_LOCK" );
  String ERR_NEED_THREAD_SEPARATOR      =
      "You need to define a thread separator between frontend and backend threads (ISkCoreConfigConstants.REFDEF_THREAD_EXECUTOR)";
  String ERR_RUN_CANT_GET_FRONTEND_LOCK = "run(...): can't get frontendLock in %d (msec)";
}
