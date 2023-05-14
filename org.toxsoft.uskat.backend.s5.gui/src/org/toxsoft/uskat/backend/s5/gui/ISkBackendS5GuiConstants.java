package org.toxsoft.uskat.backend.s5.gui;

import static org.toxsoft.uskat.core.ISkHardConstants.*;

import org.eclipse.e4.core.contexts.*;
import org.toxsoft.core.tsgui.graphics.icons.*;

/**
 * Plugin constants.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface ISkBackendS5GuiConstants {

  /**
   * IDs prefix in this plugin.
   */
  String S5GUI_ID = SK_ID + ".s5.gui"; //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Icons

  String PREFIX_OF_ICON_FIELD_NAME = "ICONID_"; //$NON-NLS-1$
  // String ICONID_XXX= "xxx"; //$NON-NLS-1$

  /**
   * Constants registration.
   *
   * @param aWinContext {@link IEclipseContext} - windows level context
   */
  static void init( IEclipseContext aWinContext ) {
    ITsIconManager iconManager = aWinContext.get( ITsIconManager.class );
    iconManager.registerStdIconByIds( Activator.PLUGIN_ID, ISkBackendS5GuiConstants.class, PREFIX_OF_ICON_FIELD_NAME );
  }

}
