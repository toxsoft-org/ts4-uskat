package org.toxsoft.uskat.core.gui.km5.sded.sded;

import static org.toxsoft.core.tsgui.m5.gui.mpc.IMultiPaneComponentConstants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.m5.gui.mpc.impl.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.m5.gui.panels.impl.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.uskat.core.api.objserv.*;

/**
 * Panel creator for {@link SdedDtoClassInfoM5Model}.
 *
 * @author dima
 */
class SdedDtoFullObjectM5PanelCreator
    extends M5DefaultPanelCreator<IDtoFullObject> {

  /**
   * Constructor.
   */
  public SdedDtoFullObjectM5PanelCreator() {
    // nop
  }

  @Override
  protected IM5EntityPanel<IDtoFullObject> doCreateEntityEditorPanel( ITsGuiContext aContext,
      IM5LifecycleManager<IDtoFullObject> aLifecycleManager ) {
    return new SdedDtoFullObjectM5EntityPanel( aContext, model(), aLifecycleManager );
  }

  @Override
  protected IM5CollectionPanel<IDtoFullObject> doCreateCollEditPanel( ITsGuiContext aContext,
      IM5ItemsProvider<IDtoFullObject> aItemsProvider, IM5LifecycleManager<IDtoFullObject> aLifecycleManager ) {
    OPDEF_IS_ACTIONS_CRUD.setValue( aContext.params(), AV_TRUE );
    OPDEF_IS_ACTIONS_HIDE_PANES.setValue( aContext.params(), AV_TRUE );
    OPDEF_IS_FILTER_PANE.setValue( aContext.params(), AV_TRUE );
    OPDEF_IS_SUPPORTS_TREE.setValue( aContext.params(), AV_TRUE );
    MultiPaneComponentModown<IDtoFullObject> mpc =
        new SdedDtoFullObjectM5Mpc( aContext, model(), aItemsProvider, aLifecycleManager );
    return new M5CollectionPanelMpcModownWrapper<>( mpc, false );
  }

}
