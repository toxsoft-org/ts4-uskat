package org.toxsoft.uskat.base.gui.km5.sgw;

import static org.toxsoft.uskat.core.ISkHardConstants.*;

import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;

/**
 * SGW - <b>S</b>imple <b>G</b>reen <b>W</b>orld entities M5 modelling constants.
 * <p>
 * It is simple beacuase does not allows to modify entities (just to read) and not all properties are covered. Main
 * purpose of SGW is to make easy to browse and select {@link Gwid} entities.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface ISgwM5Constants {

  /**
   * Prefix of SGW IDs.
   */
  String SGW_ID = SK_ID + ".sgw"; //$NON-NLS-1$

  /**
   * ID of SGW M5 model of {@link ISkClassInfo}.
   */
  String MID_SGW_CLASS_INFO = SGW_ID + ".ClassInfo"; //$NON-NLS-1$

  /**
   * ID of SGW M5 model of {@link ISkObject}.
   */
  String MID_SGW_SK_OBJECT = SGW_ID + ".SkObject"; //$NON-NLS-1$

  String MID_SGW_ATTR_INFO   = SGW_ID + ".AttrInfo";   //$NON-NLS-1$
  String MID_SGW_RIVET_INFO  = SGW_ID + ".RivetInfo";  //$NON-NLS-1$
  String MID_SGW_CLOB_INFO   = SGW_ID + ".ClobInfo";   //$NON-NLS-1$
  String MID_SGW_RTDATA_INFO = SGW_ID + ".RtdataInfo"; //$NON-NLS-1$
  String MID_SGW_LINK_INFO   = SGW_ID + ".LinkInfo";   //$NON-NLS-1$
  String MID_SGW_CMD_INFO    = SGW_ID + ".CmdInfo";    //$NON-NLS-1$
  String MID_SGW_EVENT_INFO  = SGW_ID + ".EventInfo";  //$NON-NLS-1$

  String FID_CLASS_ID  = "classId";  //$NON-NLS-1$
  String FID_PARENT_ID = "parentId"; //$NON-NLS-1$
  String FID_DATA_TYPE = "dataType"; //$NON-NLS-1$
  String FID_IS_CURR   = "isCurr";   //$NON-NLS-1$
  String FID_IS_HIST   = "isHist";   //$NON-NLS-1$
  String FID_IS_SYNC   = "isSync";   //$NON-NLS-1$

}
