package org.toxsoft.uskat.users.gui.panels;

import static org.toxsoft.core.tsgui.m5.gui.mpc.IMultiPaneComponentConstants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;

import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.ctx.impl.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.utils.layout.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.base.gui.glib.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.users.gui.km5.*;

/**
 * Self-contaioned panel to edit users {@link ISkUserService#listUsers()}.
 *
 * @author hazard157
 */
public class PanelSkUsersEditor
    extends AbstractSkStdEventsProducerLazyPanel<ISkUser> {

  private final IM5CollectionPanel<ISkUser> panelUsers;

  /**
   * Constructor.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @param aUsedConnId {@link IdChain} - ID of connection to be used, may be <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public PanelSkUsersEditor( ITsGuiContext aContext, IdChain aUsedConnId ) {
    super( aContext, aUsedConnId );
    IM5Model<ISkUser> model = m5().getModel( ISkUser.CLASS_ID, ISkUser.class );
    IM5LifecycleManager<ISkUser> lm = new SkUserM5LifecycleManager( model, skConn() );
    ITsGuiContext ctx = new TsGuiContext( aContext );
    ctx.params().addAll( aContext.params() );
    OPDEF_IS_DETAILS_PANE.setValue( ctx.params(), AV_TRUE );
    OPDEF_DETAILS_PANE_PLACE.setValue( ctx.params(), avValobj( EBorderLayoutPlacement.SOUTH ) );
    OPDEF_IS_SUPPORTS_TREE.setValue( ctx.params(), AV_TRUE );
    OPDEF_IS_ACTIONS_CRUD.setValue( ctx.params(), AV_TRUE );
    OPDEF_IS_FILTER_PANE.setValue( ctx.params(), AV_TRUE );
    panelUsers = model.panelCreator().createCollEditPanel( ctx, lm.itemsProvider(), lm );
    panelUsers.addTsSelectionListener( selectionChangeEventHelper );
    panelUsers.addTsDoubleClickListener( doubleClickEventHelper );
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkStdEventsProducerLazyPanel
  //

  @Override
  protected void doInitGui( Composite aParent ) {
    panelUsers.createControl( aParent );
    panelUsers.getControl().setLayoutData( BorderLayout.CENTER );
  }

  // ------------------------------------------------------------------------------------
  // ITsSelectionProvider
  //

  @Override
  protected ISkUser doGetSelectedItem() {
    return panelUsers.selectedItem();
  }

  @Override
  public void doSetSelectedItem( ISkUser aItem ) {
    panelUsers.setSelectedItem( aItem );
  }

}
