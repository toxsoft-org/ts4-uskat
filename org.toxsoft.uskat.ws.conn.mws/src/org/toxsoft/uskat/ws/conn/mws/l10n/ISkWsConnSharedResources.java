package org.toxsoft.uskat.ws.conn.mws.l10n;

import org.toxsoft.uskat.ws.conn.mws.*;
import org.toxsoft.uskat.ws.conn.mws.e4.handlers.*;

/**
 * Localizable resources.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface ISkWsConnSharedResources {

  /**
   * {@link ISkWsConnConstants}
   */
  String STR_PREFBUNDLEID_CONN_CONFIGS   = Messages.getString( "STR_PREFBUNDLEID_CONN_CONFIGS" );   //$NON-NLS-1$
  String STR_PREFBUNDLEID_CONN_CONFIGS_D = Messages.getString( "STR_PREFBUNDLEID_CONN_CONFIGS_D" ); //$NON-NLS-1$
  String STR_LAST_CONNECTION_ID          = Messages.getString( "STR_LAST_CONNECTION_ID" );          //$NON-NLS-1$
  String STR_LAST_CONNECTION_ID_D        = Messages.getString( "STR_LAST_CONNECTION_ID_D" );        //$NON-NLS-1$
  String STR_IS_DIALOG_AFTER_CONNECT     = Messages.getString( "STR_IS_DIALOG_AFTER_CONNECT" );     //$NON-NLS-1$
  String STR_IS_DIALOG_AFTER_CONNECT_D   = Messages.getString( "STR_IS_DIALOG_AFTER_CONNECT_D" );   //$NON-NLS-1$

  /**
   * {@link CmdConnect}
   */
  String MSG_WARN_NO_LAST_CONN      = Messages.getString( "MSG_WARN_NO_LAST_CONN" );      //$NON-NLS-1$
  String FMT_WARN_LAST_CONN_DELETED = Messages.getString( "FMT_WARN_LAST_CONN_DELETED" ); //$NON-NLS-1$

  /**
   * {@link CmdSelect}
   */
  String MSG_ASK_CONNECT_ON_CFG_WARN = Messages.getString( "MSG_ASK_CONNECT_ON_CFG_WARN" ); //$NON-NLS-1$
  String FMT_ERR_OPEN_CONNECTION     = Messages.getString( "FMT_ERR_OPEN_CONNECTION" );     //$NON-NLS-1$
  String MSG_ERR_OPEN_CONNECTION     = Messages.getString( "MSG_ERR_OPEN_CONNECTION" );     //$NON-NLS-1$
  String FMT_CONNECTION_SUCCESS      = Messages.getString( "FMT_CONNECTION_SUCCESS" );      //$NON-NLS-1$

  /**
   * {@link CmdDisconnect}
   */
  String MSG_ALREADY_DISCONNECTED      = Messages.getString( "MSG_ALREADY_DISCONNECTED" );      //$NON-NLS-1$
  String FMT_ASK_REALLY_DISCONNECT     = Messages.getString( "FMT_ASK_REALLY_DISCONNECT" );     //$NON-NLS-1$
  String MSG_SUCCESSFULLY_DISCONNECTED = Messages.getString( "MSG_SUCCESSFULLY_DISCONNECTED" ); //$NON-NLS-1$
  String MSG_WARN_DISCONNECTION_ERROR  = Messages.getString( "MSG_WARN_DISCONNECTION_ERROR" );  //$NON-NLS-1$

  String STR_DLG_SERVER_INFO   = Messages.getString( "STR_DLG_SERVER_INFO" );   //$NON-NLS-1$
  String STR_DLG_SERVER_INFO_D = Messages.getString( "STR_DLG_SERVER_INFO_D" ); //$NON-NLS-1$

}
