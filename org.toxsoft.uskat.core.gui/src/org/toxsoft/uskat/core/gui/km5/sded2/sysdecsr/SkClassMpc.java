package org.toxsoft.uskat.core.gui.km5.sded2.sysdecsr;

import static org.toxsoft.uskat.core.gui.km5.sded2.IKM5Sded2Constants.*;
import static org.toxsoft.uskat.core.gui.km5.sded2.sysdecsr.ISkResources.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.tstree.tmm.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.mpc.impl.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.utils.*;

/**
 * {@link MultiPaneComponentModown} implementation for {@link Sded2SkClassInfoM5Model}.
 *
 * @author hazard157
 */
class SkClassMpc
    extends MultiPaneComponentModown<ISkClassInfo>
    implements ISkGuiContextable {

  public SkClassMpc( ITsGuiContext aContext, IM5Model<ISkClassInfo> aModel,
      IM5ItemsProvider<ISkClassInfo> aItemsProvider, IM5LifecycleManager<ISkClassInfo> aLifecycleManager ) {
    super( aContext, aModel, aItemsProvider, aLifecycleManager );
    // add tree nodes
    TreeModeInfo<ISkClassInfo> tmiByHierarchy = new TreeModeInfo<>( "ByHierarchy", //$NON-NLS-1$
        STR_TMI_CLASS_BY_HIERARCHY, STR_TMI_CLASS_BY_HIERARCHY_D, null,
        new SkClassTreeMakers.ByHierarchy( aContext, skConn() ) );
    treeModeManager().addTreeMode( tmiByHierarchy );
    treeModeManager().setCurrentMode( tmiByHierarchy.id() );
  }

  // ------------------------------------------------------------------------------------
  // ISkGuiContextable
  //

  @Override
  public ISkConnection skConn() {
    return model().domain().tsContext().get( ISkConnection.class );
  }

  // ------------------------------------------------------------------------------------
  // MultiPaneComponentModown
  //

  @Override
  protected void doTuneBeforeDisplay() {
    tree().console().expandAll();
  }

  @Override
  protected void doAdjustEntityCreationInitialValues( IM5BunchEdit<ISkClassInfo> aValues ) {
    // if there is a selected class initialize new class as child of selected one
    ISkClassInfo sel = selectedItem();
    if( sel != null ) {
      aValues.set( FID_PARENT_ID, sel.id() );
    }
  }

}
