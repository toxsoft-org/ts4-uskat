package org.toxsoft.uskat.skadmin.logon;

import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;

import org.toxsoft.uskat.core.connection.ISkConnection;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.skadmin.core.plugins.AbstractPluginCmdLibrary;

/**
 * Плагин административных команд skadmin: вход на сервер
 *
 * @author mvk
 */
public class AdminPluginLogon
    extends AbstractPluginCmdLibrary {

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractPluginCmdLibrary
  //
  @Override
  public String getName() {
    return getClass().getName();
  }

  @Override
  protected void doInit() {
    // Формирование библиотеки команд плагина
    addCmd( new AdminCmdConnect() );
    addCmd( new AdminCmdDisconnect() );
    addCmd( new AdminCmdGetConnection() );
    addCmd( new AdminCmdInfo() );
  }

  @Override
  protected void doClose() {
    logger().debug( "doClose" ); //$NON-NLS-1$
    // Закрываем соединение если оно не закрыто
    IPlexyValue pxConnection = context().paramValueOrNull( CTX_SK_CONNECTION );
    if( pxConnection != null ) {
      ISkConnection connection = (ISkConnection)pxConnection.singleRef();
      logger().debug( "closeConnection" ); //$NON-NLS-1$
      connection.close();
    }
  }
}
