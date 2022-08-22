package org.toxsoft.uskat.refbooks.gui;

import org.eclipse.e4.core.contexts.*;
import org.toxsoft.core.tsgui.graphics.icons.*;

/**
 * Plugin constants.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface ISkRefbooksGuiConstants {

  // ------------------------------------------------------------------------------------
  // Icons

  String PREFIX_OF_ICON_FIELD_NAME = "ICON_";         //$NON-NLS-1$
  String ICON_REFBOOK              = "refbook";       //$NON-NLS-1$
  String ICON_REFBOOKS_LIST        = "refbooks-list"; //$NON-NLS-1$
  String ICON_REFBOOK_EDIT         = "refbook-edit";  //$NON-NLS-1$
  String ICON_REFBOOK_ITEM         = "rbitem";        //$NON-NLS-1$
  String ICON_REFBOOK_ITEMS_LIST   = "rbitems-list";  //$NON-NLS-1$

  /**
   * Constants registration.
   *
   * @param aWinContext {@link IEclipseContext} - windows level context
   */
  static void init( IEclipseContext aWinContext ) {
    ITsIconManager iconManager = aWinContext.get( ITsIconManager.class );
    iconManager.registerStdIconByIds( Activator.PLUGIN_ID, ISkRefbooksGuiConstants.class, PREFIX_OF_ICON_FIELD_NAME );
  }

}
