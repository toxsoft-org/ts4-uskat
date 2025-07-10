package org.toxsoft.uskat.skadmin.core.plugins;

import static org.toxsoft.uskat.skadmin.core.plugins.IAdminResources.*;

import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.plugins.*;
import org.toxsoft.uskat.skadmin.core.impl.*;

/**
 * Абстрактный плагин команд s5admin
 *
 * @author mvk
 */
public abstract class AbstractPluginCmdLibrary
    extends AbstractAdminCmdLibrary
    implements IAdminCmdLibraryPlugin {

  /**
   * Информация о плагине
   */
  private IPluginInfo pluginInfo;

  // ------------------------------------------------------------------------------------
  // IAdminCmdLibraryPlugin
  //
  @Override
  public IPluginInfo plugInfo() {
    TsIllegalStateRtException.checkTrue( isClosed(), MSG_ERR_CLOSED );
    return pluginInfo;
  }

  @Override
  public void initPlugin( IPluginInfo aPluginInfo ) {
    TsNullArgumentRtException.checkNulls( aPluginInfo );
    TsIllegalStateRtException.checkTrue( isClosed(), MSG_ERR_CLOSED );
    pluginInfo = aPluginInfo;
    // Обработка инициализации наследниками
    doInit();
  }

}
