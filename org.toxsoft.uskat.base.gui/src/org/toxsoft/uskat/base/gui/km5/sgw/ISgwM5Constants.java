package org.toxsoft.uskat.base.gui.km5.sgw;

import static org.toxsoft.uskat.core.ISkHardConstants.*;

import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.uskat.core.api.sysdescr.*;

/**
 * SGW - <b>S</b>imple <b>G</b>reen <b>W</b>orld entities M5 modelling constants.
 * <p>
 * It is isimple beacuase does not allows to modify entities (just to read) and not all properties are covered. Main
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
  String MID_SGW_CLASS_INFO = SGW_ID + ".classInfo"; //$NON-NLS-1$

  String FID_CLASS_ID  = "ClassId";  //$NON-NLS-1$
  String FID_PARENT_ID = "ParentId"; //$NON-NLS-1$

}
