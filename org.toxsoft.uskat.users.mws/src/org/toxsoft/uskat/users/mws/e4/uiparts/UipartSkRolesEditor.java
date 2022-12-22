package org.toxsoft.uskat.users.mws.e4.uiparts;

import javax.inject.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.ctx.impl.*;
import org.toxsoft.core.tsgui.bricks.stdevents.*;
import org.toxsoft.core.tsgui.mws.services.currentity.*;
import org.toxsoft.core.tsgui.widgets.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.uskat.base.gui.conn.*;
import org.toxsoft.uskat.base.gui.e4.uiparts.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.users.gui.panels.*;
import org.toxsoft.uskat.users.mws.e4.service.*;

/**
 * Uipart contains {@link PanelSkRolesEditor} and respects {@link ICurrentUsersMwsSkRoleService#current()}.
 * <p>
 * By default sets {@link PanelSkRolesEditor#getUsedConnectionId()} to <code>null</code> using Sk-connection
 * {@link ISkConnectionSupplier#defConn()} for roles management.
 * <p>
 * TODO add an ability to set {@link IdChain} of used connection
 *
 * @author hazard157
 */
public class UipartSkRolesEditor
    extends SkMwsAbstractPart {

  /**
   * Changes selection in panel when {@link ICurrentUsersMwsSkRoleService#current()} changes.
   */
  private final ICurrentEntityChangeListener<ISkRole> currentUsersMwsSkRoleServiceListener =
      aCurrent -> this.rolesEditor.setSelectedItem( aCurrent );

  /**
   * Changes {@link ICurrentUsersMwsSkRoleService#current()} when selection in panel changes.
   */
  private final ITsSelectionChangeListener<ISkRole> panelSelectionChangeListener =
      ( aSource, aSelectedItem ) -> this.currentUsersMwsSkRoleService.setCurrent( aSelectedItem );

  @Inject
  ICurrentUsersMwsSkRoleService currentUsersMwsSkRoleService;

  PanelSkRolesEditor rolesEditor;

  @Override
  protected void doCreateContent( TsComposite aParent ) {
    ITsGuiContext ctx = new TsGuiContext( tsContext() );
    rolesEditor = new PanelSkRolesEditor( ctx, null );
    rolesEditor.createControl( aParent );
    rolesEditor.addTsSelectionListener( panelSelectionChangeListener );
    currentUsersMwsSkRoleService.addCurrentEntityChangeListener( currentUsersMwsSkRoleServiceListener );
  }

}
