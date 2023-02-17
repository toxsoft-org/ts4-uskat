package org.toxsoft.uskat.sded.gui.km5;

import org.toxsoft.uskat.sded.gui.*;
import org.toxsoft.uskat.sded.gui.km5.objed.*;
import org.toxsoft.uskat.sded.gui.km5.sded.*;

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
  String STR_N_ARG_DEFS = Messages.getString( "STR_N_ARG_DEFS" ); //$NON-NLS-1$
  String STR_D_ARG_DEFS = Messages.getString( "STR_D_ARG_DEFS" ); //$NON-NLS-1$

  /**
   * {@link SdedDtoEvInfoM5Model}
   */
  String STR_N_PARAM_DEFS = Messages.getString( "STR_N_PARAM_DEFS" ); //$NON-NLS-1$
  String STR_D_PARAM_DEFS = Messages.getString( "STR_D_PARAM_DEFS" ); //$NON-NLS-1$

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
   * {@link ISkSdedGuiConstants}
   */
  String STR_N_HIDE_CLAIMED_CLASSES = Messages.getString( "STR_N_HIDE_CLAIMED_CLASSES" ); //$NON-NLS-1$
  String STR_D_HIDE_CLAIMED_CLASSES = Messages.getString( "STR_D_HIDE_CLAIMED_CLASSES" ); //$NON-NLS-1$

}
