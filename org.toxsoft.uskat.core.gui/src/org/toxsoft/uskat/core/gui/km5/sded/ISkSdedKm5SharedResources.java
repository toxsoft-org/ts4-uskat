package org.toxsoft.uskat.core.gui.km5.sded;

import org.toxsoft.uskat.core.gui.*;
import org.toxsoft.uskat.core.gui.km5.sded.objed.*;
import org.toxsoft.uskat.core.gui.km5.sded.sded.*;

/**
 * Localizable resources.
 *
 * @author hazard157
 * @author dima
 */
@SuppressWarnings( "javadoc" )
public interface ISkSdedKm5SharedResources {

  /**
   * Common
   */
  String STR_N_DATA_TYPE       = Messages.getString( "STR_N_DATA_TYPE" );       //$NON-NLS-1$
  String STR_D_DATA_TYPE       = Messages.getString( "STR_D_DATA_TYPE" );       //$NON-NLS-1$
  String FMT_ERR_ID_NOT_IDPATH = Messages.getString( "FMT_ERR_ID_NOT_IDPATH" ); //$NON-NLS-1$
  String STR_N_M5M_CLASS       = Messages.getString( "STR_N_M5M_CLASS" );       //$NON-NLS-1$
  String STR_D_M5M_CLASS       = Messages.getString( "STR_D_M5M_CLASS" );       //$NON-NLS-1$

  /**
   * {@link SdedDtoPropInfoM5ModelBase}
   */
  String STR_N_PROP_ID          = Messages.getString( "STR_N_PROP_ID" );          //$NON-NLS-1$
  String STR_D_PROP_ID          = Messages.getString( "STR_D_PROP_ID" );          //$NON-NLS-1$
  String STR_N_PROP_NAME        = Messages.getString( "STR_N_PROP_NAME" );        //$NON-NLS-1$
  String STR_D_PROP_NAME        = Messages.getString( "STR_D_PROP_NAME" );        //$NON-NLS-1$
  String STR_N_PROP_DESCRIPTION = Messages.getString( "STR_N_PROP_DESCRIPTION" ); //$NON-NLS-1$
  String STR_D_PROP_DESCRIPTION = Messages.getString( "STR_D_PROP_DESCRIPTION" ); //$NON-NLS-1$

  /**
   * {@link SdedDtoClassInfoM5Model}
   */
  String STR_N_CLASS_ID          = Messages.getString( "STR_N_CLASS_ID" );          //$NON-NLS-1$
  String STR_D_CLASS_ID          = Messages.getString( "STR_D_CLASS_ID" );          //$NON-NLS-1$
  String STR_N_PARENT_ID         = Messages.getString( "STR_N_PARENT_ID" );         //$NON-NLS-1$
  String STR_D_PARENT_ID         = Messages.getString( "STR_D_PARENT_ID" );         //$NON-NLS-1$
  String STR_N_CLASS_NAME        = Messages.getString( "STR_N_CLASS_NAME" );        //$NON-NLS-1$
  String STR_D_CLASS_NAME        = Messages.getString( "STR_D_CLASS_NAME" );        //$NON-NLS-1$
  String STR_N_CLASS_DESCRIPTION = Messages.getString( "STR_N_CLASS_DESCRIPTION" ); //$NON-NLS-1$
  String STR_D_CLASS_DESCRIPTION = Messages.getString( "STR_D_CLASS_DESCRIPTION" ); //$NON-NLS-1$

  /**
   * {@link SdedDtoCmdInfoM5Model}
   */
  String STR_N_ARG_DEFS           = Messages.getString( "STR_N_ARG_DEFS" );           //$NON-NLS-1$
  String STR_D_ARG_DEFS           = Messages.getString( "STR_D_ARG_DEFS" );           //$NON-NLS-1$
  String STR_ADD_CMD_DLG_CAPTION  = Messages.getString( "STR_ADD_CMD_DLG_CAPTION" );  //$NON-NLS-1$
  String STR_ADD_CMD_DLG_TITLE    = Messages.getString( "STR_ADD_CMD_DLG_TITLE" );    //$NON-NLS-1$
  String STR_EDIT_CMD_DLG_CAPTION = Messages.getString( "STR_EDIT_CMD_DLG_CAPTION" ); //$NON-NLS-1$
  String STR_EDIT_CMD_DLG_TITLE   = Messages.getString( "STR_EDIT_CMD_DLG_TITLE" );   //$NON-NLS-1$

