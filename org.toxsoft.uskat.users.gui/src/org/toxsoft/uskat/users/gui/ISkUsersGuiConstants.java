package org.toxsoft.uskat.users.gui;

import org.eclipse.e4.core.contexts.*;
import org.toxsoft.core.tsgui.graphics.icons.*;

/**
 * Plugin constants.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface ISkUsersGuiConstants {

  // ------------------------------------------------------------------------------------
  // Icons

  String PREFIX_OF_ICON_FIELD_NAME = "ICON_";      //$NON-NLS-1$
  String ICON_USER                 = "user";       //$NON-NLS-1$
  String ICON_USERS_LIST           = "users-list"; //$NON-NLS-1$
  String ICON_ROLE                 = "role";       //$NON-NLS-1$
  String ICON_ROLES_LIST           = "roles-list"; //$NON-NLS-1$

  /**
   * Constants registration.
   *
   * @param aWinContext {@link IEclipseContext} - windows level context
   */
  static void init( IEclipseContext aWinContext ) {
    ITsIconManager iconManager = aWinContext.get( ITsIconManager.class );
    iconManager.registerStdIconByIds( Activator.PLUGIN_ID, ISkUsersGuiConstants.class, PREFIX_OF_ICON_FIELD_NAME );
  }

}
