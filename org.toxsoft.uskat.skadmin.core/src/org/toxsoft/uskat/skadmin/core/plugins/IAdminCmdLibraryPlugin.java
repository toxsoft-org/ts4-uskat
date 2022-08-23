package org.toxsoft.uskat.skadmin.core.plugins;

import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.legacy.plugins.IPluginInfo;
import org.toxsoft.uskat.skadmin.core.IAdminCmdLibrary;

/**
 * Плагин инструмента администратора
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public interface IAdminCmdLibraryPlugin
    extends IAdminCmdLibrary {

  /**
   * Тип плагинов исполняющих наборы команд skadmin
   */
  String CMD_LIBRARY_PLUGIN_TYPE = "skadmin.library";

  /**
   * Информация о плагине
   *
   * @return {@link IPluginInfo} информация о плагине
   */
  IPluginInfo plugInfo();

  /**
   * Инициализировать плагин представляющий библиотеку команд
   *
   * @param aPluginInfo {@link IPluginInfo} информация о плагине
   * @throws TsNullArgumentRtException аргумент = null
   */
  void initPlugin( IPluginInfo aPluginInfo );

}
