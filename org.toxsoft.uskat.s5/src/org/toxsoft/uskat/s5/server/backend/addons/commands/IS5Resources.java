package org.toxsoft.uskat.s5.server.backend.addons.commands;

/**
 * Локализуемые ресурсы реализации.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IS5Resources {

  /**
   * {@link S5BaCommandsSupport}
   */
  String MSG_REGISTER_CMD_GWID   = Messages.getString( "IS5Resources.MSG_REGISTER_CMD_GWID" );
  String ERR_EXEC_CMD_MAX        = Messages.getString( "IS5Resources.ERR_EXEC_CMD_MAX" );
  String ERR_TEST_CMD_MAX        = "Test command queue overflow > %d. Command '%s' removed from queue";
  String ERR_IGNORE_GWID_BY_KIND = Messages.getString( "IS5Resources.ERR_IGNORE_GWID_BY_KIND" );
}
