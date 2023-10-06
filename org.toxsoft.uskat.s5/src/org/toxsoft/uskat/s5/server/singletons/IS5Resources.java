package org.toxsoft.uskat.s5.server.singletons;

/**
 * Константы, локализуемые ресурсы .
 *
 * @author mvk
 */
interface IS5Resources {

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_STRING_FORMAT     = Messages.getString( "IS5Resources.MSG_STRING_FORMAT" );     //$NON-NLS-1$
  String MSG_INIT_SINGLETON    = Messages.getString( "IS5Resources.MSG_INIT_SINGLETON" );    //$NON-NLS-1$
  String MSG_CLOSE_SINGLETON   = Messages.getString( "IS5Resources.MSG_CLOSE_SINGLETON" );   //$NON-NLS-1$
  String MSG_START_DO_JOB      = Messages.getString( "IS5Resources.MSG_START_DO_JOB" );      //$NON-NLS-1$
  String MSG_DOJOB             = Messages.getString( "IS5Resources.MSG_DOJOB" );             //$NON-NLS-1$
  String MSG_STOP_DO_JOB       = Messages.getString( "IS5Resources.MSG_STOP_DO_JOB" );       //$NON-NLS-1$
  String MSG_ADD_SERVER_JOB    = Messages.getString( "IS5Resources.MSG_ADD_SERVER_JOB" );    //$NON-NLS-1$
  String MSG_REMOVE_SERVER_JOB = Messages.getString( "IS5Resources.MSG_REMOVE_SERVER_JOB" ); //$NON-NLS-1$
  String MSG_GC                = Messages.getString( "IS5Resources.MSG_GC" );                //$NON-NLS-1$
  String MSG_GC_START          = Messages.getString( "IS5Resources.MSG_GC_START" );          //$NON-NLS-1$
  String MSG_GC_FINISH         = Messages.getString( "IS5Resources.MSG_GC_FINISH" );         //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String MSG_ERR_SERVER_JOB               = Messages.getString( "IS5Resources.MSG_ERR_SERVER_JOB" );               //$NON-NLS-1$
  String MSG_ERR_SERVER_JOB_ALREADY_EXIST = Messages.getString( "IS5Resources.MSG_ERR_SERVER_JOB_ALREADY_EXIST" ); //$NON-NLS-1$
  String MSG_ERR_SERVER_CONFIG_ROLLBACK   = Messages.getString( "IS5Resources.MSG_ERR_SERVER_CONFIG_ROLLBACK" );   //$NON-NLS-1$

  String MSG_ERR_EXECUTOR_NOT_FOUND       = Messages.getString( "IS5Resources.MSG_ERR_EXECUTOR_NOT_FOUND" );       //$NON-NLS-1$
  String MSG_ERR_SEQUENCE_CACHE_NOT_FOUND = Messages.getString( "IS5Resources.MSG_ERR_SEQUENCE_CACHE_NOT_FOUND" ); //$NON-NLS-1$
  String MSG_ERR_WRONG_CONFIG_TYPE        = Messages.getString( "IS5Resources.MSG_ERR_WRONG_CONFIG_TYPE" );        //$NON-NLS-1$
  String MSG_ERR_EXECUTOR_UNEXPECTED      = "try lookup %s. unexpected error: %s";                                 //$NON-NLS-1$

  String MSG_ERR_REGISTER_TRANSACTION = Messages.getString( "IS5Resources.MSG_ERR_REGISTER_TRANSACTION" ); //$NON-NLS-1$
  String MSG_ERR_METHOD_NOT_FOUND     = Messages.getString( "IS5Resources.MSG_ERR_METHOD_NOT_FOUND" );     //$NON-NLS-1$
}
