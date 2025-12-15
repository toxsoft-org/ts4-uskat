package org.toxsoft.uskat.s5.server.backend.supports.commands.impl;

/**
 * Локализуемые ресурсы реализации.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IS5Resources {

  String STR_D_BACKEND_COMMANDS = Messages.getString( "IS5Resources.STR_D_BACKEND_CURRDATA" );
  String STR_D_COMMANDS_FACTORY = Messages.getString( "IS5Resources.STR_D_COMMANDS_FACTORY" );

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_READ_COMMANDS                       = Messages.getString( "IS5Resources.MSG_READ_COMMANDS" );
  String MSG_NEW_STATE                           = Messages.getString( "IS5Resources.MSG_NEW_STATE" );
  String MSG_DOJOB                               = Messages.getString( "IS5Resources.MSG_DOJOB" );
  String MSG_SET_EXECUTABLE_CMDS                 = Messages.getString( "IS5Resources.MSG_SET_EXECUTABLE_CMDS" );
  String MSG_SESSION_EXECUTABLES_CHANGED         =
      "setHandledCommandGwids(...): frontend = %s. executors changed (%d).";
  String MSG_EXECUTABLES_REMOVED                 = "\nexecutors were removed (%d):%s";
  String MSG_EXECUTABLES_ADDED                   = "\nexecutors were added (%d):%s";
  String MSG_SESSION_EXECUTABLES_ARE_NOT_CHANGED =
      "setHandledCommandGwids(...): frontend = %s. executors are not changed.";
  String MSG_CMD_EXECUTOR                        = Messages.getString( "IS5Resources.MSG_CMD_EXECUTOR" );

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_ON_COMMANDS                   = Messages.getString( "IS5Resources.ERR_ON_COMMANDS" );
  String ERR_EXECUTOR_EXIST                = Messages.getString( "IS5Resources.ERR_EXECUTOR_EXIST" );
  String ERR_EXECUTOR_DOUBLE_REGISTER      = Messages.getString( "IS5Resources.ERR_EXECUTOR_DOUBLE_REGISTER" );
  String ERR_EXECUTOR_NOT_FOUND            = Messages.getString( "IS5Resources.ERR_EXECUTOR_NOT_FOUND" );
  String ERR_COMMAND_NOT_FOUND             = Messages.getString( "IS5Resources.ERR_COMMAND_NOT_FOUND" );
  String ERR_WRONG_NEW_STATE               = Messages.getString( "IS5Resources.ERR_WRONG_NEW_STATE" );
  String ERR_FRONTEND_DOES_NOT_SUPPORT_CMD =
      "Failed to test command %s. The frontend does not support executing commands (frontend.commands = null)";
  String ERR_TEST_FAILED_BY_TIMEOUT        = "Failed to test command %s by timeout = %d";
  String ERR_UNKNOWN_TEST_COMMAND          = "Unknown test cmd result recevied. aInstanceId = %s";

  String REASON_EXEC_BY_QUERY      = Messages.getString( "IS5Resources.REASON_EXEC_BY_QUERY" );
  String REASON_EXECUTOR_NOT_FOUND = Messages.getString( "IS5Resources.REASON_EXECUTOR_NOT_FOUND" );
  String REASON_CANCEL_BY_TIMEOUT  = Messages.getString( "IS5Resources.REASON_CANCEL_BY_TIMEOUT" );
  String REASON_SEND_AND_EXEC      = Messages.getString( "IS5Resources.REASON_SEND_AND_EXEC" );
  String REASON_SEND_AND_CANCEL    = Messages.getString( "IS5Resources.REASON_SEND_AND_CANCEL" );
}
