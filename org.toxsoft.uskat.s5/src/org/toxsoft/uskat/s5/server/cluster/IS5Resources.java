package org.toxsoft.uskat.s5.server.cluster;

/**
 * Константы, локализуемые ресурсы.
 *
 * @author mvk
 */
interface IS5Resources {

  String STR_D_CLUSTER_MANAGER = Messages.getString( "IS5Resources.STR_D_CLUSTER_MANAGER" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_WELCOME            = Messages.getString( "IS5Resources.MSG_WELCOME" );            //$NON-NLS-1$
  String MSG_GOODBYE            = Messages.getString( "IS5Resources.MSG_GOODBYE" );            //$NON-NLS-1$
  String MSG_CHANGE_COORDINATOR = Messages.getString( "IS5Resources.MSG_CHANGE_COORDINATOR" ); //$NON-NLS-1$
  String MSG_SEND_NOTICE        = Messages.getString( "IS5Resources.MSG_SEND_NOTICE" );        //$NON-NLS-1$
  String MSG_SEND_COMMAND       = Messages.getString( "IS5Resources.MSG_SEND_COMMAND" );       //$NON-NLS-1$
  String MSG_EXECUTED_COMMAND   = Messages.getString( "IS5Resources.MSG_EXECUTED_COMMAND" );   //$NON-NLS-1$
  String MSG_RECV_COMMAND       = Messages.getString( "IS5Resources.MSG_RECV_COMMAND" );       //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  // String ERR_SESSION_NOT_FOUND = "Попытка повторной инициализации сессии пользователя: %s";

}
