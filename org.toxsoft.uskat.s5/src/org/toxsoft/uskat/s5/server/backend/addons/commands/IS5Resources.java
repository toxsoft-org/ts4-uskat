package org.toxsoft.uskat.s5.server.backend.addons.commands;

/**
 * Локализуемые ресурсы реализации.
 *
 * @author mvk
 */
interface IS5Resources {

  /**
   * {@link S5BaCommandsSupport}
   */
  String MSG_REGISTER_CMD_GWID   = Messages.getString( "IS5Resources.MSG_REGISTER_CMD_GWID" );   //$NON-NLS-1$
  String ERR_EXEC_CMD_MAX        = Messages.getString( "IS5Resources.ERR_EXEC_CMD_MAX" );        //$NON-NLS-1$
  String ERR_IGNORE_GWID_BY_KIND = Messages.getString( "IS5Resources.ERR_IGNORE_GWID_BY_KIND" ); //$NON-NLS-1$
}
