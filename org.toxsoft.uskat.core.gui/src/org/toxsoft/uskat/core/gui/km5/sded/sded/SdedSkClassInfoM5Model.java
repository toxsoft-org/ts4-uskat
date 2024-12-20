package org.toxsoft.uskat.core.gui.km5.sded.sded;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.core.tsgui.m5.gui.mpc.IMultiPaneComponentConstants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.gui.km5.sded.IKM5SdedConstants.*;
import static org.toxsoft.uskat.core.gui.km5.sded.ISkSdedKm5SharedResources.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.m5.gui.mpc.impl.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.m5.gui.panels.impl.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tsgui.m5.std.models.misc.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.km5.*;

/**
 * M5-model of the {@link ISkClassInfo}.
 *
 * @author hazard157
 */
public class SdedSkClassInfoM5Model
    extends KM5ConnectedModelBase<ISkClassInfo> {

  /**
   * Attribute {@link ISkClassInfo#id()}.
   */
  public final IM5AttributeFieldDef<ISkClassInfo> CLASS_ID = new M5AttributeFieldDef<>( FID_CLASS_ID, DDEF_IDPATH ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_N_CLASS_ID, STR_D_CLASS_ID );
      setFlags( M5FF_INVARIANT | M5FF_COLUMN );
    }

    protected IAtomicValue doGetFieldValue( ISkClassInfo aEntity ) {
      return avStr( aEntity.id() );
    }

  };

  /**
   * Attribute {@link ISkClassInfo#parentId()}.
   */
  public final IM5SingleLookupFieldDef<ISkClassInfo, String> PARENT_ID =
      new M5SingleLookupFieldDef<>( FID_PARENT_ID, StringM5Model.MODEL_ID ) {

        @Override
        protected void doInit() {
          setNameAndDescription( STR_N_PARENT_ID, STR_D_PARENT_ID );
          setFlags( M5FF_INVARIANT | M5FF_DETAIL );
          setDefaultValue( IGwHardConstants.GW_ROOT_CLASS_ID );
          setLookupProvider( () -> {
            IStridablesList<ISkClassInfo> llAll = skSysdescr().listClasses();

            // TODO Auto-generated method stub
            return llAll.keys();
          } );
        }

        protected String doGetFieldValue( ISkClassInfo aEntity ) {
          return aEntity.parentId();
        }

      };

  /**
   * Attribute {@link ISkClassInfo#nmName()}.
   */
  public final IM5AttributeFieldDef<ISkClassInfo> NAME = new M5AttributeFieldDef<>( FID_NAME, DDEF_NAME ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_N_CLASS_NAME, STR_D_CLASS_NAME );
      setFlags( M5FF_COLUMN );
    }

    protected IAtomicValue doGetFieldValue( ISkClassInfo aEntity ) {
      return avStr( aEntity.nmName() );
    }

  };

  /**
   * Attribute {@link ISkClassInfo#description()}.
   */
  public final IM5AttributeFieldDef<ISkClassInfo> DESCRIPTION =
      new M5AttributeFieldDef<>( FID_DESCRIPTION, DDEF_DESCRIPTION ) {

        @Override
        protected void doInit() {
          setNameAndDescription( STR_N_CLASS_DESCRIPTION, STR_D_CLASS_DESCRIPTION );
          setFlags( M5FF_DETAIL );
        }

        protected IAtomicValue doGetFieldValue( ISkClassInfo aEntity ) {
          return avStr( aEntity.description() );
        }

      };

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SdedSkClassInfoM5Model( ISkConnection aConn ) {
    super( MID_SDED_SK_CLASS_INFO, ISkClassInfo.class, aConn );
    setNameAndDescription( STR_N_M5M_CLASS, STR_D_M5M_CLASS );
    addFieldDefs( CLASS_ID, NAME, PARENT_ID, DESCRIPTION );
    setPanelCreator( new M5DefaultPanelCreator<>() {

      protected IM5CollectionPanel<ISkClassInfo> doCreateCollEditPanel( ITsGuiContext aContext,
          IM5ItemsProvider<ISkClassInfo> aItemsProvider, IM5LifecycleManager<ISkClassInfo> aLifecycleManager ) {
        OPDEF_IS_ACTIONS_CRUD.setValue( aContext.params(), AV_TRUE );
        OPDEF_IS_ACTIONS_HIDE_PANES.setValue( aContext.params(), AV_TRUE );
        OPDEF_IS_FILTER_PANE.setValue( aContext.params(), AV_TRUE );
        OPDEF_IS_SUPPORTS_TREE.setValue( aContext.params(), AV_TRUE );
        MultiPaneComponentModown<ISkClassInfo> mpc =
            new SdedSkClassInfoMpc( aContext, model(), aItemsProvider, aLifecycleManager );
        return new M5CollectionPanelMpcModownWrapper<>( mpc, false );
      }

      protected IM5CollectionPanel<ISkClassInfo> doCreateCollViewerPanel( ITsGuiContext aContext,
          IM5ItemsProvider<ISkClassInfo> aItemsProvider ) {
        OPDEF_IS_ACTIONS_HIDE_PANES.setValue( aContext.params(), AV_FALSE );
        OPDEF_IS_ACTIONS_CRUD.setValue( aContext.params(), AV_FALSE );
        OPDEF_IS_FILTER_PANE.setValue( aContext.params(), AV_TRUE );
        OPDEF_IS_DETAILS_PANE.setValue( aContext.params(), AV_FALSE );
        OPDEF_IS_SUMMARY_PANE.setValue( aContext.params(), AV_FALSE );
        MultiPaneComponentModown<ISkClassInfo> mpc = new SdedSkClassInfoMpc( aContext, model(), aItemsProvider, null );
        return new M5CollectionPanelMpcModownWrapper<>( mpc, true );
      }

    } );
  }

  // ------------------------------------------------------------------------------------
  // M5Model
  //

  @Override
  protected IM5LifecycleManager<ISkClassInfo> doCreateDefaultLifecycleManager() {
    return new SdedSkClassInfoM5LifecycleManager( this, skConn() );
  }

  @Override
  protected IM5LifecycleManager<ISkClassInfo> doCreateLifecycleManager( Object aMaster ) {
    return new SdedSkClassInfoM5LifecycleManager( this, ISkConnection.class.cast( aMaster ) );
  }

}
