package org.toxsoft.uskat.s5.server.singletons;

/**
 * Константы, локализуемые ресурсы .
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IS5Resources {

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_STRING_FORMAT     = Messages.getString( "IS5Resources.MSG_STRING_FORMAT" );
  String MSG_INIT_SINGLETON    = Messages.getString( "IS5Resources.MSG_INIT_SINGLETON" );
  String MSG_CLOSE_SINGLETON   = Messages.getString( "IS5Resources.MSG_CLOSE_SINGLETON" );
  String MSG_START_DO_JOB      = Messages.getString( "IS5Resources.MSG_START_DO_JOB" );
  String MSG_DOJOB             = Messages.getString( "IS5Resources.MSG_DOJOB" );
  String MSG_STOP_DO_JOB       = Messages.getString( "IS5Resources.MSG_STOP_DO_JOB" );
  String MSG_ADD_SERVER_JOB    = Messages.getString( "IS5Resources.MSG_ADD_SERVER_JOB" );
  String MSG_REMOVE_SERVER_JOB = Messages.getString( "IS5Resources.MSG_REMOVE_SERVER_JOB" );
  String MSG_GC                = Messages.getString( "IS5Resources.MSG_GC" );
  String MSG_GC_START          = Messages.getString( "IS5Resources.MSG_GC_START" );
  String MSG_GC_FINISH         = Messages.getString( "IS5Resources.MSG_GC_FINISH" );

  String ERR_REMOVING_CONSTANT_NOT_SPECIFIED_IN_PATH =
      "initConfiguration(...): removing constant not specified in path. id = %s";
  String ERR_REDEFINING_CONSTANT_VALUE               =
      "setConfigurationConstantValue(...): redefining the value of a configuration constant. id = %s, prevValue = %s, newValue = %s";

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String MSG_ERR_SERVER_JOB               = Messages.getString( "IS5Resources.MSG_ERR_SERVER_JOB" );
  String MSG_ERR_SERVER_JOB_ALREADY_EXIST = Messages.getString( "IS5Resources.MSG_ERR_SERVER_JOB_ALREADY_EXIST" );
  String MSG_ERR_SERVER_CONFIG_ROLLBACK   = Messages.getString( "IS5Resources.MSG_ERR_SERVER_CONFIG_ROLLBACK" );

  String MSG_ERR_EXECUTOR_NOT_FOUND       = Messages.getString( "IS5Resources.MSG_ERR_EXECUTOR_NOT_FOUND" );
  String MSG_ERR_SEQUENCE_CACHE_NOT_FOUND = Messages.getString( "IS5Resources.MSG_ERR_SEQUENCE_CACHE_NOT_FOUND" );
  String MSG_ERR_WRONG_CONFIG_TYPE        = Messages.getString( "IS5Resources.MSG_ERR_WRONG_CONFIG_TYPE" );
  String MSG_ERR_EXECUTOR_UNEXPECTED      = "try lookup %s. unexpected error: %s";

  String MSG_ERR_REGISTER_TRANSACTION = Messages.getString( "IS5Resources.MSG_ERR_REGISTER_TRANSACTION" );
  String MSG_ERR_METHOD_NOT_FOUND     = Messages.getString( "IS5Resources.MSG_ERR_METHOD_NOT_FOUND" );
}
