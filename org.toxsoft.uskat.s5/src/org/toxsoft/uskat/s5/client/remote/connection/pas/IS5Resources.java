package org.toxsoft.uskat.s5.client.remote.connection.pas;

/**
 * Локализуемые ресурсы .
 *
 * @author mvk
 */
interface IS5Resources {

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_FIND_LOCAL_ADDR      = Messages.getString( "IS5Resources.MSG_FIND_LOCAL_ADDR" );      //$NON-NLS-1$
  String MSG_SEND_VERIFY          = Messages.getString( "IS5Resources.MSG_SEND_VERIFY" );          //$NON-NLS-1$
  String MSG_NODES_NOT_FOUND      = Messages.getString( "IS5Resources.MSG_NODES_NOT_FOUND" );      //$NON-NLS-1$
  String MSG_CALLBACKS_WAIT       = Messages.getString( "IS5Resources.MSG_CALLBACKS_WAIT" );       //$NON-NLS-1$
  String MSG_SET_CLUSTER_TOPOLOGY = Messages.getString( "IS5Resources.MSG_SET_CLUSTER_TOPOLOGY" ); //$NON-NLS-1$
  String MSG_CONNECT_NODE         = Messages.getString( "IS5Resources.MSG_CONNECT_NODE" );         //$NON-NLS-1$
  String MSG_DISCONNECT_NODE      = Messages.getString( "IS5Resources.MSG_DISCONNECT_NODE" );      //$NON-NLS-1$
  String MSG_DOJOB_RUN            = Messages.getString( "IS5Resources.MSG_DOJOB_RUN" );            //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_SERVER_SESSION_CLOSE              = Messages.getString( "IS5Resources.ERR_SERVER_SESSION_CLOSE" );      //$NON-NLS-1$
  String ERR_TRY_RECONNECT                     = Messages.getString( "IS5Resources.ERR_TRY_RECONNECT" );             //$NON-NLS-1$
  String ERR_CLOSE_BY_NOT_CONNECTION           = Messages.getString( "IS5Resources.ERR_CLOSE_BY_NOT_CONNECTION" );   //$NON-NLS-1$
  String ERR_LOCAL_ADDR_FAIL                   = Messages.getString( "IS5Resources.ERR_LOCAL_ADDR_FAIL" );           //$NON-NLS-1$
  String ERR_CLUSTER_NO_CONNECT                = Messages.getString( "IS5Resources.ERR_CLUSTER_NO_CONNECT" );        //$NON-NLS-1$
  String ERR_CLUSTER_NO_CHANNEL                = Messages.getString( "IS5Resources.ERR_CLUSTER_NO_CHANNEL" );        //$NON-NLS-1$
  String ERR_CLUSTER_NO_CHANNEL_BY_TOPOLOGY    =
      Messages.getString( "IS5Resources.ERR_CLUSTER_NO_CHANNEL_BY_TOPOLOGY" );                                       //$NON-NLS-1$
  String ERR_CONNECT_NODE                      = Messages.getString( "IS5Resources.ERR_CONNECT_NODE" );              //$NON-NLS-1$
  String ERR_NOT_FOUND_INIT_IMPL_CLASS         = Messages.getString( "IS5Resources.ERR_NOT_FOUND_INIT_IMPL_CLASS" ); //$NON-NLS-1$
  String ERR_NOT_FOUND_INIT_IMPL_CONSTRUCTOR   =
      Messages.getString( "IS5Resources.ERR_NOT_FOUND_INIT_IMPL_CONSTRUCTOR" );                                      //$NON-NLS-1$
  String ERR_NOT_FOUND_INIT_IMPL_INSTANTIATION =
      Messages.getString( "IS5Resources.ERR_NOT_FOUND_INIT_IMPL_INSTANTIATION" );                                    //$NON-NLS-1$
  String ERR_NOT_FOUND_INIT_IMPL_UNEXPECTED    =
      Messages.getString( "IS5Resources.ERR_NOT_FOUND_INIT_IMPL_UNEXPECTED" );                                       //$NON-NLS-1$
  String ERR_CLOSE_NOTIFICATION_IGNORED        =
      "onCloseChannel. Notification is ignored (notificationEnabled = false)";                                       //$NON-NLS-1$
}
