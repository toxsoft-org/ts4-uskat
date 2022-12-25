package org.toxsoft.uskat.onews.mws.e4.uiparts;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.ctx.impl.*;
import org.toxsoft.core.tsgui.widgets.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.uskat.base.gui.conn.*;
import org.toxsoft.uskat.base.gui.e4.uiparts.*;
import org.toxsoft.uskat.onews.gui.panel.*;

/**
 * Uipart contains {@link PanelOneWsProfilesEditor}.
 * <p>
 * By default sets {@link PanelOneWsProfilesEditor#getUsedConnectionId()} to <code>null</code> using Sk-connection
 * {@link ISkConnectionSupplier#defConn()} for roles management.
 * <p>
 * TODO add an ability to specify {@link IdChain} of used connection
 *
 * @author hazard157
 */
public class UipartSkOneWsProfilesEditor
    extends SkMwsAbstractPart {

  PanelOneWsProfilesEditor rolesEditor;

  @Override
  protected void doCreateContent( TsComposite aParent ) {
    ITsGuiContext ctx = new TsGuiContext( tsContext() );
    rolesEditor = new PanelOneWsProfilesEditor( ctx, null );
    rolesEditor.createControl( aParent );
  }

}
