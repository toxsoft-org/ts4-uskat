package org.toxsoft.uskat.core.gui.conn.l10n;

import org.toxsoft.uskat.core.gui.conn.*;
import org.toxsoft.uskat.core.gui.conn.m5.*;

/**
 * Localizable resources.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface ISkCoreGuiConnSharedResources {

  /**
   * {@link SkConnectionSupplier}
   */
  String FMT_ERR_CONN_ID_EXISTS      = Messages.getString( "FMT_ERR_CONN_ID_EXISTS" );      //$NON-NLS-1$
  String MSG_ERR_CANT_REMOVE_NULL_ID = Messages.getString( "MSG_ERR_CANT_REMOVE_NULL_ID" ); //$NON-NLS-1$
  String FMT_WARN_NO_SUCH_CONN_ID    = Messages.getString( "FMT_WARN_NO_SUCH_CONN_ID" );    //$NON-NLS-1$

  /**
   * {@link SkConnGuiUtils}
   */
  String DLG_EDIT_CONFIGS         = Messages.getString( "DLG_EDIT_CONFIGS" );         //$NON-NLS-1$
  String DLG_EDIT_CONFIGS_D       = Messages.getString( "DLG_EDIT_CONFIGS_D" );       //$NON-NLS-1$
  String DLG_SELECT_CFG           = Messages.getString( "DLG_SELECT_CFG" );           //$NON-NLS-1$
  String DLG_SELECT_CFG_D         = Messages.getString( "DLG_SELECT_CFG_D" );         //$NON-NLS-1$
  String FMT_ERR_UNKNOWN_PROVIDER = Messages.getString( "FMT_ERR_UNKNOWN_PROVIDER" ); //$NON-NLS-1$

  /**
   * {@link ConnectionConfigM5LifecycleManager}
   */
  String MSG_ERR_NO_CC_PROVIDER = Messages.getString( "MSG_ERR_NO_CC_PROVIDER" ); //$NON-NLS-1$

  /**
   * {@link ConnectionConfigM5Model}
   */
  String STR_N_M5M_CFG_NAME = Messages.getString( "STR_N_M5M_CFG_NAME" ); //$NON-NLS-1$
  String STR_D_M5M_CFG_NAME = Messages.getString( "STR_D_M5M_CFG_NAME" ); //$NON-NLS-1$
  String STR_N_CFG_NAME     = Messages.getString( "STR_N_CFG_NAME" );     //$NON-NLS-1$
  String STR_D_CFG_NAME     = Messages.getString( "STR_D_CFG_NAME" );     //$NON-NLS-1$
  String STR_N_PROVIDER_ID  = Messages.getString( "STR_N_PROVIDER_ID" );  //$NON-NLS-1$
  String STR_D_PROVIDER_ID  = Messages.getString( "STR_D_PROVIDER_ID" );  //$NON-NLS-1$
  String STR_N_VALUES       = Messages.getString( "STR_N_VALUES" );       //$NON-NLS-1$
  String STR_D_VALUES       = Messages.getString( "STR_D_VALUES" );       //$NON-NLS-1$
  String STR_N_PARAMS       = Messages.getString( "STR_N_PARAMS" );       //$NON-NLS-1$
  String STR_D_PARAMS       = Messages.getString( "STR_D_PARAMS" );       //$NON-NLS-1$

}
