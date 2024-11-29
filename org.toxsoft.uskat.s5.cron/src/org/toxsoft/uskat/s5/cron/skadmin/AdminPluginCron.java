package org.toxsoft.uskat.s5.cron.skadmin;

import org.toxsoft.uskat.s5.cron.skadmin.cron.*;
import org.toxsoft.uskat.skadmin.core.plugins.AbstractPluginCmdLibrary;

/**
 * Плагин s5admin: команды управления расписаниями
 *
 * @author mvk
 */
public class AdminPluginCron
    extends AbstractPluginCmdLibrary {

  /**
   * ИД-путь команд которые находятся в плагине
   */
  public static final String SCHEDULES_CMD_PATH = "sk.s5.cron."; //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractPluginCmdLibrary
  //
  @Override
  public String getName() {
    return getClass().getName();
  }

  @Override
  protected void doInit() {
    // Расписания
    addCmd( new AdminCmdListSchedules() );
    addCmd( new AdminCmdRemoveSchedule() );
    addCmd( new AdminCmdAddSchedule() );
  }

  @Override
  protected void doClose() {
    // nop
  }
}
