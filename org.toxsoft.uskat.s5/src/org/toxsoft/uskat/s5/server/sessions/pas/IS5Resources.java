package org.toxsoft.uskat.s5.server.sessions.pas;

/**
 * Локализуемые ресурсы .
 *
 * @author mvk
 */
interface IS5Resources {

  String MSG_SEND_EVENTS            = Messages.getString( "IS5Resources.MSG_SEND_EVENTS" );            //$NON-NLS-1$
  String MSG_SEND_COMMAND           = Messages.getString( "IS5Resources.MSG_SEND_COMMAND" );           //$NON-NLS-1$
  String MSG_SEND_COMMAND_STATES    = Messages.getString( "IS5Resources.MSG_SEND_COMMAND_STATES" );    //$NON-NLS-1$
  String MSG_CREATE_CALLBACK_WRITER = Messages.getString( "IS5Resources.MSG_CREATE_CALLBACK_WRITER" ); //$NON-NLS-1$
  String MSG_DOJOB_RUN              = Messages.getString( "IS5Resources.MSG_DOJOB_RUN" );              //$NON-NLS-1$

  String ERR_SEND_NOT_CONNECTION              = Messages.getString( "IS5Resources.ERR_SEND_NOT_CONNECTION" );       //$NON-NLS-1$
  String ERR_SEND_EVENTS                      = Messages.getString( "IS5Resources.ERR_SEND_EVENTS" );               //$NON-NLS-1$
  String ERR_CLOSE_BY_NOT_CONNECTION          = Messages.getString( "IS5Resources.ERR_CLOSE_BY_NOT_CONNECTION" );   //$NON-NLS-1$
  String ERR_CREATE_CALLBACK_WRITER           = Messages.getString( "IS5Resources.ERR_CREATE_CALLBACK_WRITER" );    //$NON-NLS-1$
  String ERR_NO_CHANNEL_SESSION               = Messages.getString( "IS5Resources.ERR_NO_CHANNEL_SESSION" );        //$NON-NLS-1$
  String ERR_FOUND_DUPLICATE_CHANNEL          = Messages.getString( "IS5Resources.ERR_FOUND_DUPLICATE_CHANNEL" );   //$NON-NLS-1$
  String ERR_CHANNEL_SESSION_NOT_FOUND        = Messages.getString( "IS5Resources.ERR_CHANNEL_SESSION_NOT_FOUND" ); //$NON-NLS-1$
  String ERR_CHANNEL_SESSION_NOT_VALID        = Messages.getString( "IS5Resources.ERR_CHANNEL_SESSION_NOT_VALID" ); //$NON-NLS-1$
  String ERR_CLOSED_CHANNEL_SESSION_NOT_FOUND =
      Messages.getString( "IS5Resources.ERR_CLOSED_CHANNEL_SESSION_NOT_FOUND" );                                    //$NON-NLS-1$
  String ERR_CANT_CLOSE_NULL_SESSION          = Messages.getString( "IS5Resources.ERR_CANT_CLOSE_NULL_SESSION" );   //$NON-NLS-1$
  String ERR_DETECT_BREAK_CONNECTION          = Messages.getString( "IS5Resources.ERR_DETECT_BREAK_CONNECTION" );   //$NON-NLS-1$
  String ERR_CHANNEL_NO_WILDFLY_ID            =
      "statistic(): У канала нет идентификатора сессии (sessionID = null). %s";                                     //$NON-NLS-1$
  String ERR_CHANNEL_NO_SESSION               =
      "statistic(): Не найдена сессия канала (sessionID = %s, session = null). %s";                                 //$NON-NLS-1$
  String ERR_CHANNEL_NO_STATISTIC             =
      "statistic(): Не найдена статистика канала (sessionID = %s, statistic = null). %s";                           //$NON-NLS-1$
}
