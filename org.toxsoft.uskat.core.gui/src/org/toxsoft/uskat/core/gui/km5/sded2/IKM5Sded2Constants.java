package org.toxsoft.uskat.core.gui.km5.sded2;

import static org.toxsoft.uskat.core.ISkHardConstants.*;

/**
 * M5-modeling constants for SDED.
 *
 * @author hazard157
 * @author dima
 */
@SuppressWarnings( "javadoc" )
public interface IKM5Sded2Constants {

  String SDED2_ID    = USKAT_FULL_ID + ".sded2"; //$NON-NLS-1$
  String SDED2_M5_ID = SDED2_ID + ".m5";         //$NON-NLS-1$

  // FIXME this interface must be removed, all constants moved to respective XxxM5Model classes

  String FID_CLASS_ID       = "classId";      //$NON-NLS-1$
  String FID_PARENT_ID      = "parentId";     //$NON-NLS-1$
  String FID_PROP_KIND      = "propKind";     //$NON-NLS-1$
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
  String FID_SELF_LINK_INFOS   = "selfLinkInfos";   //$NON-NLS-1$
  String FID_SELF_CLOB_INFOS   = "selfClobInfos";   //$NON-NLS-1$
  String FID_SELF_RIVET_INFOS  = "selfRivetInfos";  //$NON-NLS-1$
  String FID_SELF_RTDATA_INFOS = "selfRtdataInfos"; //$NON-NLS-1$
  String FID_SELF_CMD_INFOS    = "selfCmdInfos";    //$NON-NLS-1$
  String FID_SELF_EVENT_INFOS  = "selfEventInfos";  //$NON-NLS-1$
  String FID_ALL_PROP_INFOS    = "allPropInfos";    //$NON-NLS-1$

}
