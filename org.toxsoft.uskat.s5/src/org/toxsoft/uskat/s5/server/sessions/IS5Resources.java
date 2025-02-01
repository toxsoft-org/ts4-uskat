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
  String MSG_DOJOB_RUN                      = Messages.getString( "IS5Resources.MSG_DOJOB_RUN" );                      //$NON-NLS-1$
  String MSG_UPDATE_SESSION_CALLBACK_WRITER = Messages.getString( "IS5Resources.MSG_UPDATE_SESSION_CALLBACK_WRITER" ); //$NON-NLS-1$
  String MSG_CREATE_SESSION_CALLBACK        = Messages.getString( "IS5Resources.MSG_CREATE_SESSION_CALLBACK" );        //$NON-NLS-1$
  String MSG_CREATE_SESSION_CALLBACK_FINISH = Messages.getString( "IS5Resources.MSG_CREATE_SESSION_CALLBACK_FINISH" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_SESSION_NOT_FOUND              = Messages.getString( "IS5Resources.ERR_SESSION_NOT_FOUND" );               //$NON-NLS-1$
  String ERR_WRONG_SESSIONS_CALLBACKS       = Messages.getString( "IS5Resources.ERR_WRONG_SESSIONS_CALLBACKS" );        //$NON-NLS-1$
  String ERR_CHECK_SESSION_REMOVE           = Messages.getString( "IS5Resources.ERR_CHECK_SESSION_REMOVE" );            //$NON-NLS-1$
  String ERR_CHECK_CALLBACK                 = Messages.getString( "IS5Resources.ERR_CHECK_CALLBACK" );                  //$NON-NLS-1$
  String ERR_CHECK_CALLBACK_SESSION_INVALID = Messages.getString( "IS5Resources.ERR_CHECK_CALLBACK_SESSION_INVALID" );  //$NON-NLS-1$
  String ERR_CHECK_CALLBACK_CACHED          = Messages.getString( "IS5Resources.ERR_CHECK_CALLBACK_CACHED" );           //$NON-NLS-1$
  String ERR_CALLBACK_NOT_FOUND             = Messages.getString( "IS5Resources.ERR_CALLBACK_NOT_FOUND" );              //$NON-NLS-1$
  String ERR_SK_SESSION_NOT_FOUND           = Messages.getString( "IS5Resources.ERR_SK_SESSION_NOT_FOUND" );            //$NON-NLS-1$
  String ERR_REMOVE_OLD_SESSIONS_CHECK      = Messages.getString( "IS5Resources.ERR_REMOVE_OLD_SESSIONS_CHECK" );       //$NON-NLS-1$
  String ERR_REMOVE_OLD_SESSIONS_START      = Messages.getString( "IS5Resources.ERR_REMOVE_OLD_SESSIONS_START" );       //$NON-NLS-1$
  String ERR_REMOVE_OLD_SESSIONS_FINISH     = Messages.getString( "IS5Resources.ERR_REMOVE_OLD_SESSIONS_FINISH" );      //$NON-NLS-1$
  String ERR_CLOSE_REMOTE_SESSION           = "Ошибка завершения remote-сессии. Причина: %s";                           //$NON-NLS-1$
  String ERR_CLOSE_LOCAL_SESSION            = "Ошибка завершения local-сессии. Причина: %s";                            //$NON-NLS-1$
  String ERR_CALLBACK_WRITER_ALREADY_EXIST  =
      "tryCreateMessenger(...): для сессии %s уже существовал приемопередачтик соообщений. Его работа будет завершена"; //$NON-NLS-1$
}
