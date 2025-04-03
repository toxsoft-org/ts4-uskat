package org.toxsoft.uskat.s5.server.sessions.pas;

/**
 * Локализуемые ресурсы .
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IS5Resources {

  String MSG_SEND_EVENTS                   = Messages.getString( "IS5Resources.MSG_SEND_EVENTS" );
  String MSG_SEND_COMMAND                  = Messages.getString( "IS5Resources.MSG_SEND_COMMAND" );
  String MSG_SEND_COMMAND_STATES           = Messages.getString( "IS5Resources.MSG_SEND_COMMAND_STATES" );
  String MSG_CREATE_CALLBACK_WRITER        = Messages.getString( "IS5Resources.MSG_CREATE_CALLBACK_WRITER" );
  String MSG_DOJOB_RUN                     = Messages.getString( "IS5Resources.MSG_DOJOB_RUN" );
  String MSG_FIND_CHANNEL                  =
      "findChannel(...): aSessionID = %s, aRemoteAddr = %s, aRemotePort = %d, channels(%d): %s";
  String MSG_RECEIVED_INIT_SESSION_MESSAGE =
      "doOnReceived(...): received INIT SESSION message. aSource = %s, aMessage = %s";
  String FMT_CHANNEL                       = "\n[%d] sessionID = %s, %s %s";
  String FMT_SUITABLE                      = " - IS SUITABLE";

  String ERR_SEND_NOT_CONNECTION              = Messages.getString( "IS5Resources.ERR_SEND_NOT_CONNECTION" );
  String ERR_SEND_EVENTS                      = Messages.getString( "IS5Resources.ERR_SEND_EVENTS" );
  String ERR_CLOSE_BY_NOT_CONNECTION          = Messages.getString( "IS5Resources.ERR_CLOSE_BY_NOT_CONNECTION" );
  String ERR_CREATE_CALLBACK_WRITER           = Messages.getString( "IS5Resources.ERR_CREATE_CALLBACK_WRITER" );
  String ERR_NO_CHANNEL_SESSION               = Messages.getString( "IS5Resources.ERR_NO_CHANNEL_SESSION" );
  String ERR_FOUND_DUPLICATE_CHANNEL          = Messages.getString( "IS5Resources.ERR_FOUND_DUPLICATE_CHANNEL" );
  String ERR_CHANNEL_SESSION_NOT_FOUND        = Messages.getString( "IS5Resources.ERR_CHANNEL_SESSION_NOT_FOUND" );
  String ERR_CHANNEL_SESSION_NOT_VALID        = Messages.getString( "IS5Resources.ERR_CHANNEL_SESSION_NOT_VALID" );
  String ERR_CLOSED_CHANNEL_SESSION_NOT_FOUND =
      Messages.getString( "IS5Resources.ERR_CLOSED_CHANNEL_SESSION_NOT_FOUND" );
  String ERR_CANT_CLOSE_NULL_SESSION          = Messages.getString( "IS5Resources.ERR_CANT_CLOSE_NULL_SESSION" );
  String ERR_DETECT_BREAK_CONNECTION          = Messages.getString( "IS5Resources.ERR_DETECT_BREAK_CONNECTION" );
  String ERR_CHANNEL_NO_WILDFLY_ID            =
      "statistic(): У канала нет идентификатора сессии (sessionID = null). %s";
  String ERR_CHANNEL_NO_SESSION               =
      "statistic(): Не найдена сессия канала (sessionID = %s, session = null). %s";
  String ERR_CHANNEL_NO_STATISTIC             =
      "statistic(): Не найдена статистика канала (sessionID = %s, statistic = null). %s";
  String ERR_CHANNEL_NOT_FOUND                = "\nresult: channel is not found!";
  String ERR_FOUND_SESSION_DOUBLE             =
      "findSessionChannel(...): found the channel with same session sessionID = %s: channel0  = [%s], channel1 = [%s]";
}
