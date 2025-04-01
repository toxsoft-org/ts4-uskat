package org.toxsoft.uskat.skadmin.dev;

import org.toxsoft.uskat.skadmin.core.plugins.*;
import org.toxsoft.uskat.skadmin.dev.commands.*;
import org.toxsoft.uskat.skadmin.dev.events.*;
import org.toxsoft.uskat.skadmin.dev.objects.*;
import org.toxsoft.uskat.skadmin.dev.pas.*;
import org.toxsoft.uskat.skadmin.dev.rtdata.*;

/**
 * Плагин s5admin: команды разработчика
 *
 * @author mvk
 */
public class AdminPluginDev
    extends AbstractPluginCmdLibrary {

  /**
   * ИД-путь команд которые находятся в плагине
   */
  public static final String CMD_PATH = "sk."; //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractPluginCmdLibrary
  //
  @Override
  public String getName() {
    return getClass().getName();
  }

  @Override
  protected void doInit() {
    // Объекты
    addCmd( new AdminCmdGetAttr() );
    addCmd( new AdminCmdSetAttr() );
    addCmd( new AdminCmdRemoveObject() );
    // Данные
    addCmd( new AdminCmdRead() );
    addCmd( new AdminCmdWrite() );
    addCmd( new AdminCmdWriteTest() );
    // Команды
    addCmd( new AdminCmdSend() );
    addCmd( new AdminCmdExecutor() );
    // События
    addCmd( new AdminCmdFire() );
    addCmd( new AdminCmdReceiver() );
    // PAS
    addCmd( new AdminCmdPasConnect() );
    addCmd( new AdminCmdPasRequest() );
    addCmd( new AdminCmdPasNotify() );
    addCmd( new AdminCmdPasClose() );
  }

  @Override
  protected void doClose() {
    // nop
  }
}
