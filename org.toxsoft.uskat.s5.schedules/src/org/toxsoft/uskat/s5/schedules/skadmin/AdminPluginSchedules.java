package org.toxsoft.uskat.s5.schedules.skadmin;

import org.toxsoft.uskat.s5.schedules.skadmin.schedules.*;
import org.toxsoft.uskat.skadmin.core.plugins.AbstractPluginCmdLibrary;

/**
 * Плагин s5admin: команды управления расписаниями
 *
 * @author mvk
 */
public class AdminPluginSchedules
    extends AbstractPluginCmdLibrary {

  /**
   * ИД-путь команд которые находятся в плагине
   */
  public static final String SCHEDULES_CMD_PATH = "sk.s5.schedules."; //$NON-NLS-1$

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
