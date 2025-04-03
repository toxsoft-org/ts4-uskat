package org.toxsoft.uskat.s5.client.remote.connection.pas;

/**
 * Локализуемые ресурсы .
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IS5Resources {

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_FIND_LOCAL_ADDR           = Messages.getString( "IS5Resources.MSG_FIND_LOCAL_ADDR" );
  String MSG_SEND_VERIFY               = Messages.getString( "IS5Resources.MSG_SEND_VERIFY" );
  String MSG_NODES_NOT_FOUND           = Messages.getString( "IS5Resources.MSG_NODES_NOT_FOUND" );
  String MSG_CALLBACKS_WAIT            = Messages.getString( "IS5Resources.MSG_CALLBACKS_WAIT" );
  String MSG_SET_CLUSTER_TOPOLOGY      = Messages.getString( "IS5Resources.MSG_SET_CLUSTER_TOPOLOGY" );
  String MSG_CONNECT_NODE              = Messages.getString( "IS5Resources.MSG_CONNECT_NODE" );
  String MSG_DISCONNECT_NODE           = Messages.getString( "IS5Resources.MSG_DISCONNECT_NODE" );
  String MSG_DOJOB_RUN                 = Messages.getString( "IS5Resources.MSG_DOJOB_RUN" );
  String MSG_SENDED_INIT_SESSION_MESSAGE = "doOnSended(...): sended INIT SESSION message. aSource = %s, aMessage = %s";

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_SERVER_SESSION_CLOSE              = Messages.getString( "IS5Resources.ERR_SERVER_SESSION_CLOSE" );
  String ERR_TRY_RECONNECT                     = Messages.getString( "IS5Resources.ERR_TRY_RECONNECT" );
  String ERR_CLOSE_BY_NOT_CONNECTION           = Messages.getString( "IS5Resources.ERR_CLOSE_BY_NOT_CONNECTION" );
  String ERR_LOCAL_ADDR_FAIL                   = Messages.getString( "IS5Resources.ERR_LOCAL_ADDR_FAIL" );
  String ERR_CLUSTER_NO_CONNECT                = Messages.getString( "IS5Resources.ERR_CLUSTER_NO_CONNECT" );
  String ERR_CLUSTER_NO_CHANNEL                = Messages.getString( "IS5Resources.ERR_CLUSTER_NO_CHANNEL" );
  String ERR_CLUSTER_NO_CHANNEL_BY_TOPOLOGY    =
      Messages.getString( "IS5Resources.ERR_CLUSTER_NO_CHANNEL_BY_TOPOLOGY" );
  String ERR_CONNECT_NODE                      = Messages.getString( "IS5Resources.ERR_CONNECT_NODE" );
  String ERR_NOT_FOUND_INIT_IMPL_CLASS         = Messages.getString( "IS5Resources.ERR_NOT_FOUND_INIT_IMPL_CLASS" );
  String ERR_NOT_FOUND_INIT_IMPL_CONSTRUCTOR   =
      Messages.getString( "IS5Resources.ERR_NOT_FOUND_INIT_IMPL_CONSTRUCTOR" );
  String ERR_NOT_FOUND_INIT_IMPL_INSTANTIATION =
      Messages.getString( "IS5Resources.ERR_NOT_FOUND_INIT_IMPL_INSTANTIATION" );
  String ERR_NOT_FOUND_INIT_IMPL_UNEXPECTED    =
      Messages.getString( "IS5Resources.ERR_NOT_FOUND_INIT_IMPL_UNEXPECTED" );
  String ERR_CLOSE_NOTIFICATION_IGNORED        =
      "onCloseChannel. Notification is ignored (notificationEnabled = false)";
}
