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
  String STR_LINK_CLASS_IDS      = Messages.getString( "STR_LINK_CLASS_IDS" );     //$NON-NLS-1$
  String STR_LINK_CLASS_IDS_D    = Messages.getString( "STR_LINK_CLASS_IDS_D" );   //$NON-NLS-1$
  String STR_LINK_CONSTRAINTS    = Messages.getString( "STR_LINK_CONSTRAINTS" );   //$NON-NLS-1$
  String STR_LINK_CONSTRAINTS_D  = Messages.getString( "STR_LINK_CONSTRAINTS_D" ); //$NON-NLS-1$
  String FMT_ERR_NO_CONSTRAINTS  = "Задайте ограничения связи";
  String FMT_ERR_NO_NAME         = "Пусто имя недопустимо";
  String FMT_ERR_NEED_VALID_NAME = "Задайте имя связи";

  /**
   * {@link SdedDtoFullObjectM5Model}
   */
  String STR_N_OBJECT_NAME              = "Название";
  String STR_D_OBJECT_NAME              = "Название объекта";
  String STR_N_OBJECT_DESCRIPTION       = "Описание";
  String STR_D_OBJECT_DESCRIPTION       = "Описание объекта";
  String STR_N_ATTRS                    = "атрибуты";
  String STR_D_ATTRS                    = "Описание атрибутов";
  String STR_N_CLOBS                    = "clobs";
  String STR_D_CLOBS                    = "Описание clobs";
  String STR_N_LINKS                    = "связи";
  String STR_D_LINKS                    = "Описание связей";
  String STR_N_RIVETS                   = "rivets";
  String STR_D_RIVETS                   = "Описание заклепок";
  String STR_N_MAPPED_SKIDS_LINK_ID     = "link id";
  String STR_D_MAPPED_SKIDS_LINK_ID     = "id связи объекта";
  String STR_N_MAPPED_SKIDS_RIGHT_SKIDS = "right Skids";
  String STR_D_MAPPED_SKIDS_RIGHT_SKIDS = "right Skids по связи объекта";

  /**
   * {@link StringMapStringM5Model}
   */
  String STR_N_CLOB_KEY = "clob key";
  String STR_D_CLOB_KEY = "Ключ для clob";
  String STR_N_CLOB_VAL = "clob value";
  String STR_D_CLOB_VAL = "Значение clob'а";

  /**
   * {@link LinkIdSkidListM5Model}
   */
  String STR_N_LINK_ID            = "ID связи";
  String STR_D_LINK_ID            = "ID связи из описания класса";
  String STR_N_LINKED_SKIDS       = "Привязанные объекты";
  String STR_D_LINKED_SKIDS       = "Привязанные объекты";
  String STR_N_M5M_LINKIDSKIDLIST = "ID связи - список Skid объектов";
  String STR_D_M5M_LINKIDSKIDLIST = "Пара связь -> список привязанных объектов ";
}
