package org.toxsoft.uskat.sysext.realtime.supports.commands.impl;

/**
 * Локализуемые ресурсы реализации.
 *
 * @author mvk
 */
interface IS5Resources {

  String STR_D_BACKEND_COMMANDS = Messages.getString( "IS5Resources.STR_D_BACKEND_COMMANDS" ); //$NON-NLS-1$
  String STR_D_COMMANDS_FACTORY = Messages.getString( "IS5Resources.STR_D_COMMANDS_FACTORY" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_READ_COMMANDS       = Messages.getString( "IS5Resources.MSG_READ_COMMANDS" );       //$NON-NLS-1$
  String MSG_NEW_STATE           = Messages.getString( "IS5Resources.MSG_NEW_STATE" );           //$NON-NLS-1$
  String MSG_DOJOB               = Messages.getString( "IS5Resources.MSG_DOJOB" );               //$NON-NLS-1$
  String MSG_SET_EXECUTABLE_CMDS = Messages.getString( "IS5Resources.MSG_SET_EXECUTABLE_CMDS" ); //$NON-NLS-1$
  String MSG_CMD_EXECUTOR        = Messages.getString( "IS5Resources.MSG_CMD_EXECUTOR" );        //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_ON_COMMANDS              = Messages.getString( "IS5Resources.ERR_ON_COMMANDS" );              //$NON-NLS-1$
  String ERR_EXECUTOR_EXIST           = Messages.getString( "IS5Resources.ERR_EXECUTOR_EXIST" );           //$NON-NLS-1$
  String ERR_EXECUTOR_DOUBLE_REGISTER = Messages.getString( "IS5Resources.ERR_EXECUTOR_DOUBLE_REGISTER" ); //$NON-NLS-1$
  String ERR_EXECUTOR_NOT_FOUND       = Messages.getString( "IS5Resources.ERR_EXECUTOR_NOT_FOUND" );       //$NON-NLS-1$
  String ERR_COMMAND_NOT_FOUND        = Messages.getString( "IS5Resources.ERR_COMMAND_NOT_FOUND" );        //$NON-NLS-1$
  String ERR_WRONG_NEW_STATE          = Messages.getString( "IS5Resources.ERR_WRONG_NEW_STATE" );          //$NON-NLS-1$

  String REASON_EXEC_BY_QUERY      = Messages.getString( "IS5Resources.REASON_EXEC_BY_QUERY" );      //$NON-NLS-1$
  String REASON_EXECUTOR_NOT_FOUND = Messages.getString( "IS5Resources.REASON_EXECUTOR_NOT_FOUND" ); //$NON-NLS-1$
  String REASON_CANCEL_BY_TIMEOUT  = Messages.getString( "IS5Resources.REASON_CANCEL_BY_TIMEOUT" );  //$NON-NLS-1$
  String REASON_SEND_AND_EXEC      = Messages.getString( "IS5Resources.REASON_SEND_AND_EXEC" );      //$NON-NLS-1$
  String REASON_SEND_AND_CANCEL    = Messages.getString( "IS5Resources.REASON_SEND_AND_CANCEL" );    //$NON-NLS-1$
}
