package org.toxsoft.uskat.core.gui.km5.sded.sded;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.mpc.impl.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.uskat.core.api.objserv.*;

/**
 * {@link MultiPaneComponentModown} implementation to be used in collection panels of {@link SdedDtoClassInfoM5Model}.
 *
 * @author hazard157
 */
public class SdedDtoFullObjectM5Mpc
    extends MultiPaneComponentModown<IDtoFullObject> {

  /**
   * Constructor.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @param aModel {@link IM5Model} - the model
   * @param aItemsProvider {@link IM5ItemsProvider} - the items provider or <code>null</code>
   * @param aLifecycleManager {@link IM5LifecycleManager} - the lifecycle manager or <code>null</code>
   */
  public SdedDtoFullObjectM5Mpc( ITsGuiContext aContext, IM5Model<IDtoFullObject> aModel,
      IM5ItemsProvider<IDtoFullObject> aItemsProvider, IM5LifecycleManager<IDtoFullObject> aLifecycleManager ) {
    super( aContext, aModel, aItemsProvider, aLifecycleManager );
  }

}
