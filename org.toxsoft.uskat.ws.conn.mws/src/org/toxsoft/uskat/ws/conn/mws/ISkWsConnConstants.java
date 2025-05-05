package org.toxsoft.uskat.ws.conn.mws;

import org.eclipse.e4.core.contexts.*;
import org.toxsoft.core.tsgui.graphics.icons.*;
import org.toxsoft.uskat.core.gui.conn.cfg.*;

/**
 * Plugin common constants.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface ISkWsConnConstants {

  // ------------------------------------------------------------------------------------
  // E4
  //

  String CMDCATEGID_SK_CONNECTIONS = "org.toxsoft.uskat.ws.conn.cmdcateg.sk_connections"; //$NON-NLS-1$
  String CMDID_SKCONN_CONNECT      = "org.toxsoft.uskat.ws.conn.cmd.connect";             //$NON-NLS-1$
  String CMDID_SKCONN_CHANGE_ROLE  = "org.toxsoft.uskat.ws.conn.cmd.change_role";         //$NON-NLS-1$
  String CMDID_SKCONN_DISCONNECT   = "org.toxsoft.uskat.ws.conn.cmd.disconnect";          //$NON-NLS-1$
  String CMDID_SKCONN_SELECT       = "org.toxsoft.uskat.ws.conn.cmd.select";              //$NON-NLS-1$
  String CMDID_SKCONN_EDIT         = "org.toxsoft.uskat.ws.conn.cmd.edit";                //$NON-NLS-1$
  String CMDID_SKCONN_INFO         = "org.toxsoft.uskat.ws.conn.cmd.info";                //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Command line arguments
  //

  /**
   * Name of the file where the data from {@link IConnectionConfigService} is stored.
   * <p>
   * Path may be absolute or relative to the workstation application working directory.
   */
  String CLINEARG_CONN_CFG_FILE_NAME = "connCfgFileName"; //$NON-NLS-1$

  /**
   * Name of the file when {@link #CLINEARG_CONN_CFG_FILE_NAME} is not specified.
   * <p>
   * File is assumed to be in the workstation application working directory.
   */
  String DEFAULT_CONN_CFG_FILE_NAME = "ws-conn-cfgs.ktor"; //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Icons

  String PREFIX_OF_ICON_FIELD_NAME = "ICONID_"; //$NON-NLS-1$
  // String ICONID_FOO= "foo"; //$NON-NLS-1$

  /**
   * Constants registration.
   *
   * @param aWinContext {@link IEclipseContext} - windows level context
   */
  static void init( IEclipseContext aWinContext ) {
    ITsIconManager iconManager = aWinContext.get( ITsIconManager.class );
    iconManager.registerStdIconByIds( Activator.PLUGIN_ID, ISkWsConnConstants.class, PREFIX_OF_ICON_FIELD_NAME );
  }

}
