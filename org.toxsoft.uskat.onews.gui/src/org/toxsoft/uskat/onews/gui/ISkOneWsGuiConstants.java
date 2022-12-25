package org.toxsoft.uskat.onews.gui;

import org.eclipse.e4.core.contexts.*;
import org.toxsoft.core.tsgui.graphics.icons.*;

/**
 * Plugin constants.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface ISkOneWsGuiConstants {

  // ------------------------------------------------------------------------------------
  // Icons

  String PREFIX_OF_ICON_FIELD_NAME = "ICON_";      //$NON-NLS-1$
  String ICON_ONEWS_LIST           = "onews-list"; //$NON-NLS-1$
  String ICON_ONEWS_WS             = "onews-ws";   //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Actions

  /**
   * Constants registration.
   *
   * @param aWinContext {@link IEclipseContext} - windows level context
   */
  static void init( IEclipseContext aWinContext ) {
    ITsIconManager iconManager = aWinContext.get( ITsIconManager.class );
    iconManager.registerStdIconByIds( Activator.PLUGIN_ID, ISkOneWsGuiConstants.class, PREFIX_OF_ICON_FIELD_NAME );
  }

}