  /**
   * {@link SdedDtoEvInfoM5Model}
   */
  String STR_N_PARAM_DEFS         = Messages.getString( "STR_N_PARAM_DEFS" );         //$NON-NLS-1$
  String STR_D_PARAM_DEFS         = Messages.getString( "STR_D_PARAM_DEFS" );         //$NON-NLS-1$
  String STR_ADD_EVT_DLG_CAPTION  = Messages.getString( "STR_ADD_EVT_DLG_CAPTION" );  //$NON-NLS-1$
  String STR_ADD_EVT_DLG_TITLE    = Messages.getString( "STR_ADD_EVT_DLG_TITLE" );    //$NON-NLS-1$
  String STR_EDIT_EVT_DLG_CAPTION = Messages.getString( "STR_EDIT_EVT_DLG_CAPTION" ); //$NON-NLS-1$
  String STR_EDIT_EVT_DLG_TITLE   = Messages.getString( "STR_EDIT_EVT_DLG_TITLE" );   //$NON-NLS-1$

  /**
   * {@link SdedDtoRivetInfoM5Model}
   */
  String STR_N_RIVETED_COUNT = Messages.getString( "STR_N_RIVETED_COUNT" ); //$NON-NLS-1$
  String STR_D_RIVETED_COUNT = Messages.getString( "STR_D_RIVETED_COUNT" ); //$NON-NLS-1$

  /**
   * {@link SdedDtoRtdataInfoM5Model}
   */
  String STR_N_IS_CURR      = Messages.getString( "STR_N_IS_CURR" );      //$NON-NLS-1$
  String STR_D_IS_CURR      = Messages.getString( "STR_D_IS_CURR" );      //$NON-NLS-1$
  String STR_N_IS_HIST      = Messages.getString( "STR_N_IS_HIST" );      //$NON-NLS-1$
  String STR_D_IS_HIST      = Messages.getString( "STR_D_IS_HIST" );      //$NON-NLS-1$
  String STR_N_IS_SYNC      = Messages.getString( "STR_N_IS_SYNC" );      //$NON-NLS-1$
  String STR_D_IS_SYNC      = Messages.getString( "STR_D_IS_SYNC" );      //$NON-NLS-1$
  String STR_N_SYNC_DELTA_T = Messages.getString( "STR_N_SYNC_DELTA_T" ); //$NON-NLS-1$
  String STR_D_SYNC_DELTA_T = Messages.getString( "STR_D_SYNC_DELTA_T" ); //$NON-NLS-1$

  /**
   * {@link SdedSkClassInfoM5Model}, {@link SdedSkObjectMpc}
   */
  String STR_N_TMI_BY_HIERARCHY = Messages.getString( "STR_N_TMI_BY_HIERARCHY" ); //$NON-NLS-1$
  String STR_D_TMI_BY_HIERARCHY = Messages.getString( "STR_D_TMI_BY_HIERARCHY" ); //$NON-NLS-1$

  /**
   * {@link ISkCoreGuiConstants}
   */
  String STR_N_HIDE_CLAIMED_CLASSES = Messages.getString( "STR_N_HIDE_CLAIMED_CLASSES" ); //$NON-NLS-1$
  String STR_D_HIDE_CLAIMED_CLASSES = Messages.getString( "STR_D_HIDE_CLAIMED_CLASSES" ); //$NON-NLS-1$

  /**
   * {@link SdedDtoLinkInfoM5Model}
   */
  String STR_LINK_CLASS_IDS      = Messages.getString( "STR_LINK_CLASS_IDS" );      //$NON-NLS-1$
  String STR_LINK_CLASS_IDS_D    = Messages.getString( "STR_LINK_CLASS_IDS_D" );    //$NON-NLS-1$
  String STR_LINK_CONSTRAINTS    = Messages.getString( "STR_LINK_CONSTRAINTS" );    //$NON-NLS-1$
  String STR_LINK_CONSTRAINTS_D  = Messages.getString( "STR_LINK_CONSTRAINTS_D" );  //$NON-NLS-1$
  String FMT_ERR_NO_CONSTRAINTS  = Messages.getString( "FMT_ERR_NO_CONSTRAINTS" );  //$NON-NLS-1$
  String FMT_ERR_NO_NAME         = Messages.getString( "FMT_ERR_NO_NAME" );         //$NON-NLS-1$
  String FMT_ERR_NEED_VALID_NAME = Messages.getString( "FMT_ERR_NEED_VALID_NAME" ); //$NON-NLS-1$

