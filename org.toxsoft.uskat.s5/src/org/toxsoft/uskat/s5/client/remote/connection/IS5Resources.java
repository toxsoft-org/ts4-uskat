package org.toxsoft.uskat.s5.client.remote.connection;

import org.toxsoft.uskat.s5.common.*;

/**
 * Локализуемые ресурсы подсистемы подключения к серверу.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IS5Resources {

  String D_DISCONNECTED = Messages.getString( "IS5Resources.D_DISCONNECTED" );
  String N_DISCONNECTED = Messages.getString( "IS5Resources.N_DISCONNECTED" );
  String D_INACTIVE     = Messages.getString( "IS5Resources.D_INACTIVE" );
  String N_INACTIVE     = Messages.getString( "IS5Resources.N_INACTIVE" );
  String D_CONNECTED    = Messages.getString( "IS5Resources.D_CONNECTED" );
  String N_CONNECTED    = Messages.getString( "IS5Resources.N_CONNECTED" );
  String D_CLIENT_TYPE  = Messages.getString( "IS5Resources.D_CLIENT_TYPE" );
  String MSG_NULL_REF   = Messages.getString( "IS5Resources.MSG_NULL_REF" );

  /**
   * {@link S5Connection} client messages
   */
  String USER_PROGRESS_BEFORE_CONNECT    = Messages.getString( "IS5Resources.USER_PROGRESS_BEFORE_CONNECT" );
  String USER_LOOKUP_REMOTE_API_START    = Messages.getString( "IS5Resources.USER_LOOKUP_REMOTE_API_START" );
  String USER_INIT_REMOTE_API_START      = Messages.getString( "IS5Resources.USER_INIT_REMOTE_API_START" );
  String USER_INIT_CALLBACKS_START       = Messages.getString( "IS5Resources.USER_INIT_CALLBACKS_START" );
  String USER_INIT_CALLBACKS_FINISH      = Messages.getString( "IS5Resources.USER_INIT_CALLBACKS_FINISH" );
  String USER_PROGRESS_CONNECT           = Messages.getString( "IS5Resources.USER_PROGRESS_CONNECT" );
  String USER_PROGRESS_WAIT_OPEN         = Messages.getString( "IS5Resources.USER_PROGRESS_WAIT_OPEN" );
  String USER_PROGRESS_BEFORE_ACTIVE     = Messages.getString( "IS5Resources.USER_PROGRESS_BEFORE_ACTIVE" );
  String USER_PROGRESS_ACTIVE_ON         = Messages.getString( "IS5Resources.USER_PROGRESS_ACTIVE_ON" );
  String USER_PROGRESS_AFTER_ACTIVE      = Messages.getString( "IS5Resources.USER_PROGRESS_AFTER_ACTIVE" );
  String USER_PROGRESS_WAIT_TRY_CONNECT  = Messages.getString( "IS5Resources.USER_PROGRESS_WAIT_TRY_CONNECT" );
  String USER_PROGRESS_AFTER_CONNECT_END = Messages.getString( "IS5Resources.USER_PROGRESS_AFTER_CONNECT_END" );

  /**
   * {@link S5Connection}
   */
  String MSG_CALL_REMOTE_API_CLOSE            = Messages.getString( "IS5Resources.MSG_CALL_REMOTE_API_CLOSE" );
  String MSG_CONNECTION_ACTIVATED             = Messages.getString( "IS5Resources.MSG_CONNECTION_ACTIVATED" );
  String MSG_CONNECTION_DEACTIVATED           = Messages.getString( "IS5Resources.MSG_CONNECTION_DEACTIVATED" );
  String MSG_CREATE_CONNECTION                = Messages.getString( "IS5Resources.MSG_CREATE_CONNECTION" );
  String MSG_CLOSE_CONNECTION_QUERY           = Messages.getString( "IS5Resources.MSG_CLOSE_CONNECTION_QUERY" );
  String MSG_CONNECTION_CLOSED                = Messages.getString( "IS5Resources.MSG_CONNECTION_CLOSED" );
  String MSG_CONNECTION_BREAKED               = Messages.getString( "IS5Resources.MSG_CONNECTION_BREAKED" );
  String MSG_CONNECTION_THREAD_START          = Messages.getString( "IS5Resources.MSG_CONNECTION_THREAD_START" );
  String MSG_CONNECTION_THREAD_STARTED        = Messages.getString( "IS5Resources.MSG_CONNECTION_THREAD_STARTED" );
  String MSG_CONNECTION_THREAD_RESTART        = Messages.getString( "IS5Resources.MSG_CONNECTION_THREAD_RESTART" );
  String MSG_CONNECTION_THREAD_FINISH_BY_NODE =
      Messages.getString( "IS5Resources.MSG_CONNECTION_THREAD_FINISH_BY_NODE" );
  String MSG_CONNECTION_THREAD_FINISH         = Messages.getString( "IS5Resources.MSG_CONNECTION_THREAD_FINISH" );
  String MSG_CONNECTION_NOT_ACTIVE            = Messages.getString( "IS5Resources.MSG_CONNECTION_NOT_ACTIVE" );
  String MSG_REMOTE_EJB_DISABLE               = Messages.getString( "IS5Resources.MSG_REMOTE_EJB_DISABLE" );
  String MSG_REMOTE_EJB_DISABLED              = Messages.getString( "IS5Resources.MSG_REMOTE_EJB_DISABLED" );
  String MSG_CONNECTION_THREAD_QUERY_FINISH   = Messages.getString( "IS5Resources.MSG_CONNECTION_THREAD_QUERY_FINISH" );
  String MSG_CONNECTION_THREAD_COMPLETED      = Messages.getString( "IS5Resources.MSG_CONNECTION_THREAD_COMPLETED" );

  String MSG_LOOKUP_REMOTE_API_FINISH = Messages.getString( "IS5Resources.MSG_LOOKUP_REMOTE_API_FINISH" );

  String MSG_TRYCONNECT_USING_CLASSLOADER = "tryConnect(...): classLoader = %s";
  String MSG_INIT_REMOTE_API_FINISH       = Messages.getString( "IS5Resources.MSG_INIT_REMOTE_API_FINISH" );

  String MSG_CONNECT_JMS_SERVER = Messages.getString( "IS5Resources.MSG_CONNECT_JMS_SERVER" );
  String MSG_START_JMS_START    = Messages.getString( "IS5Resources.MSG_START_JMS_START" );
  String MSG_START_JMS_FINISH   = Messages.getString( "IS5Resources.MSG_START_JMS_FINISH" );

  String MSG_SERVICE_PROXY_REPLACE = Messages.getString( "IS5Resources.MSG_SERVICE_PROXY_REPLACE" );
  String MSG_PROXY_REPLACE         = Messages.getString( "IS5Resources.MSG_PROXY_REPLACE" );

  String MSG_TRY_RECONNECT_NODE       = Messages.getString( "IS5Resources.MSG_TRY_RECONNECT_NODE" );
  String MSG_NODE_NOT_CHANGED         = Messages.getString( "IS5Resources.MSG_NODE_NOT_CHANGED" );
  String MSG_FOUND_NODE_RECONNECT_JMS = Messages.getString( "IS5Resources.MSG_FOUND_NODE_RECONNECT_JMS" );
  String MSG_CREATE_CLUSTER           = Messages.getString( "IS5Resources.MSG_CREATE_CLUSTER" );
  String MSG_REMOVE_CLUSTER           = Messages.getString( "IS5Resources.MSG_REMOVE_CLUSTER" );
  String MSG_ADD_CLUSTER_NODE         = Messages.getString( "IS5Resources.MSG_ADD_CLUSTER_NODE" );
  String MSG_REMOVE_CLUSTER_NODE      = Messages.getString( "IS5Resources.MSG_REMOVE_CLUSTER_NODE" );
  String MSG_OPEN_CONNECT_COMPLETED   = Messages.getString( "IS5Resources.MSG_OPEN_CONNECT_COMPLETED" );

  String MSG_QUERY_CLOSE_THREAD                 = Messages.getString( "IS5Resources.MSG_QUERY_CLOSE_THREAD" );
  String MSG_STACK                              = Messages.getString( "IS5Resources.MSG_STACK" );
  String MSG_SET_INVOCATION_CREATE_TIMEOUT      =
      Messages.getString( "IS5Resources.MSG_SET_INVOCATION_CREATE_TIMEOUT" );
  String MSG_SET_INVOCATION_FAILURE_TIMEOUT     =
      Messages.getString( "IS5Resources.MSG_SET_INVOCATION_FAILURE_TIMEOUT" );
  String MSG_INTERCEPTOR_SET_INVOCATION_TIMEOUT =
      Messages.getString( "IS5Resources.MSG_INTERCEPTOR_SET_INVOCATION_TIMEOUT" );
  String MSG_CLIENT_OPTIONS                     = "client options: \n%s";

  String ERR_NOT_FOUND_AVAILABLE_NODES = Messages.getString( "IS5Resources.ERR_NOT_FOUND_AVAILABLE_NODES" );
  String ERR_RECONNECT_NODE            = Messages.getString( "IS5Resources.ERR_RECONNECT_NODE" );

  String ERR_CONNECTION_DEACTIVATED        = Messages.getString( "IS5Resources.ERR_CONNECTION_DEACTIVATED" );
  String ERR_RESTORE_ALREADY_RUNNING       = Messages.getString( "IS5Resources.ERR_RESTORE_ALREADY_RUNNING" );
  String ERR_RESTORE_THREAD_ALREADY_EXIST  = Messages.getString( "IS5Resources.ERR_RESTORE_THREAD_ALREADY_EXIST" );
  String ERR_LOCK_INTERRUPT                = Messages.getString( "IS5Resources.ERR_LOCK_INTERRUPT" );
  String ERR_CONNECTION_THREAD_INTERRUPT   = Messages.getString( "IS5Resources.ERR_CONNECTION_THREAD_INTERRUPT" );
  String ERR_ACCESS_TO_DISCONNECTED_SERVER = Messages.getString( "IS5Resources.ERR_ACCESS_TO_DISCONNECTED_SERVER" );
  String ERR_ALREADY_ACTIVE                = Messages.getString( "IS5Resources.ERR_ALREADY_ACTIVE" );
  String ERR_HOSTS_NOT_DEFINED             = Messages.getString( "IS5Resources.ERR_HOSTS_NOT_DEFINED" );
  String ERR_CONNECTION_IS_CLOSING         = Messages.getString( "IS5Resources.ERR_CONNECTION_IS_CLOSING" );
  String ERR_LOGIN                         = Messages.getString( "IS5Resources.ERR_LOGIN" );
  String ERR_UNKNOWN_REMOTE                = Messages.getString( "IS5Resources.ERR_UNKNOWN_REMOTE" );
  String ERR_URL                           = Messages.getString( "IS5Resources.ERR_URL" );
  String ERR_NO_CLIENT_SUPPORT             = Messages.getString( "IS5Resources.ERR_NO_CLIENT_SUPPORT" );
  String ERR_ENTRY_NOT_FOUND               = Messages.getString( "IS5Resources.ERR_ENTRY_NOT_FOUND" );
  String ERR_NOT_CONNECTION                = Messages.getString( "IS5Resources.ERR_NOT_CONNECTION" );
  String ERR_NOT_CONNECTED                 = Messages.getString( "IS5Resources.ERR_NOT_CONNECTED" );
  String ERR_NO_ACCESS                     = Messages.getString( "IS5Resources.ERR_NO_ACCESS" );
  String ERR_SERVER_NOT_FOUND              = Messages.getString( "IS5Resources.ERR_SERVER_NOT_FOUND" );
  String ERR_INIT_SESSION                  = Messages.getString( "IS5Resources.ERR_INIT_SESSION" );
  String ERR_UNEXPECTED_BY_CONNECT         = Messages.getString( "IS5Resources.ERR_UNEXPECTED_BY_CONNECT" );
  String ERR_JMS_CONNECT                   = Messages.getString( "IS5Resources.ERR_JMS_CONNECT" );
  String ERR_CLIENT_API                    = Messages.getString( "IS5Resources.ERR_CLIENT_API" );
  String ERR_CREATE_CONNECTION             = Messages.getString( "IS5Resources.ERR_CREATE_CONNECTION" );
  String ERR_CREATE_CONNECTION_WITH_STACK  = Messages.getString( "IS5Resources.ERR_CREATE_CONNECTION_WITH_STACK" );
  String ERR_CONNECTION_THREAD_START       = Messages.getString( "IS5Resources.ERR_CONNECTION_THREAD_START" );
  String ERR_NO_EJB_RETRY                  = Messages.getString( "IS5Resources.ERR_NO_EJB_RETRY" );
  String ERR_NO_EJB_RETRY_BY_CLOSE         = Messages.getString( "IS5Resources.ERR_NO_EJB_RETRY_BY_CLOSE" );
  String ERR_UNEXPECTED_CLOSE              = Messages.getString( "IS5Resources.ERR_UNEXPECTED_CLOSE" );
  String ERR_REPLACE_PROXY                 = Messages.getString( "IS5Resources.ERR_REPLACE_PROXY" );
  String ERR_TRANSOPRT_NOT_FOUND           = Messages.getString( "IS5Resources.ERR_TRANSOPRT_NOT_FOUND" );
  String ERR_TRANSOPRT_LOAD                = Messages.getString( "IS5Resources.ERR_TRANSOPRT_LOAD" );

  String ERR_UNEXPECT_PROGRESS_PROBLEM            = Messages.getString( "IS5Resources.ERR_UNEXPECT_PROGRESS_PROBLEM" );
  String ERR_CLOSE_BY_LOCK_NOT_FOUND              =
      "%s. run(): Не найдена блокировка соединения в пуле блокировок. Завершение потока восстановления соединения";
  String ERR_RESTORE_CONNECTION_TRY_LOCK          = "restoreSessionQuery(): ошибка получения блокировки %s.";
  String ERR_RESTORE_CONNECTION_CALL_CLOSE_REMOTE = "restoreSessionQuery(): call handlingUnexpectedBreak()";
  String ERR_RESTORE_CONNECTION_LOCK_INTERRUPT    = "restoreSessionQuery(): lockThread.interrupt()";
  String ERR_RESTORE_CONNECTION_LOOKUP_INTERRUPT  = "restoreSessionQuery(): lookupApiThread.interrupt()";

  String MSG_CONNECTION_THREAD_TRY_INTERRUPT = Messages.getString( "IS5Resources.MSG_CONNECTION_THREAD_TRY_INTERRUPT" );
  String MSG_DOJOB_THREAD_INTERRUPT          = Messages.getString( "IS5Resources.MSG_DOJOB_THREAD_INTERRUPT" );
  String MSG_CHANGE_ADDON_AFFINITY           = Messages.getString( "IS5Resources.MSG_CHANGE_ADDON_AFFINITY" );
  String MSG_CHANGE_CALL_AFFINITY            = Messages.getString( "IS5Resources.MSG_CHANGE_CALL_AFFINITY" );
  String MSG_UPDATE_CLUSTER_TOPOLOGY         = Messages.getString( "IS5Resources.MSG_UPDATE_CLUSTER_TOPOLOGY" );

  /**
   * Правило формирования URL провайдера
   */
  String PROVIDER_URL_FORMAT = Messages.getString( "IS5Resources.PROVIDER_URL_FORMAT" );

  /**
   * Правило формирования JNDI-имени точки входа
   * <p>
   * app, module, distinct, jndiName
   */
  String JNDI_LOOKUP_FORMAT = Messages.getString( "IS5Resources.JNDI_LOOKUP_FORMAT" );

  /**
   * {@link S5Host}
   */
  String MSG_ERR_INV_HOSTS_LIST_FORMAT = Messages.getString( "IS5Resources.MSG_ERR_INV_HOSTS_LIST_FORMAT" );

}
