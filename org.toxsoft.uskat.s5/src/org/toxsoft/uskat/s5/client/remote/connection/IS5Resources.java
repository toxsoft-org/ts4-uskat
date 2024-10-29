package org.toxsoft.uskat.s5.client.remote.connection;

import org.toxsoft.uskat.s5.common.*;

/**
 * Локализуемые ресурсы подсистемы подключения к серверу.
 *
 * @author mvk
 */
interface IS5Resources {

  String D_DISCONNECTED = Messages.getString( "IS5Resources.D_DISCONNECTED" ); //$NON-NLS-1$
  String N_DISCONNECTED = Messages.getString( "IS5Resources.N_DISCONNECTED" ); //$NON-NLS-1$
  String D_INACTIVE     = Messages.getString( "IS5Resources.D_INACTIVE" );     //$NON-NLS-1$
  String N_INACTIVE     = Messages.getString( "IS5Resources.N_INACTIVE" );     //$NON-NLS-1$
  String D_CONNECTED    = Messages.getString( "IS5Resources.D_CONNECTED" );    //$NON-NLS-1$
  String N_CONNECTED    = Messages.getString( "IS5Resources.N_CONNECTED" );    //$NON-NLS-1$
  String D_CLIENT_TYPE  = Messages.getString( "IS5Resources.D_CLIENT_TYPE" );  //$NON-NLS-1$
  String MSG_NULL_REF   = Messages.getString( "IS5Resources.MSG_NULL_REF" );   //$NON-NLS-1$

  /**
   * {@link S5Connection} client messages
   */
  String USER_PROGRESS_BEFORE_CONNECT    = Messages.getString( "IS5Resources.USER_PROGRESS_BEFORE_CONNECT" );    //$NON-NLS-1$
  String USER_LOOKUP_REMOTE_API_START    = Messages.getString( "IS5Resources.USER_LOOKUP_REMOTE_API_START" );    //$NON-NLS-1$
  String USER_INIT_REMOTE_API_START      = Messages.getString( "IS5Resources.USER_INIT_REMOTE_API_START" );      //$NON-NLS-1$
  String USER_INIT_CALLBACKS_START       = Messages.getString( "IS5Resources.USER_INIT_CALLBACKS_START" );       //$NON-NLS-1$
  String USER_INIT_CALLBACKS_FINISH      = Messages.getString( "IS5Resources.USER_INIT_CALLBACKS_FINISH" );      //$NON-NLS-1$
  String USER_PROGRESS_CONNECT           = Messages.getString( "IS5Resources.USER_PROGRESS_CONNECT" );           //$NON-NLS-1$
  String USER_PROGRESS_WAIT_OPEN         = Messages.getString( "IS5Resources.USER_PROGRESS_WAIT_OPEN" );         //$NON-NLS-1$
  String USER_PROGRESS_BEFORE_ACTIVE     = Messages.getString( "IS5Resources.USER_PROGRESS_BEFORE_ACTIVE" );     //$NON-NLS-1$
  String USER_PROGRESS_ACTIVE_ON         = Messages.getString( "IS5Resources.USER_PROGRESS_ACTIVE_ON" );         //$NON-NLS-1$
  String USER_PROGRESS_AFTER_ACTIVE      = Messages.getString( "IS5Resources.USER_PROGRESS_AFTER_ACTIVE" );      //$NON-NLS-1$
  String USER_PROGRESS_WAIT_TRY_CONNECT  = Messages.getString( "IS5Resources.USER_PROGRESS_WAIT_TRY_CONNECT" );  //$NON-NLS-1$
  String USER_PROGRESS_AFTER_CONNECT_END = Messages.getString( "IS5Resources.USER_PROGRESS_AFTER_CONNECT_END" ); //$NON-NLS-1$

