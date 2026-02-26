package org.toxsoft.uskat.backend.memtext;

import org.toxsoft.core.txtproj.lib.*;

/**
 * Localizable resources.
 *
 * @author hazard157
 */
interface ISkResources {

  /**
   * {@link IBackendMemtextConstants}
   */
  String STR_NOT_STORED_OBJ_CLASS_IDS   = Messages.getString( "STR_NOT_STORED_OBJ_CLASS_IDS" );   //$NON-NLS-1$
  String STR_NOT_STORED_OBJ_CLASS_IDS_D = Messages.getString( "STR_D_NOT_STORED_OBJ_CLASS_IDS" ); //$NON-NLS-1$
  String STR_IS_EVENTS_STORED           = Messages.getString( "STR_N_IS_EVENTS_STORED" );         //$NON-NLS-1$
  String STR_IS_EVENTS_STORED_D         = Messages.getString( "STR_D_IS_EVENTS_STORED" );         //$NON-NLS-1$
  String STR_MAX_EVENTS_COUNT           = Messages.getString( "STR_N_MAX_EVENTS_COUNT" );         //$NON-NLS-1$
  String STR_MAX_EVENTS_COUNT_D         = Messages.getString( "STR_D_MAX_EVENTS_COUNT" );         //$NON-NLS-1$
  String STR_IS_CMDS_STORED             = Messages.getString( "STR_N_IS_CMDS_STORED" );           //$NON-NLS-1$
  String STR_IS_CMDS_STORED_D           = Messages.getString( "STR_D_IS_CMDS_STORED" );           //$NON-NLS-1$
  String STR_MAX_CMDS_COUNT             = Messages.getString( "STR_N_MAX_CMDS_COUNT" );           //$NON-NLS-1$
  String STR_MAX_CMDS_COUNT_D           = Messages.getString( "STR_D_MAX_CMDS_COUNT" );           //$NON-NLS-1$
  String STR_HISTORY_DEPTH_HOURS        = Messages.getString( "STR_N_HISTORY_DEPTH_HOURS" );      //$NON-NLS-1$
  String STR_HISTORY_DEPTH_HOURS_D      = Messages.getString( "STR_D_HISTORY_DEPTH_HOURS" );      //$NON-NLS-1$
  String STR_CURR_DATA_10MS_TICKS       = Messages.getString( "STR_N_CURR_DATA_10MS_TICKS" );     //$NON-NLS-1$
  String STR_CURR_DATA_10MS_TICKS_D     = Messages.getString( "STR_D_CURR_DATA_10MS_TICKS" );     //$NON-NLS-1$
  String FMT_ERR_TEST_FAILED_BY_TIMEOUT = Messages.getString( "FMT_ERR_TEST_FAILED_BY_TIMEOUT" ); //$NON-NLS-1$

  /**
   * {@link MtbBackendToFile}
   */
  String STR_OP_FILE_PATH               = Messages.getString( "STR_OP_FILE_PATH" );               //$NON-NLS-1$
  String STR_OP_FILE_PATH_D             = Messages.getString( "STR_OP_FILE_PATH_D" );             //$NON-NLS-1$
  String STR_OP_AUTO_SAVE_SECS          = Messages.getString( "STR_OP_AUTO_SAVE_SECS" );          //$NON-NLS-1$
  String STR_OP_AUTO_SAVE_SECS_D        = Messages.getString( "STR_OP_AUTO_SAVE_SECS_D" );        //$NON-NLS-1$
  String FMT_ERR_NO_FILE_NAME_SPECIFIED = Messages.getString( "FMT_ERR_NO_FILE_NAME_SPECIFIED" ); //$NON-NLS-1$

  /**
   * {@link MtbBackendToFileMetaInfo}
   */
  String STR_BACKEND_MEMTEXT_TO_FILE   = Messages.getString( "STR_BACKEND_MEMTEXT_TO_FILE" );   //$NON-NLS-1$
  String STR_BACKEND_MEMTEXT_TO_FILE_D = Messages.getString( "STR_BACKEND_MEMTEXT_TO_FILE_D" ); //$NON-NLS-1$

  /**
   * {@link MtbBackendToTsProj}
   */
  String STR_OP_PDU_ID      = Messages.getString( "STR_OP_PDU_ID" );                                        //$NON-NLS-1$
  String STR_OP_PDU_ID_D    = Messages.getString( "STR_OP_PDU_ID_D" );                                      //$NON-NLS-1$
  String STR_REF_PROJECT    = Messages.getString( "STR_REF_PROJECT" );                                      //$NON-NLS-1$
  String STR_REF_PROJECT_D  = Messages.getString( "STR_REF_PROJECT_D" ) + ITsProject.class.getSimpleName(); //$NON-NLS-1$
  String FMT_ERR_INV_PDU_ID = Messages.getString( "FMT_ERR_INV_PDU_ID" );                                   //$NON-NLS-1$

}
