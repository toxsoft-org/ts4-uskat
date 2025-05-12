package org.toxsoft.uskat.ws.conn.mws;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.ws.conn.mws.l10n.ISkWsConnSharedResources.*;

import org.eclipse.e4.core.contexts.*;
import org.toxsoft.core.tsgui.graphics.icons.*;
import org.toxsoft.core.tsgui.graphics.image.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
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

  // ------------------------------------------------------------------------------------
  // Application Preferences
  //

  String PBID_CONN_CONFIGS = USKAT_FULL_ID + ".ws.conn_configs"; //$NON-NLS-1$

  String APREFID_LAST_CONNECTION_ID = "LastConnectionID"; //$NON-NLS-1$

  IDataDef APPREF_LAST_CONNECTION_ID = DataDef.create( APREFID_LAST_CONNECTION_ID, STRING, //
      TSID_NAME, STR_LAST_CONNECTION_ID, //
      TSID_DESCRIPTION, STR_LAST_CONNECTION_ID_D, //
      TSID_KEEPER_ID, EThumbSize.KEEPER_ID, //
      TSID_DEFAULT_VALUE, avValobj( EThumbSize.SZ180 ) //
  );

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
