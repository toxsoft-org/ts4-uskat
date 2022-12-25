package org.toxsoft.uskat.onews.mws;

import org.eclipse.e4.core.contexts.*;
import org.toxsoft.core.tsgui.graphics.icons.*;

/**
 * Plugin constants.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface ISkOneWsMwsConstants {

  // ------------------------------------------------------------------------------------
  // E4

  String PARTID_SK_PROFILES_EDITOR = "org.toxsoft.uskat.onews.part.profiles_editor"; //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Icons

  String PREFIX_OF_ICON_FIELD_NAME = "ICON_"; //$NON-NLS-1$
  // String ICON_XXX = "xxx"; //$NON-NLS-1$

  /**
   * Constants registration.
   *
   * @param aWinContext {@link IEclipseContext} - windows level context
   */
  static void init( IEclipseContext aWinContext ) {
    ITsIconManager iconManager = aWinContext.get( ITsIconManager.class );
    iconManager.registerStdIconByIds( Activator.PLUGIN_ID, ISkOneWsMwsConstants.class, PREFIX_OF_ICON_FIELD_NAME );
  }

}
