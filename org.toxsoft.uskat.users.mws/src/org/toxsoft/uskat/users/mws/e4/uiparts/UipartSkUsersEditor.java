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
 * Uipart contains {@link PanelSkUsersEditor} and respects {@link ICurrentUsersMwsSkUserService#current()}.
 * <p>
 * By default sets {@link PanelSkUsersEditor#getUsedConnectionId()} to <code>null</code> using Sk-connection
 * {@link ISkConnectionSupplier#defConn()} for users management.
 * <p>
 * TODO add an ability to specify {@link IdChain} of used connection
 *
 * @author hazard157
 */
public class UipartSkUsersEditor
    extends SkMwsAbstractPart {

  /**
   * Changes selection in panel when {@link ICurrentUsersMwsSkUserService#current()} changes.
   */
  private final ICurrentEntityChangeListener<ISkUser> currentUsersMwsSkUserServiceListener =
      aCurrent -> this.usersEditor.setSelectedItem( aCurrent );

  /**
   * Changes {@link ICurrentUsersMwsSkUserService#current()} when selection in panel changes.
   */
  private final ITsSelectionChangeListener<ISkUser> panelSelectionChangeListener =
      ( aSource, aSelectedItem ) -> this.currentUsersMwsSkUserService.setCurrent( aSelectedItem );

  @Inject
  ICurrentUsersMwsSkUserService currentUsersMwsSkUserService;

  PanelSkUsersEditor usersEditor;

  @Override
  protected void doCreateContent( TsComposite aParent ) {
    ITsGuiContext ctx = new TsGuiContext( tsContext() );
    usersEditor = new PanelSkUsersEditor( ctx, null );
    usersEditor.createControl( aParent );
    usersEditor.addTsSelectionListener( panelSelectionChangeListener );
    currentUsersMwsSkUserService.addCurrentEntityChangeListener( currentUsersMwsSkUserServiceListener );
  }

}
