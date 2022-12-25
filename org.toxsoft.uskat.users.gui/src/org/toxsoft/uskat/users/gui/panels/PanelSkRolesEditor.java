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
 * Self-contaioned panel to edit roles {@link ISkUserService#listRoles()}.
 *
 * @author hazard157
 */
public class PanelSkRolesEditor
    extends AbstractSkStdEventsProducerLazyPanel<ISkRole> {

  private final IM5CollectionPanel<ISkRole> panelRoles;

  /**
   * Constructor.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @param aUsedConnId {@link IdChain} - ID of connection to be used, may be <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public PanelSkRolesEditor( ITsGuiContext aContext, IdChain aUsedConnId ) {
    super( aContext, aUsedConnId );
    IM5Model<ISkRole> model = m5().getModel( ISkRole.CLASS_ID, ISkRole.class );
    IM5LifecycleManager<ISkRole> lm = new SkRoleM5LifecycleManager( model, skConn() );
    ITsGuiContext ctx = new TsGuiContext( aContext );
    ctx.params().addAll( aContext.params() );
    OPDEF_IS_DETAILS_PANE.setValue( ctx.params(), AV_TRUE );
    OPDEF_DETAILS_PANE_PLACE.setValue( ctx.params(), avValobj( EBorderLayoutPlacement.SOUTH ) );
    OPDEF_IS_SUPPORTS_TREE.setValue( ctx.params(), AV_TRUE );
    OPDEF_IS_ACTIONS_CRUD.setValue( ctx.params(), AV_TRUE );
    OPDEF_IS_FILTER_PANE.setValue( ctx.params(), AV_TRUE );
    panelRoles = model.panelCreator().createCollEditPanel( ctx, lm.itemsProvider(), lm );
    panelRoles.addTsSelectionListener( selectionChangeEventHelper );
    panelRoles.addTsDoubleClickListener( doubleClickEventHelper );
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkStdEventsProducerLazyPanel
  //

  @Override
  protected void doInitGui( Composite aParent ) {
    panelRoles.createControl( aParent );
    panelRoles.getControl().setLayoutData( BorderLayout.CENTER );
  }

  // ------------------------------------------------------------------------------------
  // ITsSelectionProvider
  //

  @Override
  protected ISkRole doGetSelectedItem() {
    return panelRoles.selectedItem();
  }

  @Override
  protected void doSetSelectedItem( ISkRole aItem ) {
    panelRoles.setSelectedItem( aItem );
  }

}
