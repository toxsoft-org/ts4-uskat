package org.toxsoft.uskat.sded.gui;

import org.eclipse.e4.core.contexts.*;
import org.toxsoft.core.tsgui.graphics.icons.*;

/**
 * Plugin constants.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface ISkSdedGuiConstants {

  // ------------------------------------------------------------------------------------
  // Icons

  String PREFIX_OF_ICON_FIELD_NAME = "ICONID_";           //$NON-NLS-1$
  String ICONID_SDED_CLASS         = "sded-class";        //$NON-NLS-1$
  String ICONID_SDED_CLASSES_LIST  = "sded-classes-list"; //$NON-NLS-1$
  String ICONID_SDED_CLASS_ATTR    = "sded-class-attr";   //$NON-NLS-1$
  String ICONID_SDED_CLASS_CLOB    = "sded-class-clob";   //$NON-NLS-1$
  String ICONID_SDED_CLASS_RIVET   = "sded-class-rivet";  //$NON-NLS-1$
  String ICONID_SDED_CLASS_LINK    = "sded-class-link";   //$NON-NLS-1$
  String ICONID_SDED_CLASS_DATA    = "sded-class-data";   //$NON-NLS-1$
  String ICONID_SDED_CLASS_CMD     = "sded-class-cmd";    //$NON-NLS-1$
  String ICONID_SDED_CLASS_EVENT   = "sded-class-event";  //$NON-NLS-1$
  String ICONID_SDED_OBJ           = "sded-obj";          //$NON-NLS-1$
  String ICONID_SDED_OBJS_LIST     = "sded-objs-list";    //$NON-NLS-1$
  String ICONID_SDED_OBJ_ATTR      = "sded-obj-attr";     //$NON-NLS-1$
  String ICONID_SDED_OBJ_CLOB      = "sded-obj-clob";     //$NON-NLS-1$
  String ICONID_SDED_OBJ_RIVET     = "sded-obj-rivet";    //$NON-NLS-1$
  String ICONID_SDED_OBJ_LINK      = "sded-obj-link";     //$NON-NLS-1$
  String ICONID_SDED_OBJ_DATA      = "sded-obj-data";     //$NON-NLS-1$
  String ICONID_SDED_OBJ_CMD       = "sded-obj-cmd";      //$NON-NLS-1$
  String ICONID_SDED_OBJ_EVENT     = "sded-obj-event";    //$NON-NLS-1$

  /**
   * Constants registration.
   *
   * @param aWinContext {@link IEclipseContext} - windows level context
   */
  static void init( IEclipseContext aWinContext ) {
    ITsIconManager iconManager = aWinContext.get( ITsIconManager.class );
    iconManager.registerStdIconByIds( Activator.PLUGIN_ID, ISkSdedGuiConstants.class, PREFIX_OF_ICON_FIELD_NAME );
  }

}
