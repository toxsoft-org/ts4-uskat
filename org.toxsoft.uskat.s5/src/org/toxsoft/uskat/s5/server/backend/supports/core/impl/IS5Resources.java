package org.toxsoft.uskat.s5.server.backend.supports.core.impl;

/**
 * Константы, локализуемые ресурсы .
 *
 * @author mvk
 */
interface IS5Resources {

  String STR_D_BACKEND_CORE = Messages.getString( "IS5Resources.STR_D_BACKEND_CORE" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_CREATE_BACKEND = Messages.getString( "IS5Resources.MSG_CREATE_BACKEND" ); //$NON-NLS-1$
  String MSG_INIT_BACKEND   = Messages.getString( "IS5Resources.MSG_INIT_BACKEND" );   //$NON-NLS-1$
  String MSG_CLOSE_BACKEND  = Messages.getString( "IS5Resources.MSG_CLOSE_BACKEND" );  //$NON-NLS-1$

  String MSG_CHANGE_MODE              =
      "setMode(...): changing server mode: oldMode = %s, newMode = %s, loadAverage = %s";                                                   //$NON-NLS-1$
  String ERR_REJECT_CHANGE_MODE       =
      "setMode(...): the request to change server mode was rejected: oldMode = %s, newMode = %s, cause = %s";                               //$NON-NLS-1$
  String ERR_REJECT_CHANGE_CONNECTION =
      "setSharedConnection(...): the request to change shared connection was rejected: oldConnection = %s, newConnection = %s, cause = %s"; //$NON-NLS-1$
  String ERR_SUPPORT_IS_NOT_AVAILABLE = "Support %s is not available";                                                                      //$NON-NLS-1$
  String ERR_WAITING_SUPPORT          = "Waiting load support %s for backend implementation. %d";                                           //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_SUPPORT_ALREADY_REGISTER = Messages.getString( "IS5Resources.ERR_SUPPORT_ALREADY_REGISTER" ); //$NON-NLS-1$
}
