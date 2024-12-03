package org.toxsoft.uskat.core.gui.km5.sded;

import static org.toxsoft.uskat.core.gui.ISkCoreGuiConstants.*;

import org.toxsoft.uskat.core.gui.km5.sded.objed.*;

/**
 * M5-modeling constants for SDED.
 *
 * @author hazard157
 * @author dima
 */
@SuppressWarnings( "javadoc" )
public interface IKM5SdedConstants {

  String MID_SDED_SK_CLASS_INFO   = SDED_ID + ".SkClassInfo";    //$NON-NLS-1$
  String MID_SDED_DTO_CLASS_INFO  = SDED_ID + ".DtoClassInfo";   //$NON-NLS-1$
  String MID_SDED_DTO_FULL_OBJECT = SDED_ID + ".DtoFullObject";  //$NON-NLS-1$
  String MID_SDED_ATTR_INFO       = SDED_ID + ".AttrInfo";       //$NON-NLS-1$
  String MID_SDED_RIVET_INFO      = SDED_ID + ".RivetInfo";      //$NON-NLS-1$
  String MID_SDED_CLOB_INFO       = SDED_ID + ".ClobInfo";       //$NON-NLS-1$
  String MID_SDED_RTDATA_INFO     = SDED_ID + ".RtdataInfo";     //$NON-NLS-1$
  String MID_SDED_LINK_INFO       = SDED_ID + ".LinkInfo";       //$NON-NLS-1$
  String MID_SDED_CMD_INFO        = SDED_ID + ".CmdInfo";        //$NON-NLS-1$
  String MID_SDED_EVENT_INFO      = SDED_ID + ".EventInfo";      //$NON-NLS-1$
  String MID_SDED_COLL_CONSTRAINT = SDED_ID + ".CollConstraint"; //$NON-NLS-1$

  /**
   * ID of M5-model {@link SdedSkObjectM5Model}.
   */
  String MID_SDED_SK_OBJECT = SDED_ID + ".SkObject"; //$NON-NLS-1$

  String FID_CLASS_ID       = "classId";      //$NON-NLS-1$
  String FID_PARENT_ID      = "parentId";     //$NON-NLS-1$
  String FID_DATA_TYPE      = "dataType";     //$NON-NLS-1$
  String FID_PARAM_DEFS     = "paramDefs";    //$NON-NLS-1$
  String FID_ARG_DEFS       = "argDefs";      //$NON-NLS-1$
  String FID_RIGHT_CLASS_ID = "rightClassId"; //$NON-NLS-1$
  String FID_COUNT          = "count";        //$NON-NLS-1$
  String FID_IS_CURR        = "isCurr";       //$NON-NLS-1$
  String FID_IS_HIST        = "isHist";       //$NON-NLS-1$
  String FID_IS_SYNC        = "isSync";       //$NON-NLS-1$
  String FID_SYNC_DELTA_T   = "syncDeltaT";   //$NON-NLS-1$

  String FID_SELF_ATTR_INFOS   = "selfAttrInfos";   //$NON-NLS-1$
  String FID_SELF_CLOB_INFOS   = "selfClobInfos";   //$NON-NLS-1$
  String FID_SELF_RIVET_INFOS  = "selfRivetInfos";  //$NON-NLS-1$
  String FID_SELF_RTDATA_INFOS = "selfRtdataInfos"; //$NON-NLS-1$
  String FID_SELF_LINK_INFOS   = "selfLinkInfos";   //$NON-NLS-1$
  String FID_SELF_CMD_INFOS    = "selfCmdInfos";    //$NON-NLS-1$
  String FID_SELF_EVENT_INFOS  = "selfEventInfos";  //$NON-NLS-1$

}
