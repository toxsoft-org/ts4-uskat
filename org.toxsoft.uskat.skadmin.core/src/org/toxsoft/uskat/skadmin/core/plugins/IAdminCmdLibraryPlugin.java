package org.toxsoft.uskat.skadmin.core.plugins;

import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.plugins.*;
import org.toxsoft.uskat.skadmin.core.*;

/**
 * Interface of the skamin tool plugin (the commands provider for skadmin).
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public interface IAdminCmdLibraryPlugin
    extends IAdminCmdLibrary {

  /**
   * The skadmin tool plugin JAR file type ID for TS plugin manager.
   * <p>
   * This constant must be used in plugin JAR file's manifest under the ke {@link IPluginsHardConstants#PLUGIN_TYPE_ID}
   * to identify JAR-file as the skadmin tool plugin.
   */
  String CMD_LIBRARY_PLUGIN_TYPE = "skadmin.library";

  /**
   * Returns information about the skadmin plugin.
   * <p>
   * Method returns value specified during initialization as an argument of {@link #initPlugin(IPluginInfo)} method.
   *
   * @return {@link IPluginInfo} - the plugin information
   */
  IPluginInfo plugInfo();

  /**
   * Called by the skadmin tool when using plugin.
   * <p>
   * The plugin implementation must save argument and register provided commands and perform any initialization if
   * needed.
   *
   * @param aPluginInfo {@link IPluginInfo} -
   * @throws TsNullArgumentRtException аргумент = null
   */
  void initPlugin( IPluginInfo aPluginInfo );

}
