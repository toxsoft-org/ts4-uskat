package org.toxsoft.uskat.s5.client.remote.addons;

import ru.uskat.backend.addons.batchops.ISkBackendAddonBatchOperations;
import ru.uskat.backend.addons.realtime.ISkBackendAddonRealtime;

/**
 * Константы, локализуемые ресурсы .
 *
 * @author mvk
 */
interface IS5Resources {

  String STR_N_BACKEND_ADDONS = Messages.getString( "IS5Resources.STR_N_BACKEND_ADDONS" ); //$NON-NLS-1$
  String STR_D_BACKEND_ADDONS = Messages.getString( "IS5Resources.STR_D_BACKEND_ADDONS" ); //$NON-NLS-1$

  String STR_D_BACKEND_ADDON_BATCH_OPERATIONS =
      Messages.getString( "IS5Resources.STR_D_BACKEND_ADDON_BATCH_OPERATIONS" ) //$NON-NLS-1$
          + ISkBackendAddonBatchOperations.SK_BACKEND_ADDON_ID
          + Messages.getString( "IS5Resources.STR_D_BACKEND_ADDON_BATCH_OPERATIONS___1" ); //$NON-NLS-1$

  String STR_D_BACKEND_ADDON_REALTIME =
      Messages.getString( "IS5Resources.STR_D_BACKEND_ADDON_REALTIME" ) + ISkBackendAddonRealtime.SK_BACKEND_ADDON_ID //$NON-NLS-1$
          + Messages.getString( "IS5Resources.STR_D_BACKEND_ADDON_REALTIME___1" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_REGISTER_CMD_GWIDS = Messages.getString( "IS5Resources.MSG_REGISTER_CMD_GWIDS" ); //$NON-NLS-1$
  String MSG_TRANSMIT_VALUES    = Messages.getString( "IS5Resources.MSG_TRANSMIT_VALUES" );    //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_NO_CONNECTION           = Messages.getString( "IS5Resources.ERR_NO_CONNECTION" );           //$NON-NLS-1$
  String ERR_BACKEND_ADDON_NOT_FOUND = Messages.getString( "IS5Resources.ERR_BACKEND_ADDON_NOT_FOUND" ); //$NON-NLS-1$
  String ERR_ON_EVENTS               = Messages.getString( "IS5Resources.ERR_ON_EVENTS" );               //$NON-NLS-1$

}
