package org.toxsoft.uskat.core.gui.km5.sded2.sysdecsr;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.core.tsgui.m5.gui.mpc.IMultiPaneComponentConstants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.api.sysdescr.ESkClassPropKind.*;
import static org.toxsoft.uskat.core.gui.ISkCoreGuiConstants.*;
import static org.toxsoft.uskat.core.gui.km5.sded2.IKM5Sded2Constants.*;
import static org.toxsoft.uskat.core.gui.km5.sded2.sysdecsr.ISkResources.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.m5.gui.mpc.impl.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.m5.gui.panels.impl.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tsgui.m5.std.models.misc.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.km5.*;

/**
 * M5-model of {@link ISkClassInfo}.
 *
 * @author hazard157
 */
public class Sded2SkClassInfoM5Model
    extends KM5ConnectedModelBase<ISkClassInfo> {

  /**
   * The model ID.
   */
  public static final String MODEL_ID = SDED2_M5_ID + ".SkClassInfo"; //$NON-NLS-1$

  /**
   * Field {@link ISkClassInfo#id()}.
   */
  public final IM5AttributeFieldDef<ISkClassInfo> CLASS_ID = new M5AttributeFieldDef<>( FID_CLASS_ID, DDEF_IDPATH, //
      TSID_NAME, STR_SCI_CLASS_ID, //
      TSID_DESCRIPTION, STR_SCI_CLASS_ID_D, //
      M5_OPDEF_FLAGS, avInt( M5FF_INVARIANT | M5FF_COLUMN ) //
  ) {

    protected IAtomicValue doGetFieldValue( ISkClassInfo aEntity ) {
      return avStr( aEntity.id() );
    }

  };

  /**
   * Field {@link ISkClassInfo#parentId()}.
   */
  public final IM5SingleLookupFieldDef<ISkClassInfo, String> PARENT_ID =
      new M5SingleLookupFieldDef<>( FID_PARENT_ID, StringM5Model.MODEL_ID, //
          TSID_NAME, STR_SCI_PARENT_ID, //
          TSID_DESCRIPTION, STR_SCI_PARENT_ID_D, //
          M5_OPDEF_FLAGS, avInt( M5FF_INVARIANT | M5FF_DETAIL ), //
          TSID_DEFAULT_VALUE, avStr( IGwHardConstants.GW_ROOT_CLASS_ID ) //
      ) {

        @Override
        protected void doInit() {
          setLookupProvider( () -> skSysdescr().listClasses().keys() );
        }

        protected String doGetFieldValue( ISkClassInfo aEntity ) {
          return aEntity.parentId();
        }

      };

  /**
   * Field {@link ISkClassInfo#nmName()}.
   */
  public final IM5AttributeFieldDef<ISkClassInfo> NAME = new M5AttributeFieldDef<>( FID_NAME, DDEF_NAME, //
      TSID_NAME, STR_SCI_NAME, //
      TSID_DESCRIPTION, STR_SCI_NAME_D, //
      M5_OPDEF_FLAGS, avInt( M5FF_COLUMN ) //
  ) {

    protected IAtomicValue doGetFieldValue( ISkClassInfo aEntity ) {
      return avStr( aEntity.nmName() );
    }

  };

  /**
   * Attribute {@link ISkClassInfo#description()}.
   */
  public final IM5AttributeFieldDef<ISkClassInfo> DESCRIPTION =
      new M5AttributeFieldDef<>( FID_DESCRIPTION, DDEF_DESCRIPTION, //
          TSID_NAME, STR_SCI_DECSRIPTION, //
          TSID_DESCRIPTION, STR_SCI_DECSRIPTION_D, //
          M5_OPDEF_FLAGS, avInt( M5FF_DETAIL ) //
      ) {

        protected IAtomicValue doGetFieldValue( ISkClassInfo aEntity ) {
          return avStr( aEntity.description() );
        }

      };

  /**
   * Field contains of list attributes {@link IDtoClassInfo#attrInfos()}.
   */
  public final IM5MultiModownFieldDef<ISkClassInfo, IDtoAttrInfo> SELF_ATTR_INFOS =
      new M5MultiModownFieldDef<>( FID_SELF_ATTR_INFOS, Sded2DtoAttrInfoM5Model.MODEL_ID ) {

        @Override
        protected void doInit() {
          setNameAndDescription( ATTR.pluralName(), ATTR.description() );
          params().setStr( TSID_ICON_ID, ICONID_SDED_CLASS_ATTR );
          params().setBool( IValedControlConstants.OPDEF_IS_HEIGHT_FIXED, false );
          params().setInt( IValedControlConstants.OPDEF_VERTICAL_SPAN, 10 );
          setFlags( 0 );
        }

        protected IList<IDtoAttrInfo> doGetFieldValue( ISkClassInfo aEntity ) {
          return aEntity.attrs().listSelf();
        }

      };

  /**
   * Field contains of list rivets {@link IDtoClassInfo#rivetInfos()}.
   */
  public final IM5MultiModownFieldDef<ISkClassInfo, IDtoRivetInfo> SELF_RIVET_INFOS =
      new M5MultiModownFieldDef<>( FID_SELF_RIVET_INFOS, Sded2DtoRivetInfoM5Model.MODEL_ID ) {

        @Override
        protected void doInit() {
          setNameAndDescription( RIVET.pluralName(), RIVET.description() );
          params().setStr( TSID_ICON_ID, ICONID_SDED_CLASS_RIVET );
          params().setBool( IValedControlConstants.OPDEF_IS_HEIGHT_FIXED, false );
          params().setInt( IValedControlConstants.OPDEF_VERTICAL_SPAN, 10 );
          setFlags( 0 );
        }

        protected IList<IDtoRivetInfo> doGetFieldValue( ISkClassInfo aEntity ) {
          return aEntity.rivets().listSelf();
        }

      };

  /**
   * Field contains of list links {@link IDtoClassInfo#linkInfos()}.
   */
  public final IM5MultiModownFieldDef<ISkClassInfo, IDtoLinkInfo> SELF_LINK_INFOS =
      new M5MultiModownFieldDef<>( FID_SELF_LINK_INFOS, Sded2DtoLinkInfoM5Model.MODEL_ID ) {

        @Override
        protected void doInit() {
          setNameAndDescription( LINK.pluralName(), LINK.description() );
          params().setStr( TSID_ICON_ID, ICONID_SDED_CLASS_LINK );
          params().setBool( IValedControlConstants.OPDEF_IS_HEIGHT_FIXED, false );
          params().setInt( IValedControlConstants.OPDEF_VERTICAL_SPAN, 10 );
          setFlags( 0 );
        }

        protected IList<IDtoLinkInfo> doGetFieldValue( ISkClassInfo aEntity ) {
          return aEntity.links().listSelf();
        }

      };

  /**
   * Field contains of list rtdata {@link IDtoClassInfo#rtdataInfos()}.
   */
  public final IM5MultiModownFieldDef<ISkClassInfo, IDtoRtdataInfo> SELF_RTDATA_INFOS =
      new M5MultiModownFieldDef<>( FID_SELF_RTDATA_INFOS, Sded2DtoRtdataInfoM5Model.MODEL_ID ) {

        @Override
        protected void doInit() {
          setNameAndDescription( RTDATA.pluralName(), RTDATA.description() );
          params().setStr( TSID_ICON_ID, ICONID_SDED_CLASS_DATA );
          params().setBool( IValedControlConstants.OPDEF_IS_HEIGHT_FIXED, false );
          params().setInt( IValedControlConstants.OPDEF_VERTICAL_SPAN, 10 );
          setFlags( 0 );
        }

        protected IList<IDtoRtdataInfo> doGetFieldValue( ISkClassInfo aEntity ) {
          return aEntity.rtdata().listSelf();
        }

      };

  /**
   * Field contains of list commands {@link IDtoClassInfo#cmdInfos()}.
   */
  public final IM5MultiModownFieldDef<ISkClassInfo, IDtoCmdInfo> SELF_CMD_INFOS =
      new M5MultiModownFieldDef<>( FID_SELF_CMD_INFOS, Sded2DtoCmdInfoM5Model.MODEL_ID ) {

        @Override
        protected void doInit() {
          setNameAndDescription( CMD.pluralName(), CMD.description() );
          params().setStr( TSID_ICON_ID, ICONID_SDED_CLASS_CMD );
          params().setBool( IValedControlConstants.OPDEF_IS_HEIGHT_FIXED, false );
          params().setInt( IValedControlConstants.OPDEF_VERTICAL_SPAN, 10 );
          setFlags( 0 );
        }

        protected IList<IDtoCmdInfo> doGetFieldValue( ISkClassInfo aEntity ) {
          return aEntity.cmds().listSelf();
        }

      };

  /**
   * Field contains of list events {@link IDtoClassInfo#eventInfos()}.
   */
  public final IM5MultiModownFieldDef<ISkClassInfo, IDtoEventInfo> SELF_EVENT_INFOS =
      new M5MultiModownFieldDef<>( FID_SELF_EVENT_INFOS, Sded2DtoEventInfoM5Model.MODEL_ID ) {

        @Override
        protected void doInit() {
          setNameAndDescription( EVENT.pluralName(), EVENT.description() );
          params().setStr( TSID_ICON_ID, ICONID_SDED_CLASS_EVENT );
          params().setBool( IValedControlConstants.OPDEF_IS_HEIGHT_FIXED, false );
          params().setInt( IValedControlConstants.OPDEF_VERTICAL_SPAN, 10 );
          setFlags( 0 );
        }

        protected IList<IDtoEventInfo> doGetFieldValue( ISkClassInfo aEntity ) {
          return aEntity.events().listSelf();
        }

      };

  /**
   * Field contains of list CLOBs {@link IDtoClassInfo#clobInfos()}.
   */
  public final IM5MultiModownFieldDef<ISkClassInfo, IDtoClobInfo> SELF_CLOB_INFOS =
      new M5MultiModownFieldDef<>( FID_SELF_CLOB_INFOS, Sded2DtoClobInfoM5Model.MODEL_ID ) {

        @Override
        protected void doInit() {
          setNameAndDescription( CLOB.pluralName(), CLOB.description() );
          params().setStr( TSID_ICON_ID, ICONID_SDED_CLASS_CLOB );
          params().setBool( IValedControlConstants.OPDEF_IS_HEIGHT_FIXED, false );
          params().setInt( IValedControlConstants.OPDEF_VERTICAL_SPAN, 10 );
          setFlags( 0 );
        }

        protected IList<IDtoClobInfo> doGetFieldValue( ISkClassInfo aEntity ) {
          return aEntity.clobs().listSelf();
        }

      };

  /**
   * Field contains {@link ISkClassInfo#listProps(boolean, boolean, boolean) ISkClassInfo#listProps( true, true, false
   * )}.
   */
  public final IM5MultiModownFieldDef<ISkClassInfo, IDtoClassPropInfoBase> ALL_PROPS =
      new M5MultiModownFieldDef<>( FID_ALL_PROP_INFOS, Sded2DtoPropInfoM5Model.MODEL_ID ) {

        @Override
        protected void doInit() {
          setNameAndDescription( STR_TAB_BROWSE, STR_TAB_BROWSE_D );
          params().setStr( TSID_ICON_ID, ICONID_SDED_CLASS_CLOB );
          params().setBool( IValedControlConstants.OPDEF_IS_HEIGHT_FIXED, false );
          params().setInt( IValedControlConstants.OPDEF_VERTICAL_SPAN, 10 );
          setFlags( M5FF_READ_ONLY );
        }

        protected IList<IDtoClassPropInfoBase> doGetFieldValue( ISkClassInfo aEntity ) {
          return aEntity.listProps( true, true, false );
        }

      };

  // TODO field PARAMS ?

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public Sded2SkClassInfoM5Model( ISkConnection aConn ) {
    super( MODEL_ID, ISkClassInfo.class, aConn );
    setNameAndDescription( STR_M5M_CLASS, STR_M5M_CLASS_D );
    addFieldDefs( CLASS_ID, NAME, PARENT_ID, DESCRIPTION, SELF_ATTR_INFOS, SELF_RIVET_INFOS, SELF_LINK_INFOS,
        SELF_RTDATA_INFOS, SELF_CMD_INFOS, SELF_EVENT_INFOS, SELF_CLOB_INFOS, ALL_PROPS );
    setPanelCreator( new M5DefaultPanelCreator<>() {

      @Override
      protected IM5CollectionPanel<ISkClassInfo> doCreateCollEditPanel( ITsGuiContext aContext,
          IM5ItemsProvider<ISkClassInfo> aItemsProvider, IM5LifecycleManager<ISkClassInfo> aLifecycleManager ) {
        OPDEF_IS_ACTIONS_CRUD.setValue( aContext.params(), AV_TRUE );
        OPDEF_IS_SUPPORTS_TREE.setValue( aContext.params(), AV_TRUE );
        OPDEF_IS_ADD_COPY_ACTION.setValue( aContext.params(), AV_TRUE );
        MultiPaneComponentModown<ISkClassInfo> mpc =
            new SkClassMpc( aContext, model(), aItemsProvider, aLifecycleManager );
        return new M5CollectionPanelMpcModownWrapper<>( mpc, false );
      }

      @Override
      protected IM5CollectionPanel<ISkClassInfo> doCreateCollViewerPanel( ITsGuiContext aContext,
          IM5ItemsProvider<ISkClassInfo> aItemsProvider ) {
        OPDEF_IS_ACTIONS_CRUD.setValue( aContext.params(), AV_FALSE );
        OPDEF_IS_SUPPORTS_TREE.setValue( aContext.params(), AV_TRUE );
        MultiPaneComponentModown<ISkClassInfo> mpc = new SkClassMpc( aContext, model(), aItemsProvider, null );
        return new M5CollectionPanelMpcModownWrapper<>( mpc, true );
      }

      @Override
      protected IM5FilterPanel<ISkClassInfo> doCreateFilterPanel( ITsGuiContext aContext ) {
        return new SkClassFilterPane( aContext, model() );
      }

      @Override
      protected IM5EntityPanel<ISkClassInfo> doCreateEntityEditorPanel( ITsGuiContext aContext,
          IM5LifecycleManager<ISkClassInfo> aLifecycleManager ) {
        return new SkClassEntityPanel( aContext, model(), aLifecycleManager );
      }
    } );
  }

  // ------------------------------------------------------------------------------------
  // M5Model
  //

  @Override
  protected IM5LifecycleManager<ISkClassInfo> doCreateDefaultLifecycleManager() {
    return new SkClassM5LifecycleManager( this, skConn() );
  }

  @Override
  protected IM5LifecycleManager<ISkClassInfo> doCreateLifecycleManager( Object aMaster ) {
    return new SkClassM5LifecycleManager( this, ISkConnection.class.cast( aMaster ) );
  }

}
