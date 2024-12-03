package org.toxsoft.uskat.core.gui.km5.sded.objed;

import static org.toxsoft.core.tsgui.m5.gui.mpc.IMultiPaneComponentConstants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.core.gui.km5.sded.IKM5SdedConstants.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.m5.gui.panels.impl.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.km5.*;

/**
 * M5-model of the {@link ISkObject}.
 * <p>
 * This model exposes only {@link ISkObject} properties: SKID, name and description. Model uses
 * {@link SdedSkObjectM5LifecycleManager} allowing basic CRUD operations.
 *
 * @author hazard157
 */
public class SdedSkObjectM5Model
    extends KM5ModelBasic<ISkObject> {

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SdedSkObjectM5Model( ISkConnection aConn ) {
    super( MID_SDED_SK_OBJECT, ISkObject.class, aConn );
    addFieldDefs( SKID, CLASS_ID, STRID, NAME, DESCRIPTION );
    setPanelCreator( new M5DefaultPanelCreator<>() {

      protected IM5CollectionPanel<ISkObject> doCreateCollEditPanel( ITsGuiContext aContext,
          IM5ItemsProvider<ISkObject> aItemsProvider, IM5LifecycleManager<ISkObject> aLifecycleManager ) {
        OPDEF_IS_ACTIONS_CRUD.setValue( aContext.params(), AV_TRUE );
        OPDEF_IS_ACTIONS_HIDE_PANES.setValue( aContext.params(), AV_TRUE );
        OPDEF_IS_FILTER_PANE.setValue( aContext.params(), AV_TRUE );
        OPDEF_IS_SUPPORTS_TREE.setValue( aContext.params(), AV_TRUE );
        SdedSkObjectMpc mpc = new SdedSkObjectMpc( aContext, model(), aItemsProvider, aLifecycleManager );
        if( aLifecycleManager instanceof SdedSkObjectM5LifecycleManager ) {
          ((SdedSkObjectM5LifecycleManager)aLifecycleManager).setMpc( mpc );
        }
        return new M5CollectionPanelMpcModownWrapper<>( mpc, false );
      }

      @Override
      protected IM5CollectionPanel<ISkObject> doCreateCollChecksPanel( ITsGuiContext aContext,
          IM5ItemsProvider<ISkObject> aItemsProvider ) {
        OPDEF_IS_ACTIONS_CRUD.setValue( aContext.params(), AV_FALSE );
        OPDEF_IS_ACTIONS_HIDE_PANES.setValue( aContext.params(), AV_TRUE );
        OPDEF_IS_FILTER_PANE.setValue( aContext.params(), AV_TRUE );
        OPDEF_IS_SUPPORTS_CHECKS.setValue( aContext.params(), AV_TRUE );
        OPDEF_IS_SUPPORTS_TREE.setValue( aContext.params(), AV_TRUE );
        SdedSkObjectMpc mpc = new SdedSkObjectMpc( aContext, model(), aItemsProvider );

        return new M5CollectionPanelMpcModownWrapper<>( mpc, true );
      }

    } );
  }

  // ------------------------------------------------------------------------------------
  // M5Model
  //

  @Override
  protected IM5LifecycleManager<ISkObject> doCreateDefaultLifecycleManager() {
    SdedSkObjectM5LifecycleManager retVal = new SdedSkObjectM5LifecycleManager( this, skConn() );
    return retVal;
  }

  @Override
  protected IM5LifecycleManager<ISkObject> doCreateLifecycleManager( Object aMaster ) {
    return new SdedSkObjectM5LifecycleManager( this, ISkConnection.class.cast( aMaster ) );
  }

}
