package org.toxsoft.uskat.s5.server.sessions;

/**
 * Константы, локализуемые ресурсы.
 *
 * @author mvk
 */
interface IS5Resources {

  String STR_D_SESSION_MANAGER    = Messages.getString( "IS5Resources.STR_D_SESSION_MANAGER" );    //$NON-NLS-1$
  String TO_STRING_FORMAT         = Messages.getString( "IS5Resources.TO_STRING_FORMAT" );         //$NON-NLS-1$
  String RESOURCE_TOSTRING_FORMAT = Messages.getString( "IS5Resources.RESOURCE_TOSTRING_FORMAT" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_SESSION_MOVE_TO_NODE        = Messages.getString( "IS5Resources.MSG_SESSION_MOVE_TO_NODE" );        //$NON-NLS-1$
  String MSG_CREATE_SESSION_START        = Messages.getString( "IS5Resources.MSG_CREATE_SESSION_START" );        //$NON-NLS-1$
  String MSG_CREATE_SESSION_SERVICE      = Messages.getString( "IS5Resources.MSG_CREATE_SESSION_SERVICE" );      //$NON-NLS-1$
  String MSG_REMOVE_SESSION_SERVICE      = Messages.getString( "IS5Resources.MSG_REMOVE_SESSION_SERVICE" );      //$NON-NLS-1$
  String MSG_SESSION_EXTENSION_CONTEXT   = Messages.getString( "IS5Resources.MSG_SESSION_EXTENSION_CONTEXT" );   //$NON-NLS-1$
  String MSG_SESSION_SERVICE_CONTEXT     = Messages.getString( "IS5Resources.MSG_SESSION_SERVICE_CONTEXT" );     //$NON-NLS-1$
  String MSG_SESSION_EXTENSION_REMOVE    = Messages.getString( "IS5Resources.MSG_SESSION_EXTENSION_REMOVE" );    //$NON-NLS-1$
  String MSG_SESSION_SERVICE_REMOVE      = Messages.getString( "IS5Resources.MSG_SESSION_SERVICE_REMOVE" );      //$NON-NLS-1$
  String MSG_SESSION_EXTENSION_ACTIVATE  = Messages.getString( "IS5Resources.MSG_SESSION_EXTENSION_ACTIVATE" );  //$NON-NLS-1$
  String MSG_SESSION_SERVICE_ACTIVATE    = Messages.getString( "IS5Resources.MSG_SESSION_SERVICE_ACTIVATE" );    //$NON-NLS-1$
  String MSG_SESSION_EXTENSION_PASSIVATE = Messages.getString( "IS5Resources.MSG_SESSION_EXTENSION_PASSIVATE" ); //$NON-NLS-1$
  String MSG_SESSION_SERVICE_PASSIVATE   = Messages.getString( "IS5Resources.MSG_SESSION_SERVICE_PASSIVATE" );   //$NON-NLS-1$
  String MSG_CLOSE_RESOURCE              = Messages.getString( "IS5Resources.MSG_CLOSE_RESOURCE" );              //$NON-NLS-1$
  String MSG_SESSION_QUERY_REMOVE        = Messages.getString( "IS5Resources.MSG_SESSION_QUERY_REMOVE" );        //$NON-NLS-1$
  String MSG_SESSION_VALIDATION          = Messages.getString( "IS5Resources.MSG_SESSION_VALIDATION" );          //$NON-NLS-1$
  String MSG_SESSION_SERVICE_VALIDATION  = Messages.getString( "IS5Resources.MSG_SESSION_SERVICE_VALIDATION" );  //$NON-NLS-1$
  String MSG_REMOVE_API_SESSION_BEAN     = Messages.getString( "IS5Resources.MSG_REMOVE_API_SESSION_BEAN" );     //$NON-NLS-1$

  String MSG_CREATED_SESSION           = Messages.getString( "IS5Resources.MSG_CREATED_SESSION" );           //$NON-NLS-1$
  String MSG_REMOVED_SESSION           = Messages.getString( "IS5Resources.MSG_REMOVED_SESSION" );           //$NON-NLS-1$
  String MSG_DOJOB_RUN                 = Messages.getString( "IS5Resources.MSG_DOJOB_RUN" );                 //$NON-NLS-1$
  String MSG_PUT_SESSION_TO_CACHE      = Messages.getString( "IS5Resources.MSG_PUT_SESSION_TO_CACHE" );      //$NON-NLS-1$
  String MSG_REMOVE_SESSION_FROM_CACHE = Messages.getString( "IS5Resources.MSG_REMOVE_SESSION_FROM_CACHE" ); //$NON-NLS-1$
  String MSG_CD_READER_CONFIG_FOUND    = Messages.getString( "IS5Resources.MSG_CD_READER_CONFIG_FOUND" );    //$NON-NLS-1$
  String MSG_CD_WRITER_CONFIG_FOUND    = Messages.getString( "IS5Resources.MSG_CD_WRITER_CONFIG_FOUND" );    //$NON-NLS-1$
  String MSG_EVENTS_CONFIG_FOUND       = Messages.getString( "IS5Resources.MSG_EVENTS_CONFIG_FOUND" );       //$NON-NLS-1$
  String MSG_SESSION_EXTENSION_VERIFY  = Messages.getString( "IS5Resources.MSG_SESSION_EXTENSION_VERIFY" );  //$NON-NLS-1$

  String MSG_UPDATE_SESSION_CALLBACK_WRITER = Messages.getString( "IS5Resources.MSG_UPDATE_SESSION_CALLBACK_WRITER" ); //$NON-NLS-1$
  String MSG_CREATE_SESSION_CALLBACK        = Messages.getString( "IS5Resources.MSG_CREATE_SESSION_CALLBACK" );        //$NON-NLS-1$
  String MSG_CREATE_SESSION_CALLBACK_FINISH = Messages.getString( "IS5Resources.MSG_CREATE_SESSION_CALLBACK_FINISH" ); //$NON-NLS-1$
  String MSG_DOJOB_INITED                   = Messages.getString( "IS5Resources.MSG_DOJOB_INITED" );                   //$NON-NLS-1$
  String MSG_DOJOB_START_PAUSE              = Messages.getString( "IS5Resources.MSG_DOJOB_START_PAUSE" );              //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_MSG_SESSION_REJECT            = Messages.getString( "IS5Resources.ERR_MSG_SESSION_REJECT" );            //$NON-NLS-1$
  String ERR_SESSION_EXTENSION_REMOVE      = Messages.getString( "IS5Resources.ERR_SESSION_EXTENSION_REMOVE" );      //$NON-NLS-1$
  String ERR_MSG_SESSION_SERVICE_REMOVE    = Messages.getString( "IS5Resources.ERR_MSG_SESSION_SERVICE_REMOVE" );    //$NON-NLS-1$
  String ERR_MSG_SESSION_NOINFO_REMOVE     = Messages.getString( "IS5Resources.ERR_MSG_SESSION_NOINFO_REMOVE" );     //$NON-NLS-1$
  String ERR_MSG_SERVICE_UNEXPECTED_ERROR  = Messages.getString( "IS5Resources.ERR_MSG_SERVICE_UNEXPECTED_ERROR" );  //$NON-NLS-1$
  String ERR_MSG_SESSION_SERVICE_CLOSE     = Messages.getString( "IS5Resources.ERR_MSG_SESSION_SERVICE_CLOSE" );     //$NON-NLS-1$
  String ERR_MSG_VALID_SERVICE_IS_ROLLBACK = Messages.getString( "IS5Resources.ERR_MSG_VALID_SERVICE_IS_ROLLBACK" ); //$NON-NLS-1$
  String ERR_MSG_VALID_SESSION_IS_BUSY     = Messages.getString( "IS5Resources.ERR_MSG_VALID_SESSION_IS_BUSY" );     //$NON-NLS-1$
  String ERR_MSG_VALID_SERVICE_IS_BUSY     = Messages.getString( "IS5Resources.ERR_MSG_VALID_SERVICE_IS_BUSY" );     //$NON-NLS-1$
  String ERR_MSG_VALID_SESSION_IS_SHUTDOWN = Messages.getString( "IS5Resources.ERR_MSG_VALID_SESSION_IS_SHUTDOWN" ); //$NON-NLS-1$
  String ERR_MSG_VALID_SERVICE_IS_SHUTDOWN = Messages.getString( "IS5Resources.ERR_MSG_VALID_SERVICE_IS_SHUTDOWN" ); //$NON-NLS-1$
  String ERR_MSG_VALID_SESSION_UNEXPECTED  = Messages.getString( "IS5Resources.ERR_MSG_VALID_SESSION_UNEXPECTED" );  //$NON-NLS-1$
  String ERR_MSG_VALID_SERVICE_UNEXPECTED  = Messages.getString( "IS5Resources.ERR_MSG_VALID_SERVICE_UNEXPECTED" );  //$NON-NLS-1$
  String ERR_MSG_CLOSE_SERVICE_IS_ROLLBACK = Messages.getString( "IS5Resources.ERR_MSG_CLOSE_SERVICE_IS_ROLLBACK" ); //$NON-NLS-1$
  String ERR_MSG_CLOSE_SERVICE_IS_BUSY     = Messages.getString( "IS5Resources.ERR_MSG_CLOSE_SERVICE_IS_BUSY" );     //$NON-NLS-1$
  String ERR_MSG_CLOSE_SERVICE_IS_SHUTDOWN = Messages.getString( "IS5Resources.ERR_MSG_CLOSE_SERVICE_IS_SHUTDOWN" ); //$NON-NLS-1$
  String ERR_MSG_CLOSE_SERVICE_UNEXPECTED  = Messages.getString( "IS5Resources.ERR_MSG_CLOSE_SERVICE_UNEXPECTED" );  //$NON-NLS-1$
  String ERR_MSG_CLOSE_RESOURCE            = Messages.getString( "IS5Resources.ERR_MSG_CLOSE_RESOURCE" );            //$NON-NLS-1$
  String ERR_MSG_CLOSE_NOT_INIT_SESSION    = Messages.getString( "IS5Resources.ERR_MSG_CLOSE_NOT_INIT_SESSION" );    //$NON-NLS-1$
  String ERR_MSG_REMOVE_NOT_READY_SESSION  = Messages.getString( "IS5Resources.ERR_MSG_REMOVE_NOT_READY_SESSION" );  //$NON-NLS-1$
  String ERR_MSG_WRONG_PORT                = Messages.getString( "IS5Resources.ERR_MSG_WRONG_PORT" );                //$NON-NLS-1$
  String ERR_SESSION_READONLY              = Messages.getString( "IS5Resources.ERR_SESSION_READONLY" );              //$NON-NLS-1$
  String ERR_SESSION_NOT_FOUND             = Messages.getString( "IS5Resources.ERR_SESSION_NOT_FOUND" );             //$NON-NLS-1$
  String ERR_SESSION_ALREADY_CLOSED        = Messages.getString( "IS5Resources.ERR_SESSION_ALREADY_CLOSED" );        //$NON-NLS-1$
  String ERR_BACKEND_SINGLETON_FAIL        = Messages.getString( "IS5Resources.ERR_BACKEND_SINGLETON_FAIL" );        //$NON-NLS-1$

  String ERR_CREATE_CALLBACK_WRITER         = Messages.getString( "IS5Resources.ERR_CREATE_CALLBACK_WRITER" );             //$NON-NLS-1$
  String ERR_CONFIG_CALLBACK_WRITER         = Messages.getString( "IS5Resources.ERR_CONFIG_CALLBACK_WRITER" );             //$NON-NLS-1$
  String ERR_UNEXPECT_SESSION_RESTORE       = Messages.getString( "IS5Resources.ERR_UNEXPECT_SESSION_RESTORE" );           //$NON-NLS-1$
  String ERR_CLOSED_SESSION_NOT_BE_OPEN     = Messages.getString( "IS5Resources.ERR_CLOSED_SESSION_NOT_BE_OPEN" );         //$NON-NLS-1$
  String ERR_CLOSE_SESSION_BY_CLOSE_PAS     = Messages.getString( "IS5Resources.ERR_CLOSE_SESSION_BY_CLOSE_PAS" );         //$NON-NLS-1$
  String ERR_CLOSE_SESSION                  = Messages.getString( "IS5Resources.ERR_CLOSE_SESSION" );                      //$NON-NLS-1$
  String ERR_CLOSE_SESSION_BY_SEND_FAIL     = Messages.getString( "IS5Resources.ERR_CLOSE_SESSION_BY_SEND_FAIL" );         //$NON-NLS-1$
  String ERR_SESSION_BACKEND_ALREADY_CLOSED = Messages.getString( "IS5Resources.ERR_SESSION_BACKEND_ALREADY_CLOSED" );     //$NON-NLS-1$
  String ERR_CD_READER_CONFIG_NOT_FOUND     = Messages.getString( "IS5Resources.ERR_CD_READER_CONFIG_NOT_FOUND" );         //$NON-NLS-1$
  String ERR_CD_WRITER_CONFIG_NOT_FOUND     = Messages.getString( "IS5Resources.ERR_CD_WRITER_CONFIG_NOT_FOUND" );         //$NON-NLS-1$
  String ERR_EVENTS_NOT_FOUND               = Messages.getString( "IS5Resources.ERR_EVENTS_NOT_FOUND" );                   //$NON-NLS-1$
  String ERR_WRONG_SESSIONS_CALLBACKS       = Messages.getString( "IS5Resources.ERR_WRONG_SESSIONS_CALLBACKS" );           //$NON-NLS-1$
  String ERR_CHECK_SESSIOIN                 = Messages.getString( "IS5Resources.ERR_CHECK_SESSIOIN" );                     //$NON-NLS-1$
  String ERR_CHECK_SESSION_CREATE_CALLBACK  = Messages.getString( "IS5Resources.ERR_CHECK_SESSION_CREATE_CALLBACK" );      //$NON-NLS-1$
  String ERR_CHECK_SESSION_NOT_PRIMARY      = Messages.getString( "IS5Resources.ERR_CHECK_SESSION_NOT_PRIMARY" );          //$NON-NLS-1$
  String ERR_CHECK_SESSION_REMOVE           = Messages.getString( "IS5Resources.ERR_CHECK_SESSION_REMOVE" );               //$NON-NLS-1$
  String ERR_CHECK_CALLBACK                 = Messages.getString( "IS5Resources.ERR_CHECK_CALLBACK" );                     //$NON-NLS-1$
  String ERR_CHECK_CALLBACK_SESSION_INVALID = Messages.getString( "IS5Resources.ERR_CHECK_CALLBACK_SESSION_INVALID" );     //$NON-NLS-1$
  String ERR_CHECK_CALLBACK_CACHED          = Messages.getString( "IS5Resources.ERR_CHECK_CALLBACK_CACHED" );              //$NON-NLS-1$
  String ERR_CALLBACK_NOT_FOUND             = Messages.getString( "IS5Resources.ERR_CALLBACK_NOT_FOUND" );                 //$NON-NLS-1$
  String ERR_WRONG_SESSION                  = Messages.getString( "IS5Resources.ERR_WRONG_SESSION" );                      //$NON-NLS-1$
  String ERR_SK_SESSION_NOT_FOUND           = Messages.getString( "IS5Resources.ERR_SK_SESSION_NOT_FOUND" );               //$NON-NLS-1$
  String ERR_REMOVE_OLD_SESSIONS_CHECK      = Messages.getString( "IS5Resources.ERR_REMOVE_OLD_SESSIONS_CHECK" );          //$NON-NLS-1$
  String ERR_REMOVE_OLD_SESSIONS_START      = Messages.getString( "IS5Resources.ERR_REMOVE_OLD_SESSIONS_START" );          //$NON-NLS-1$
  String ERR_REMOVE_OLD_SESSIONS_FINISH     = Messages.getString( "IS5Resources.ERR_REMOVE_OLD_SESSIONS_FINISH" );         //$NON-NLS-1$
  String ERR_DONT_CALL_DO_BEFORE_CLOSE      = Messages.getString( "IS5Resources.ERR_DONT_CALL_DO_BEFORE_CLOSE" );          //$NON-NLS-1$
  String ERR_SERVER_OVERLOAD                = Messages.getString( "IS5Resources.ERR_SERVER_OVERLOAD" );                    //$NON-NLS-1$
  String ERR_CLOSE_REMOTE_SESSION           = "Ошибка завершения remote-сессии. Причина: %s";                              //$NON-NLS-1$
  String ERR_CLOSE_LOCAL_SESSION            = "Ошибка завершения local-сессии. Причина: %s";                               //$NON-NLS-1$
  String ERR_CALLBACK_WRITER_ALREADY_EXIST  =
      "tryCreateCallbackWriter(...): для сессии %s уже существовал писатель обратных вызовов. Его работа будет завершена"; //$NON-NLS-1$

  String STR_N_SESSION = "Сессия";
  String STR_D_SESSION = "Сессия пользователя подключенного к серверу";

  String STR_N_AID_STARTTIME = "Открыта";
  String STR_D_AID_STARTTIME = "Метка времени (мсек с начала эпохи) открытия сессии";

  String STR_N_AID_ENDTIME = "Закрыта";
  String STR_D_AID_ENDTIME = "Метка времени (мсек с начала эпохи) завершения сессии";

  String STR_N_AID_BACKEND_SPECIFIC_PARAMS = "Backend";
  String STR_D_AID_BACKEND_SPECIFIC_PARAMS = "Специфичные для бекенда параметры";

  String STR_N_AID_CONNECTION_CREATION_PARAMS = "Connection";
  String STR_D_AID_CONNECTION_CREATION_PARAMS = "Параметры создания соединения";

  String STR_N_LNK_USER = "Пользователь";
  String STR_D_LNK_USER = "Пользователь, который вошел в систему";
}
