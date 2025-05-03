package org.toxsoft.uskat.ws.conn.mws;

import org.eclipse.e4.core.contexts.*;
import org.toxsoft.core.tsgui.graphics.icons.*;

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

  String CMDID_S5CONN_OPEN   = "org.toxsoft.uskat.ws.conn.cmd.open";   //$NON-NLS-1$
  String CMDID_S5CONN_CLOSE  = "org.toxsoft.uskat.ws.conn.cmd.close";  //$NON-NLS-1$
  String CMDID_S5CONN_INFO   = "org.toxsoft.uskat.ws.conn.cmd.info";   //$NON-NLS-1$
  String CMDID_S5CONN_SELECT = "org.toxsoft.uskat.ws.conn.cmd.select"; //$NON-NLS-1$
  String CMDID_S5CONN_EDIT   = "org.toxsoft.uskat.ws.conn.cmd.edit";   //$NON-NLS-1$
  String CMDID_S5CONN_IMPORT = "org.toxsoft.uskat.ws.conn.cmd.import"; //$NON-NLS-1$
  String CMDID_S5CONN_EXPORT = "org.toxsoft.uskat.ws.conn.cmd.export"; //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Icons

  String PREFIX_OF_ICON_FIELD_NAME = "ICONID_";          //$NON-NLS-1$
  String ICONID_USKAT_CONNECT      = "uskat-connect";    //$NON-NLS-1$
  String ICONID_USKAT_DISCONNECT   = "uskat-disconnect"; //$NON-NLS-1$
  String ICONID_USKAT_SERVER       = "uskat-server";     //$NON-NLS-1$

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
