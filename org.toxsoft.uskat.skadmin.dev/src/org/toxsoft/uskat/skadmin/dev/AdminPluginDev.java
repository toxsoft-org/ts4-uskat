package org.toxsoft.uskat.skadmin.dev;

import org.toxsoft.uskat.skadmin.core.plugins.AbstractPluginCmdLibrary;
import org.toxsoft.uskat.skadmin.dev.batch.AdminCmdBatchRead;
import org.toxsoft.uskat.skadmin.dev.commands.AdminCmdExecutor;
import org.toxsoft.uskat.skadmin.dev.commands.AdminCmdSend;
import org.toxsoft.uskat.skadmin.dev.events.*;
import org.toxsoft.uskat.skadmin.dev.lobs.*;
import org.toxsoft.uskat.skadmin.dev.objects.AdminCmdGetAttr;
import org.toxsoft.uskat.skadmin.dev.objects.AdminCmdSetAttr;
import org.toxsoft.uskat.skadmin.dev.pas.*;
import org.toxsoft.uskat.skadmin.dev.rtdata.AdminCmdRead;
import org.toxsoft.uskat.skadmin.dev.rtdata.AdminCmdWrite;

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
  public static final String DEV_CMD_PATH = "sk.dev."; //$NON-NLS-1$

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
    // lobs
    addCmd( new AdminCmdListIds() );
    addCmd( new AdminCmdExportClobs() );
    addCmd( new AdminCmdImportClobs() );
    // Пакетная обработка
    addCmd( new AdminCmdBatchRead() );
    // Команды
    addCmd( new AdminCmdSend() );
    addCmd( new AdminCmdExecutor() );
    // События
    addCmd( new AdminCmdFire() );
    addCmd( new AdminCmdReceiver() );
    addCmd( new AdminCmdQuery() );
    // Текущие данные
    addCmd( new AdminCmdRead() );
    addCmd( new AdminCmdWrite() );
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
