package org.toxsoft.uskat.s5.server.backend.impl;

/**
 * Константы, локализуемые ресурсы .
 *
 * @author mvk
 */
interface IS5Resources {

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_CREATE_SESSION_REQUEST      = Messages.getString( "IS5Resources.MSG_CREATE_SESSION_REQUEST" );      //$NON-NLS-1$
  String MSG_SESSION_INITIALIZE          = "initialize session";                                                 //$NON-NLS-1$
  String MSG_SESSION_ACTIVATE            = Messages.getString( "IS5Resources.MSG_SESSION_ACTIVATE" );            //$NON-NLS-1$
  String MSG_SESSION_PASSIVATE           = Messages.getString( "IS5Resources.MSG_SESSION_PASSIVATE" );           //$NON-NLS-1$
  String MSG_SESSION_REMOVE              = Messages.getString( "IS5Resources.MSG_SESSION_REMOVE" );              //$NON-NLS-1$
  String MSG_CREATE_SESSION_START        = Messages.getString( "IS5Resources.MSG_CREATE_SESSION_START" );        //$NON-NLS-1$
  String MSG_CREATE_SESSION_FINISH       = Messages.getString( "IS5Resources.MSG_CREATE_SESSION_FINISH" );       //$NON-NLS-1$
  String MSG_SESSION_VERIFY_START        = Messages.getString( "IS5Resources.MSG_SESSION_VERIFY_START" );        //$NON-NLS-1$
  String MSG_SESSION_VERIFY_FINISH       = Messages.getString( "IS5Resources.MSG_SESSION_VERIFY_FINISH" );       //$NON-NLS-1$
  String MSG_SESSION_QUERY_BEFORE_REMOVE = Messages.getString( "IS5Resources.MSG_SESSION_QUERY_BEFORE_REMOVE" ); //$NON-NLS-1$
  String MSG_BEFORE_REMOVE_SESSION       = Messages.getString( "IS5Resources.MSG_BEFORE_REMOVE_SESSION" );       //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_SESSION_CLOSE               = Messages.getString( "IS5Resources.ERR_SESSION_CLOSE" );               //$NON-NLS-1$
  String ERR_SESSION_REMOVE              = Messages.getString( "IS5Resources.ERR_SESSION_REMOVE" );              //$NON-NLS-1$
  String ERR_DOUBLE_INIT                 = Messages.getString( "IS5Resources.ERR_DOUBLE_INIT" );                 //$NON-NLS-1$
  String ERR_UNEXPECTED_EXTENSION_INIT   = Messages.getString( "IS5Resources.ERR_UNEXPECTED_EXTENSION_INIT" );   //$NON-NLS-1$
  String ERR_UNEXPECTED_EXTENSION_CHECK2 = Messages.getString( "IS5Resources.ERR_UNEXPECTED_EXTENSION_CHECK2" ); //$NON-NLS-1$
  String ERR_UNEXPECTED_EXTENSION_CHECK  = Messages.getString( "IS5Resources.ERR_UNEXPECTED_EXTENSION_CHECK" );  //$NON-NLS-1$
  String ERR_SESSION_NOT_FOUND           = Messages.getString( "IS5Resources.ERR_SESSION_NOT_FOUND" );           //$NON-NLS-1$
  String ERR_SESSION_ALREADY_CLOSED      = Messages.getString( "IS5Resources.ERR_SESSION_ALREADY_CLOSED" );      //$NON-NLS-1$
  String ERR_BEFORE_REMOVE_ALREADY       = Messages.getString( "IS5Resources.ERR_BEFORE_REMOVE_ALREADY" );       //$NON-NLS-1$
  String ERR_UNEXPECTED_EXTENSION_CLOSE  = Messages.getString( "IS5Resources.ERR_UNEXPECTED_EXTENSION_CLOSE" );  //$NON-NLS-1$
  String ERR_API_UNEXPECTED_ERROR        = Messages.getString( "IS5Resources.ERR_API_UNEXPECTED_ERROR" );        //$NON-NLS-1$
  String ERR_WRONG_USER                  = Messages.getString( "IS5Resources.ERR_WRONG_USER" );                  //$NON-NLS-1$
  String ERR_WRONG_VERSION               = Messages.getString( "IS5Resources.ERR_WRONG_VERSION" );               //$NON-NLS-1$
  String ERR_REJECT_CONNECT              = "Отказ подключения к системе клиента %s[%s:%s]. Причина: %s";         //$NON-NLS-1$
  String ERR_UNEXPECTED_CONNECT_FAIL     = "Неожиданная ошибка подключения клиента %s[%s:%s]. Причина: %s";      //$NON-NLS-1$
}