  /**
   * {@link S5Connection}
   */
  String MSG_CALL_REMOTE_API_CLOSE            = Messages.getString( "IS5Resources.MSG_CALL_REMOTE_API_CLOSE" );          //$NON-NLS-1$
  String MSG_CONNECTION_ACTIVATED             = Messages.getString( "IS5Resources.MSG_CONNECTION_ACTIVATED" );           //$NON-NLS-1$
  String MSG_CONNECTION_DEACTIVATED           = Messages.getString( "IS5Resources.MSG_CONNECTION_DEACTIVATED" );         //$NON-NLS-1$
  String MSG_CREATE_CONNECTION                = Messages.getString( "IS5Resources.MSG_CREATE_CONNECTION" );              //$NON-NLS-1$
  String MSG_CLOSE_CONNECTION_QUERY           = Messages.getString( "IS5Resources.MSG_CLOSE_CONNECTION_QUERY" );         //$NON-NLS-1$
  String MSG_CONNECTION_CLOSED                = Messages.getString( "IS5Resources.MSG_CONNECTION_CLOSED" );              //$NON-NLS-1$
  String MSG_CONNECTION_BREAKED               = Messages.getString( "IS5Resources.MSG_CONNECTION_BREAKED" );             //$NON-NLS-1$
  String MSG_CONNECTION_THREAD_START          = Messages.getString( "IS5Resources.MSG_CONNECTION_THREAD_START" );        //$NON-NLS-1$
  String MSG_CONNECTION_THREAD_STARTED        = Messages.getString( "IS5Resources.MSG_CONNECTION_THREAD_STARTED" );      //$NON-NLS-1$
  String MSG_CONNECTION_THREAD_RESTART        = Messages.getString( "IS5Resources.MSG_CONNECTION_THREAD_RESTART" );      //$NON-NLS-1$
  String MSG_CONNECTION_THREAD_FINISH_BY_NODE =
      Messages.getString( "IS5Resources.MSG_CONNECTION_THREAD_FINISH_BY_NODE" );                                         //$NON-NLS-1$
  String MSG_CONNECTION_THREAD_FINISH         = Messages.getString( "IS5Resources.MSG_CONNECTION_THREAD_FINISH" );       //$NON-NLS-1$
  String MSG_CONNECTION_NOT_ACTIVE            = Messages.getString( "IS5Resources.MSG_CONNECTION_NOT_ACTIVE" );          //$NON-NLS-1$
  String MSG_REMOTE_EJB_DISABLE               = Messages.getString( "IS5Resources.MSG_REMOTE_EJB_DISABLE" );             //$NON-NLS-1$
  String MSG_REMOTE_EJB_DISABLED              = Messages.getString( "IS5Resources.MSG_REMOTE_EJB_DISABLED" );            //$NON-NLS-1$
  String MSG_CONNECTION_THREAD_QUERY_FINISH   = Messages.getString( "IS5Resources.MSG_CONNECTION_THREAD_QUERY_FINISH" ); //$NON-NLS-1$
  String MSG_CONNECTION_THREAD_COMPLETED      = Messages.getString( "IS5Resources.MSG_CONNECTION_THREAD_COMPLETED" );    //$NON-NLS-1$

  String MSG_LOOKUP_REMOTE_API_FINISH = Messages.getString( "IS5Resources.MSG_LOOKUP_REMOTE_API_FINISH" ); //$NON-NLS-1$

  String MSG_TRYCONNECT_USING_CLASSLOADER = "tryConnect(...): classLoader = %s";                             //$NON-NLS-1$
  String MSG_INIT_REMOTE_API_FINISH       = Messages.getString( "IS5Resources.MSG_INIT_REMOTE_API_FINISH" ); //$NON-NLS-1$

  String MSG_CONNECT_JMS_SERVER = Messages.getString( "IS5Resources.MSG_CONNECT_JMS_SERVER" ); //$NON-NLS-1$
  String MSG_START_JMS_START    = Messages.getString( "IS5Resources.MSG_START_JMS_START" );    //$NON-NLS-1$
  String MSG_START_JMS_FINISH   = Messages.getString( "IS5Resources.MSG_START_JMS_FINISH" );   //$NON-NLS-1$

