package org.toxsoft.uskat.sysext.batchop.addon;

import ru.uskat.backend.addons.batchops.ISkBackendAddonBatchOperations;

/**
 * Локализуемые ресурсы реализации.
 *
 * @author mvk
 */
interface ISkResources {

  String STR_N_BACKEND_BATCH = Messages.getString( "ISkResources.STR_N_BACKEND_BATCH" ); //$NON-NLS-1$
  String STR_D_BACKEND_BATCH = Messages.getString( "ISkResources.STR_D_BACKEND_BATCH" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //

  String STR_D_BACKEND_ADDON_BATCH_OPERATIONS =
      Messages.getString( "ISkResources.STR_D_BACKEND_ADDON_BATCH_OPERATIONS" ) //$NON-NLS-1$
          + ISkBackendAddonBatchOperations.SK_BACKEND_ADDON_ID
          + Messages.getString( "ISkResources.STR_D_BACKEND_ADDON_BATCH_OPERATIONS___1" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  // String ERR_NO_DEFAULT_VALUE =
  // "Класс %s. Текущее данное %s имеет тип %s для которого требуется, но неопределено значение по умолчанию";

}
