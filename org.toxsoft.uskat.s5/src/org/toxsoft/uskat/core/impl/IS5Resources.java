package org.toxsoft.uskat.core.impl;

/**
 * Локализуемые ресурсы реализации службы системного описания.
 *
 * @author mvk
 */
interface IS5Resources {

  // ------------------------------------------------------------------------------------
  // Тексты сообщений
  //
  String MSG_REGISTER_EVENT_GWID = Messages.getString( "IS5Resources.MSG_REGISTER_EVENT_GWID" ); //$NON-NLS-1$
  String MSG_REGISTER_CMD_GWID   = Messages.getString( "IS5Resources.MSG_REGISTER_CMD_GWID" );   //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String MSG_ERR_IGNORE_GWID_BY_KIND = Messages.getString( "IS5Resources.MSG_ERR_IGNORE_GWID_BY_KIND" ); //$NON-NLS-1$
  String MSG_ERR_HANDER_ON_EVENTS    = Messages.getString( "IS5Resources.MSG_ERR_HANDER_ON_EVENTS" );    //$NON-NLS-1$
  String MSG_ERR_EXEC_CMD_MAX        = Messages.getString( "IS5Resources.MSG_ERR_EXEC_CMD_MAX" );        //$NON-NLS-1$
}
