package org.toxsoft.uskat.core.gui.km5.sded.sded;

import static org.toxsoft.core.tsgui.m5.gui.mpc.IMultiPaneComponentConstants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.m5.gui.mpc.impl.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.m5.gui.panels.impl.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * Panel creator for {@link SdedDtoClassInfoM5Model}.
 *
 * @author dima
 */
class SdedDtoClassInfoM5PanelCreator
    extends M5DefaultPanelCreator<IDtoClassInfo> {

  /**
   * Constructor.
   */
  public SdedDtoClassInfoM5PanelCreator() {
    // nop
  }

  @Override
  protected IM5EntityPanel<IDtoClassInfo> doCreateEntityEditorPanel( ITsGuiContext aContext,
      IM5LifecycleManager<IDtoClassInfo> aLifecycleManager ) {
    return new SdedDtoClassInfoM5EntityPanel( aContext, model(), aLifecycleManager );
  }

  @Override
  protected IM5CollectionPanel<IDtoClassInfo> doCreateCollEditPanel( ITsGuiContext aContext,
      IM5ItemsProvider<IDtoClassInfo> aItemsProvider, IM5LifecycleManager<IDtoClassInfo> aLifecycleManager ) {
    OPDEF_IS_ACTIONS_CRUD.setValue( aContext.params(), AV_TRUE );
    OPDEF_IS_ACTIONS_HIDE_PANES.setValue( aContext.params(), AV_TRUE );
    OPDEF_IS_FILTER_PANE.setValue( aContext.params(), AV_TRUE );
    OPDEF_IS_SUPPORTS_TREE.setValue( aContext.params(), AV_TRUE );
    MultiPaneComponentModown<IDtoClassInfo> mpc =
        new SdedDtoClassInfoM5Mpc( aContext, model(), aItemsProvider, aLifecycleManager );
    return new M5CollectionPanelMpcModownWrapper<>( mpc, false );
  }

}
