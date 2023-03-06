package org.toxsoft.uskat.base.gui.conn.cfg.m5;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.core.tsgui.m5.gui.mpc.IMultiPaneComponentConstants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.base.gui.conn.cfg.m5.IConnectionConfigM5Constants.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.mpc.impl.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.m5.gui.panels.impl.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tsgui.valed.controls.basic.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strid.idgen.*;
import org.toxsoft.uskat.base.gui.conn.cfg.*;
import org.toxsoft.uskat.core.*;

/**
 * Panel creator for {@link ConnectionConfigM5Model}.
 *
 * @author hazard157
 */
public class ConnectionConfigM5PanelCreator
    extends M5DefaultPanelCreator<IConnectionConfig> {

  // TODO add the "Create a copy" button to the collection edit panel

  class Controller
      extends M5EntityPanelWithValedsController<IConnectionConfig> {

    @Override
    public void beforeSetValues( IM5Bunch<IConnectionConfig> aValues ) {
      String providerId = (String)aValues.get( FID_PROVIDER_ID );
      prepareValusEditor( providerId );
    }

    @Override
    public boolean doProcessEditorValueChange( IValedControl<?> aEditor, IM5FieldDef<IConnectionConfig, ?> aFieldDef,
        boolean aEditFinished ) {
      switch( aFieldDef.id() ) {
        case FID_PROVIDER_ID:
          // when changing the provider, change the value editor
          String providerId = (String)editors().getByKey( FID_PROVIDER_ID ).getValue();
          prepareValusEditor( providerId );
          // we will try to use the available value as much as possible
          ValedOptionSet vops = getEditor( FID_VALUES, ValedOptionSet.class );
          vops.setValue( lastValues().getAs( FID_VALUES, IOptionSet.class ) );
          break;
        default:
          break;
      }
      return true;
    }

    private void prepareValusEditor( String aProviderId ) {
      IConnectionConfigService ccs = (IConnectionConfigService)panel().lifecycleManager().master();
      ValedOptionSet vops = getEditor( FID_VALUES, ValedOptionSet.class );
      IConnectionConfigProvider p = aProviderId != null ? ccs.listProviders().findByKey( aProviderId ) : null;
      if( p != null ) {
        vops.setOptionDefs( p.opDefs() );
      }
      else {
        vops.setOptionDefs( null );
      }
    }

  }

  private static final String CONN_CFG_ID_PREFIX = ISkHardConstants.SK_ID + "_ConnCfg"; //$NON-NLS-1$

  private final IStridGenerator idGen = new UuidStridGenerator( UuidStridGenerator.createState( CONN_CFG_ID_PREFIX ) );

  /**
   * Constructor.
   */
  public ConnectionConfigM5PanelCreator() {
    // nop
  }

  @Override
  protected IM5EntityPanel<IConnectionConfig> doCreateEntityEditorPanel( ITsGuiContext aContext,
      IM5LifecycleManager<IConnectionConfig> aLifecycleManager ) {
    return new M5DefaultEntityControlledPanel<>( aContext, model(), aLifecycleManager, new Controller() );
  }

  @Override
  protected IM5CollectionPanel<IConnectionConfig> doCreateCollEditPanel( ITsGuiContext aContext,
      IM5ItemsProvider<IConnectionConfig> aItemsProvider, IM5LifecycleManager<IConnectionConfig> aLifecycleManager ) {
    OPDEF_IS_ACTIONS_CRUD.setValue( aContext.params(), AV_TRUE );
    MultiPaneComponentModown<IConnectionConfig> mpc =
        new MultiPaneComponentModown<>( aContext, model(), aItemsProvider, aLifecycleManager ) {

          @Override
          protected void doAdjustEntityCreationInitialValues( IM5BunchEdit<IConnectionConfig> aValues ) {
            String id = idGen.nextId();
            aValues.set( FID_ID, avStr( id ) );
          }
        };
    return new M5CollectionPanelMpcModownWrapper<>( mpc, false );
  }

}
