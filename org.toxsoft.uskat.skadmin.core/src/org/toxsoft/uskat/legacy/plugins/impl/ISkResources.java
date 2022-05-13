package org.toxsoft.uskat.legacy.plugins.impl;

import org.toxsoft.uskat.legacy.plugins.IPluginsHardConstants;

/**
 * Локализуемые ресурсы реализации подсистемы подключаемых модулей (плагинов).
 *
 * @author goga
 */
interface ISkResources {

  /**
   * {@link ChangedPluginsInfo}
   */
  String MSG_ERR_STATE_TABLE_UNSOLVED = Messages.getString( "ISkResources.MSG_ERR_STATE_TABLE_UNSOLVED" ); //$NON-NLS-1$

  /**
   * {@link PluginUtils}
   */
  String MSG_ERR_NO_MANIFEST                   = Messages.getString( "ISkResources.MSG_ERR_NO_MANIFEST" );                      //$NON-NLS-1$
  String MSG_ERR_NOT_PLUGIN_CONTAINER          = Messages.getString( "ISkResources.MSG_ERR_NOT_PLUGIN_CONTAINER" )              //$NON-NLS-1$
      + IPluginsHardConstants.MF_MAIN_ATTR_PLUGIN_CONTAINER_VERSION
      + Messages.getString( "ISkResources.MSG_ERR_NOT_PLUGIN_CONTAINER___1" );                                                  //$NON-NLS-1$
  String MSG_ERR_INV_CONTAINER_VERSION_FORMAT  =
      Messages.getString( "ISkResources.MSG_ERR_INV_CONTAINER_VERSION_FORMAT" )                                                 //$NON-NLS-1$
          + IPluginsHardConstants.MF_MAIN_ATTR_PLUGIN_CONTAINER_VERSION
          + Messages.getString( "ISkResources.MSG_ERR_INV_CONTAINER_VERSION_FORMAT___1" );                                      //$NON-NLS-1$
  String MSG_ERR_INV_CONTAINER_VERSION         = Messages.getString( "ISkResources.MSG_ERR_INV_CONTAINER_VERSION" );            //$NON-NLS-1$
  String MSG_ERR_INCOPLETE_PLUGIN_INFO_SECTION =
      Messages.getString( "ISkResources.MSG_ERR_INCOPLETE_PLUGIN_INFO_SECTION" );                                               //$NON-NLS-1$
  String MSG_ERR_PLUGIN_ID_NOT_ID_PATH         = Messages.getString( "ISkResources.MSG_ERR_PLUGIN_ID_NOT_ID_PATH" );            //$NON-NLS-1$
  String MSG_ERR_PLUGIN_TYPE_NOT_ID_PATH       = Messages.getString( "ISkResources.MSG_ERR_PLUGIN_TYPE_NOT_ID_PATH" );          //$NON-NLS-1$
  String MSG_ERR_INV_PLUGIN_VERSION            = Messages.getString( "ISkResources.MSG_ERR_INV_PLUGIN_VERSION" );               //$NON-NLS-1$

  /**
   * {@link PluginStorage}
   */
  String MSG_ERR_CANT_CREATE_PLUGIN_OBJECT    = Messages.getString( "ISkResources.MSG_ERR_CANT_CREATE_PLUGIN_OBJECT" );  //$NON-NLS-1$
  String MSG_ERR_CANT_RESOLVE_DEPENDENCE_TYPE =
      Messages.getString( "ISkResources.MSG_ERR_CANT_RESOLVE_DEPENDENCE_TYPE" );                                         //$NON-NLS-1$
  String MSG_ERR_FOR_DEPENDENCE               = Messages.getString( "ISkResources.MSG_ERR_FOR_DEPENDENCE" );             //$NON-NLS-1$
  String MSG_ERR_EXACT_VERSION_NUMBER         = Messages.getString( "ISkResources.MSG_ERR_EXACT_VERSION_NUMBER" );       //$NON-NLS-1$
  String MSG_ERR_NEED_NEWER_VERSION_NUMBER    = Messages.getString( "ISkResources.MSG_ERR_NEED_NEWER_VERSION_NUMBER" );  //$NON-NLS-1$
  String MSG_ERR_AVAILABLE_VERSION_NUMBER     = Messages.getString( "ISkResources.MSG_ERR_AVAILABLE_VERSION_NUMBER" );   //$NON-NLS-1$
  String MSG_ERR_CANT_RESOLVE_DEPENDENCE_ID   = Messages.getString( "ISkResources.MSG_ERR_CANT_RESOLVE_DEPENDENCE_ID" ); //$NON-NLS-1$

  /**
   * {@link ErrorUtils}
   */
  String MSG_FMT_EX_CAUSE_CLASS = Messages.getString( "ISkResources.MSG_FMT_EX_CAUSE_CLASS" ); //$NON-NLS-1$
  String MSG_FMT_EX_MESSAGE     = Messages.getString( "ISkResources.MSG_FMT_EX_MESSAGE" );     //$NON-NLS-1$

}
