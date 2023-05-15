package org.toxsoft.skide.plugin.exconn.main;

import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.ctx.impl.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.utils.layout.*;
import org.toxsoft.core.tsgui.widgets.*;
import org.toxsoft.skide.core.api.*;
import org.toxsoft.skide.core.api.impl.*;
import org.toxsoft.uskat.core.gui.conn.cfg.*;
import org.toxsoft.uskat.core.gui.conn.cfg.m5.*;

/**
 * {@link AbstractSkideUnitPanel} implementation.
 *
 * @author hazard157
 */
class SkideExconnUnitPanel
    extends AbstractSkideUnitPanel {

  private TsComposite                           backplane;
  private IM5CollectionPanel<IConnectionConfig> cfgsListPanel;

  public SkideExconnUnitPanel( ITsGuiContext aContext, ISkideUnit aUnit ) {
    super( aContext, aUnit );
  }

  @Override
  protected Control doCreateControl( Composite aParent ) {
    backplane = new TsComposite( aParent );
    backplane.setLayout( new BorderLayout() );
    IConnectionConfigService ccService = tsContext().get( IConnectionConfigService.class );
    IM5Model<IConnectionConfig> model =
        m5().getModel( IConnectionConfigM5Constants.MID_SK_CONN_CFG, IConnectionConfig.class );
    IM5LifecycleManager<IConnectionConfig> lm = model.getLifecycleManager( ccService );
    ITsGuiContext ctx = new TsGuiContext( tsContext() );
    cfgsListPanel = model.panelCreator().createCollEditPanel( ctx, lm.itemsProvider(), lm );
    cfgsListPanel.createControl( backplane );
    cfgsListPanel.getControl().setLayoutData( BorderLayout.WEST );

    // TODO SkideExconnUnitPanel.doCreateControl()

    return backplane;
  }

  // --- DEBUG and old code
  // @Override
  // protected Control doCreateControl( Composite aParent ) {
  // CLabel label = new CLabel( aParent, SWT.CENTER );
  // label.setText( "Unit template 1: right panel" ); //$NON-NLS-1$
  // return label;
  // }
  // ---

}