  String MSG_SERVICE_PROXY_REPLACE = Messages.getString( "IS5Resources.MSG_SERVICE_PROXY_REPLACE" ); //$NON-NLS-1$
  String MSG_PROXY_REPLACE         = Messages.getString( "IS5Resources.MSG_PROXY_REPLACE" );         //$NON-NLS-1$

  String MSG_TRY_RECONNECT_NODE       = Messages.getString( "IS5Resources.MSG_TRY_RECONNECT_NODE" );       //$NON-NLS-1$
  String MSG_NODE_NOT_CHANGED         = Messages.getString( "IS5Resources.MSG_NODE_NOT_CHANGED" );         //$NON-NLS-1$
  String MSG_FOUND_NODE_RECONNECT_JMS = Messages.getString( "IS5Resources.MSG_FOUND_NODE_RECONNECT_JMS" ); //$NON-NLS-1$
  String MSG_CREATE_CLUSTER           = Messages.getString( "IS5Resources.MSG_CREATE_CLUSTER" );           //$NON-NLS-1$
  String MSG_REMOVE_CLUSTER           = Messages.getString( "IS5Resources.MSG_REMOVE_CLUSTER" );           //$NON-NLS-1$
  String MSG_ADD_CLUSTER_NODE         = Messages.getString( "IS5Resources.MSG_ADD_CLUSTER_NODE" );         //$NON-NLS-1$
  String MSG_REMOVE_CLUSTER_NODE      = Messages.getString( "IS5Resources.MSG_REMOVE_CLUSTER_NODE" );      //$NON-NLS-1$
  String MSG_OPEN_CONNECT_COMPLETED   = Messages.getString( "IS5Resources.MSG_OPEN_CONNECT_COMPLETED" );   //$NON-NLS-1$

  String MSG_QUERY_CLOSE_THREAD                 = Messages.getString( "IS5Resources.MSG_QUERY_CLOSE_THREAD" ); //$NON-NLS-1$
  String MSG_STACK                              = Messages.getString( "IS5Resources.MSG_STACK" );              //$NON-NLS-1$
  String MSG_SET_INVOCATION_CREATE_TIMEOUT      =
      Messages.getString( "IS5Resources.MSG_SET_INVOCATION_CREATE_TIMEOUT" );                                  //$NON-NLS-1$
  String MSG_SET_INVOCATION_FAILURE_TIMEOUT     =
      Messages.getString( "IS5Resources.MSG_SET_INVOCATION_FAILURE_TIMEOUT" );                                 //$NON-NLS-1$
  String MSG_INTERCEPTOR_SET_INVOCATION_TIMEOUT =
      Messages.getString( "IS5Resources.MSG_INTERCEPTOR_SET_INVOCATION_TIMEOUT" );                             //$NON-NLS-1$

  String ERR_NOT_FOUND_AVAILABLE_NODES = Messages.getString( "IS5Resources.ERR_NOT_FOUND_AVAILABLE_NODES" ); //$NON-NLS-1$
  String ERR_RECONNECT_NODE            = Messages.getString( "IS5Resources.ERR_RECONNECT_NODE" );            //$NON-NLS-1$

