package org.toxsoft.uskat.s5.server.sessions.init;

/**
 * Константы, локализуемые ресурсы.
 *
 * @author mvk
 */
interface IS5Resources {

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_CREATE_SESSION_START = Messages.getString( "IS5Resources.MSG_CREATE_SESSION_START" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_MSG_ADDON_DATA_NO_SERIALIZABLE = Messages.getString( "IS5Resources.ERR_MSG_ADDON_DATA_NO_SERIALIZABLE" ); //$NON-NLS-1$
  String ERR_MSG_ADDON_DATA_ALREADY_EXIST   = Messages.getString( "IS5Resources.ERR_MSG_ADDON_DATA_ALREADY_EXIST" );   //$NON-NLS-1$
}