  /**
   * {@link SdedDtoFullObjectM5Model}
   */
  String STR_OBJECT_NAME                = Messages.getString( "STR_OBJECT_NAME" );                //$NON-NLS-1$
  String STR_OBJECT_NAME_D              = Messages.getString( "STR_OBJECT_NAME_D" );              //$NON-NLS-1$
  String STR_OBJECT_DESCRIPTION         = Messages.getString( "STR_OBJECT_DESCRIPTION" );         //$NON-NLS-1$
  String STR_OBJECT_DESCRIPTION_D       = Messages.getString( "STR_OBJECT_DESCRIPTION_D" );       //$NON-NLS-1$
  String STR_ATTRS                      = Messages.getString( "STR_ATTRS" );                      //$NON-NLS-1$
  String STR_ATTRS_D                    = Messages.getString( "STR_ATTRS_D" );                    //$NON-NLS-1$
  String STR_CLOBS                      = Messages.getString( "STR_CLOBS" );                      //$NON-NLS-1$
  String STR_CLOBS_D                    = Messages.getString( "STR_CLOBS_D" );                    //$NON-NLS-1$
  String STR_LINKS                      = Messages.getString( "STR_LINKS" );                      //$NON-NLS-1$
  String STR_LINKS_D                    = Messages.getString( "STR_LINKS_D" );                    //$NON-NLS-1$
  String STR_RIVETS                     = Messages.getString( "STR_RIVETS" );                     //$NON-NLS-1$
  String STR_RIVETS_D                   = Messages.getString( "STR_RIVETS_D" );                   //$NON-NLS-1$
  String STR_MAPPED_SKIDS_LINK_ID       = Messages.getString( "STR_MAPPED_SKIDS_LINK_ID" );       //$NON-NLS-1$
  String STR_MAPPED_SKIDS_LINK_ID_D     = Messages.getString( "STR_MAPPED_SKIDS_LINK_ID_D" );     //$NON-NLS-1$
  String STR_MAPPED_SKIDS_RIGHT_SKIDS   = Messages.getString( "STR_MAPPED_SKIDS_RIGHT_SKIDS" );   //$NON-NLS-1$
  String STR_MAPPED_SKIDS_RIGHT_SKIDS_D = Messages.getString( "STR_MAPPED_SKIDS_RIGHT_SKIDS_D" ); //$NON-NLS-1$

  /**
   * {@link StringMapStringM5Model}
   */
  String STR_CLOB_KEY   = Messages.getString( "STR_CLOB_KEY" );   //$NON-NLS-1$
  String STR_CLOB_KEY_D = Messages.getString( "STR_CLOB_KEY_D" ); //$NON-NLS-1$
  String STR_CLOB_VAL   = Messages.getString( "STR_CLOB_VAL" );   //$NON-NLS-1$
  String STR_CLOB_VAL_D = Messages.getString( "STR_CLOB_VAL_D" ); //$NON-NLS-1$

  /**
   * {@link LinkIdSkidListM5Model}
   */
  String STR_LINK_ID              = Messages.getString( "STR_LINK_ID" );              //$NON-NLS-1$
  String STR_LINK_ID_D            = Messages.getString( "STR_LINK_ID_D" );            //$NON-NLS-1$
  String STR_LINKED_SKIDS         = Messages.getString( "STR_LINKED_SKIDS" );         //$NON-NLS-1$
  String STR_LINKED_SKIDS_D       = Messages.getString( "STR_LINKED_SKIDS_D" );       //$NON-NLS-1$
  String STR_M5M_LINKIDSKIDLIST   = Messages.getString( "STR_M5M_LINKIDSKIDLIST" );   //$NON-NLS-1$
  String STR_M5M_LINKIDSKIDLIST_D = Messages.getString( "STR_M5M_LINKIDSKIDLIST_D" ); //$NON-NLS-1$
}