  String ERR_CONNECTION_DEACTIVATED        = Messages.getString( "IS5Resources.ERR_CONNECTION_DEACTIVATED" );        //$NON-NLS-1$
  String ERR_RESTORE_ALREADY_RUNNING       = Messages.getString( "IS5Resources.ERR_RESTORE_ALREADY_RUNNING" );       //$NON-NLS-1$
  String ERR_RESTORE_THREAD_ALREADY_EXIST  = Messages.getString( "IS5Resources.ERR_RESTORE_THREAD_ALREADY_EXIST" );  //$NON-NLS-1$
  String ERR_LOCK_INTERRUPT                = Messages.getString( "IS5Resources.ERR_LOCK_INTERRUPT" );                //$NON-NLS-1$
  String ERR_CONNECTION_THREAD_INTERRUPT   = Messages.getString( "IS5Resources.ERR_CONNECTION_THREAD_INTERRUPT" );   //$NON-NLS-1$
  String ERR_ACCESS_TO_DISCONNECTED_SERVER = Messages.getString( "IS5Resources.ERR_ACCESS_TO_DISCONNECTED_SERVER" ); //$NON-NLS-1$
  String ERR_ALREADY_ACTIVE                = Messages.getString( "IS5Resources.ERR_ALREADY_ACTIVE" );                //$NON-NLS-1$
  String ERR_HOSTS_NOT_DEFINED             = Messages.getString( "IS5Resources.ERR_HOSTS_NOT_DEFINED" );             //$NON-NLS-1$
  String ERR_CONNECTION_IS_CLOSING         = Messages.getString( "IS5Resources.ERR_CONNECTION_IS_CLOSING" );         //$NON-NLS-1$
  String ERR_LOGIN                         = Messages.getString( "IS5Resources.ERR_LOGIN" );                         //$NON-NLS-1$
  String ERR_UNKNOWN_REMOTE                = Messages.getString( "IS5Resources.ERR_UNKNOWN_REMOTE" );                //$NON-NLS-1$
  String ERR_URL                           = Messages.getString( "IS5Resources.ERR_URL" );                           //$NON-NLS-1$
  String ERR_NO_CLIENT_SUPPORT             = Messages.getString( "IS5Resources.ERR_NO_CLIENT_SUPPORT" );             //$NON-NLS-1$
  String ERR_ENTRY_NOT_FOUND               = Messages.getString( "IS5Resources.ERR_ENTRY_NOT_FOUND" );               //$NON-NLS-1$
  String ERR_NOT_CONNECTION                = Messages.getString( "IS5Resources.ERR_NOT_CONNECTION" );                //$NON-NLS-1$
  String ERR_NOT_CONNECTED                 = Messages.getString( "IS5Resources.ERR_NOT_CONNECTED" );                 //$NON-NLS-1$
  String ERR_NO_ACCESS                     = Messages.getString( "IS5Resources.ERR_NO_ACCESS" );                     //$NON-NLS-1$
  String ERR_SERVER_NOT_FOUND              = Messages.getString( "IS5Resources.ERR_SERVER_NOT_FOUND" );              //$NON-NLS-1$
  String ERR_INIT_SESSION                  = Messages.getString( "IS5Resources.ERR_INIT_SESSION" );                  //$NON-NLS-1$
  String ERR_UNEXPECTED_BY_CONNECT         = Messages.getString( "IS5Resources.ERR_UNEXPECTED_BY_CONNECT" );         //$NON-NLS-1$
  String ERR_JMS_CONNECT                   = Messages.getString( "IS5Resources.ERR_JMS_CONNECT" );                   //$NON-NLS-1$
  String ERR_CLIENT_API                    = Messages.getString( "IS5Resources.ERR_CLIENT_API" );                    //$NON-NLS-1$
  String ERR_CREATE_CONNECTION             = Messages.getString( "IS5Resources.ERR_CREATE_CONNECTION" );             //$NON-NLS-1$
  String ERR_CREATE_CONNECTION_WITH_STACK  = Messages.getString( "IS5Resources.ERR_CREATE_CONNECTION_WITH_STACK" );  //$NON-NLS-1$
  String ERR_CONNECTION_THREAD_START       = Messages.getString( "IS5Resources.ERR_CONNECTION_THREAD_START" );       //$NON-NLS-1$
  String ERR_NO_EJB_RETRY                  = Messages.getString( "IS5Resources.ERR_NO_EJB_RETRY" );                  //$NON-NLS-1$
  String ERR_NO_EJB_RETRY_BY_CLOSE         = Messages.getString( "IS5Resources.ERR_NO_EJB_RETRY_BY_CLOSE" );         //$NON-NLS-1$
  String ERR_UNEXPECTED_CLOSE              = Messages.getString( "IS5Resources.ERR_UNEXPECTED_CLOSE" );              //$NON-NLS-1$
  String ERR_REPLACE_PROXY                 = Messages.getString( "IS5Resources.ERR_REPLACE_PROXY" );                 //$NON-NLS-1$
  String ERR_TRANSOPRT_NOT_FOUND           = Messages.getString( "IS5Resources.ERR_TRANSOPRT_NOT_FOUND" );           //$NON-NLS-1$
  String ERR_TRANSOPRT_LOAD                = Messages.getString( "IS5Resources.ERR_TRANSOPRT_LOAD" );                //$NON-NLS-1$

