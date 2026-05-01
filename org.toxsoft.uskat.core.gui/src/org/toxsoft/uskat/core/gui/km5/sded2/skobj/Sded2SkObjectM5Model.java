package org.toxsoft.uskat.core.gui.km5.sded2.skobj;

import static org.toxsoft.core.tsgui.m5.gui.mpc.IMultiPaneComponentConstants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.core.gui.km5.sded2.IKM5Sded2Constants.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.m5.gui.mpc.impl.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.m5.gui.panels.impl.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.km5.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * M5-model of {@link ISkObject} to be used with SDED object editor.
 * <p>
 * Note: this model does not creates lifecycle manager, manually create {@link Sded2SkObjectM5LifecycleManager} instead.
 *
 * @author hazard157
 */
public class Sded2SkObjectM5Model
    extends KM5ModelBasic<ISkObject>
    implements ISkConnected {

  /**
   * The model ID.
   */
  public static final String MODEL_ID = SDED2_M5_ID + ".SkObject"; //$NON-NLS-1$

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public Sded2SkObjectM5Model( ISkConnection aConn ) {
    super( MODEL_ID, ISkObject.class, aConn );
    addFieldDefs( STRID, NAME, DESCRIPTION );
    setPanelCreator( new M5DefaultPanelCreator<>() {
      protected IM5CollectionPanel<ISkObject> doCreateCollViewerPanel( ITsGuiContext aContext,
          IM5ItemsProvider<ISkObject> aItemsProvider ) {
        OPDEF_IS_ACTIONS_CRUD.setValue( aContext.params(), AV_FALSE );
        MultiPaneComponentModown<ISkObject> mpc = new Sded2SkObjectMpc( aContext, model(), aItemsProvider );
        return new M5CollectionPanelMpcModownWrapper<>( mpc, true );
      }

      protected IM5CollectionPanel<ISkObject> doCreateCollEditPanel( ITsGuiContext aContext,
          IM5ItemsProvider<ISkObject> aItemsProvider, IM5LifecycleManager<ISkObject> aLifecycleManager ) {
        OPDEF_IS_ACTIONS_CRUD.setValue( aContext.params(), AV_TRUE );
        OPDEF_IS_ADD_COPY_ACTION.setValue( aContext.params(), AV_TRUE );
        MultiPaneComponentModown<ISkObject> mpc =
            new Sded2SkObjectMpc( aContext, model(), aItemsProvider, aLifecycleManager );
        return new M5CollectionPanelMpcModownWrapper<>( mpc, false );
      }
    } );
  }

}