  String ERR_UNEXPECT_PROGRESS_PROBLEM            = Messages.getString( "IS5Resources.ERR_UNEXPECT_PROGRESS_PROBLEM" ); //$NON-NLS-1$
  String ERR_CLOSE_BY_LOCK_NOT_FOUND              =
      "%s. run(): Не найдена блокировка соединения в пуле блокировок. Завершение потока восстановления соединения";     //$NON-NLS-1$
  String ERR_RESTORE_CONNECTION_TRY_LOCK          = "restoreSessionQuery(): ошибка получения блокировки %s.";           //$NON-NLS-1$
  String ERR_RESTORE_CONNECTION_CALL_CLOSE_REMOTE = "restoreSessionQuery(): call handlingUnexpectedBreak()";            //$NON-NLS-1$
  String ERR_RESTORE_CONNECTION_LOCK_INTERRUPT    = "restoreSessionQuery(): lockThread.interrupt()";                    //$NON-NLS-1$
  String ERR_RESTORE_CONNECTION_LOOKUP_INTERRUPT  = "restoreSessionQuery(): lookupApiThread.interrupt()";               //$NON-NLS-1$

  String MSG_CONNECTION_THREAD_TRY_INTERRUPT = Messages.getString( "IS5Resources.MSG_CONNECTION_THREAD_TRY_INTERRUPT" ); //$NON-NLS-1$
  String MSG_DOJOB_THREAD_INTERRUPT          = Messages.getString( "IS5Resources.MSG_DOJOB_THREAD_INTERRUPT" );          //$NON-NLS-1$
  String MSG_CHANGE_ADDON_AFFINITY           = Messages.getString( "IS5Resources.MSG_CHANGE_ADDON_AFFINITY" );           //$NON-NLS-1$
  String MSG_CHANGE_CALL_AFFINITY            = Messages.getString( "IS5Resources.MSG_CHANGE_CALL_AFFINITY" );            //$NON-NLS-1$
  String MSG_UPDATE_CLUSTER_TOPOLOGY         = Messages.getString( "IS5Resources.MSG_UPDATE_CLUSTER_TOPOLOGY" );         //$NON-NLS-1$

  /**
   * Правило формирования URL провайдера
   */
  String PROVIDER_URL_FORMAT = Messages.getString( "IS5Resources.PROVIDER_URL_FORMAT" ); //$NON-NLS-1$

  /**
   * Правило формирования JNDI-имени точки входа
   * <p>
   * app, module, distinct, jndiName
   */
  String JNDI_LOOKUP_FORMAT = Messages.getString( "IS5Resources.JNDI_LOOKUP_FORMAT" ); //$NON-NLS-1$

  /**
   * {@link S5Host}
   */
  String MSG_ERR_INV_HOSTS_LIST_FORMAT = Messages.getString( "IS5Resources.MSG_ERR_INV_HOSTS_LIST_FORMAT" ); //$NON-NLS-1$

}
